package tech.hawkon.pinshift;

import android.content.SharedPreferences;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.view.Gravity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Locale;
import java.util.List;
import java.util.Map;

import tech.hawkon.pinshift.database.DataBaseHistoryLocation;
import tech.hawkon.pinshift.utils.GoUtils;
import tech.hawkon.pinshift.utils.NumericSettings;

public class HistoryActivity extends BaseActivity {
    public static final String RESULT_LOCATION_NAME = "RESULT_LOCATION_NAME";
    public static final String RESULT_LOCATION_LONGITUDE = "RESULT_LOCATION_LONGITUDE";
    public static final String RESULT_LOCATION_LATITUDE = "RESULT_LOCATION_LATITUDE";
    public static final String KEY_ID = "KEY_ID";
    public static final String KEY_LOCATION = "KEY_LOCATION";
    public static final String KEY_TIME = "KEY_TIME";
    public static final String KEY_LNG_LAT_WGS = "KEY_LNG_LAT_WGS";
    public static final String KEY_LNG_LAT_CUSTOM = "KEY_LNG_LAT_CUSTOM";

    private ListView mRecordListView;
    private TextView noRecordText;
    private LinearLayout mSearchLayout;
    private SQLiteDatabase mHistoryLocationDB;
    private List<Map<String, Object>> mAllRecord;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* 为了启动欢迎页全屏，状态栏被设置了透明，但是会导致其他页面状态栏空白
         * 这里设计如下：
         * 1. 除了 WelcomeActivity 之外的所有 Activity 均继承 BaseActivity
         * 2. WelcomeActivity 单独处理，其他 Activity 手动填充 StatusBar
         * */
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, this.getTheme()));

        setContentView(R.layout.activity_history);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        initLocationDataBase();

        initSearchView();

        initRecordListView();
    }

    @Override
    protected void onDestroy() {
        if (mHistoryLocationDB != null && mHistoryLocationDB.isOpen()) {
            mHistoryLocationDB.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this add items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish(); // back button
            return true;
        } else if (id ==  R.id.action_delete) {
            new AlertDialog.Builder(HistoryActivity.this)
                    .setTitle("警告")//这里是表头的内容
                    .setMessage("确定要删除全部历史记录吗?")//这里是中间显示的具体信息
                    .setPositiveButton("确定",
                            (dialog, which) -> {
                                if (deleteRecord(-1)) {
                                    GoUtils.DisplayToast(this, getResources().getString(R.string.history_delete_ok));
                                    updateRecordList();
                                }
                            })
                    .setNegativeButton("取消",
                            (dialog, which) -> {
                            })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initLocationDataBase() {
        try {
            DataBaseHistoryLocation hisLocDBHelper = new DataBaseHistoryLocation(getApplicationContext());
            mHistoryLocationDB = hisLocDBHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e("HistoryActivity", "ERROR - initLocationDataBase", e);
        }

        recordArchive();
    }

    //sqlite 操作 查询所有记录
    private List<Map<String, Object>> fetchAllRecord() {
        List<Map<String, Object>> data = new ArrayList<>();

        if (mHistoryLocationDB == null || !mHistoryLocationDB.isOpen()) {
            Log.e("HistoryActivity", "ERROR - history database is unavailable");
            return data;
        }

        String[] projection = new String[] {
                DataBaseHistoryLocation.DB_COLUMN_ID,
                DataBaseHistoryLocation.DB_COLUMN_LOCATION,
                DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_WGS84,
                DataBaseHistoryLocation.DB_COLUMN_LATITUDE_WGS84,
                DataBaseHistoryLocation.DB_COLUMN_TIMESTAMP,
                DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_CUSTOM,
                DataBaseHistoryLocation.DB_COLUMN_LATITUDE_CUSTOM
        };

        try (Cursor cursor = mHistoryLocationDB.query(DataBaseHistoryLocation.TABLE_NAME, projection,
                    DataBaseHistoryLocation.DB_COLUMN_ID + " > ?", new String[] {"0"},
                    null, null, DataBaseHistoryLocation.DB_COLUMN_TIMESTAMP + " DESC, "
                            + DataBaseHistoryLocation.DB_COLUMN_ID + " DESC", null)) {

            int idIndex = cursor.getColumnIndexOrThrow(DataBaseHistoryLocation.DB_COLUMN_ID);
            int locationIndex = cursor.getColumnIndexOrThrow(DataBaseHistoryLocation.DB_COLUMN_LOCATION);
            int longitudeIndex = cursor.getColumnIndexOrThrow(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_WGS84);
            int latitudeIndex = cursor.getColumnIndexOrThrow(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_WGS84);
            int timestampIndex = cursor.getColumnIndexOrThrow(DataBaseHistoryLocation.DB_COLUMN_TIMESTAMP);
            int customLongitudeIndex = cursor.getColumnIndexOrThrow(DataBaseHistoryLocation.DB_COLUMN_LONGITUDE_CUSTOM);
            int customLatitudeIndex = cursor.getColumnIndexOrThrow(DataBaseHistoryLocation.DB_COLUMN_LATITUDE_CUSTOM);

            while (cursor.moveToNext()) {
                try {
                    Map<String, Object> item = new HashMap<>();
                    item.put(KEY_ID, String.valueOf(cursor.getLong(idIndex)));
                    item.put(KEY_LOCATION, cursor.isNull(locationIndex) ? "" : cursor.getString(locationIndex));
                    String timestamp = cursor.isNull(timestampIndex)
                            ? "" : String.valueOf(cursor.getLong(timestampIndex));
                    item.put(KEY_TIME, safeFormatTimestamp(timestamp));
                    String longitude = cursor.isNull(longitudeIndex) ? "" : cursor.getString(longitudeIndex);
                    String latitude = cursor.isNull(latitudeIndex) ? "" : cursor.getString(latitudeIndex);
                    String customLongitude = cursor.isNull(customLongitudeIndex)
                            ? "" : cursor.getString(customLongitudeIndex);
                    String customLatitude = cursor.isNull(customLatitudeIndex)
                            ? "" : cursor.getString(customLatitudeIndex);
                    item.put(KEY_LNG_LAT_WGS, "[经度:" + formatCoordinate(longitude)
                            + " 纬度:" + formatCoordinate(latitude) + "]");
                    item.put(KEY_LNG_LAT_CUSTOM, "[经度:" + formatCoordinate(customLongitude)
                            + " 纬度:" + formatCoordinate(customLatitude) + "]");
                    data.add(item);
                } catch (Exception e) {
                    Log.w("HistoryActivity", "Skipping malformed history row", e);
                }
            }
        } catch (Exception e) {
            Log.e("HistoryActivity", "ERROR - fetchAllRecord", e);
        }

        return data;
    }

    private static String formatCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        try {
            return BigDecimal.valueOf(Double.parseDouble(value.trim()))
                    .setScale(11, RoundingMode.HALF_UP).toPlainString();
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static String safeFormatTimestamp(String timestamp) {
        try {
            return GoUtils.timeStamp2Date(timestamp);
        } catch (RuntimeException e) {
            return timestamp == null ? "" : timestamp;
        }
    }

    private void recordArchive() {
        double limits = NumericSettings.getDouble(
                sharedPreferences, NumericSettings.Setting.HISTORY_EXPIRATION);
        final long weekSecond = (long) (limits * 24 * 60 * 60);

        try {
            mHistoryLocationDB.delete(DataBaseHistoryLocation.TABLE_NAME,
                    DataBaseHistoryLocation.DB_COLUMN_TIMESTAMP + " < ?", new String[] {Long.toString(System.currentTimeMillis() / 1000 - weekSecond)});
        } catch (Exception e) {
            Log.e("HistoryActivity", "ERROR - recordArchive");
        }
    }

    private boolean deleteRecord(int ID) {
        boolean deleteRet = true;

        try {
            if (ID <= -1) {
                mHistoryLocationDB.delete(DataBaseHistoryLocation.TABLE_NAME,null, null);
            } else {
                mHistoryLocationDB.delete(DataBaseHistoryLocation.TABLE_NAME,
                        DataBaseHistoryLocation.DB_COLUMN_ID + " = ?", new String[] {Integer.toString(ID)});
            }
        } catch (Exception e) {
            deleteRet = false;
            Log.e("HistoryActivity", "ERROR - deleteRecord");
        }

        return deleteRet;
    }

    private void initSearchView() {
        SearchView mSearchView = findViewById(R.id.searchView);
        mSearchView.onActionViewExpanded();// 当展开无输入内容的时候，没有关闭的图标
        mSearchView.setSubmitButtonEnabled(false);//显示提交按钮
        mSearchView.setFocusable(false);
        mSearchView.clearFocus();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {// 当点击搜索按钮时触发该方法
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {// 当搜索内容改变时触发该方法
                if (TextUtils.isEmpty(newText)) {
                    SimpleAdapter simAdapt = new SimpleAdapter(
                            HistoryActivity.this.getBaseContext(),
                            mAllRecord,
                            R.layout.history_item,
                            new String[]{KEY_ID, KEY_LOCATION, KEY_TIME, KEY_LNG_LAT_WGS, KEY_LNG_LAT_CUSTOM}, // 与下面数组元素要一一对应
                            new int[]{R.id.LocationID, R.id.LocationText, R.id.TimeText, R.id.WGSLatLngText, R.id.BDLatLngText});
                    mRecordListView.setAdapter(simAdapt);
                } else {
                    List<Map<String, Object>> searchRet = new ArrayList<>();
                    for (int i = 0; i < mAllRecord.size(); i++){
                        if (mAllRecord.get(i).toString().toLowerCase(Locale.ROOT)
                                .contains(newText.toLowerCase(Locale.ROOT))){
                            searchRet.add(mAllRecord.get(i));
                        }
                    }
                    if (!searchRet.isEmpty()) {
                        SimpleAdapter simAdapt = new SimpleAdapter(
                                HistoryActivity.this.getBaseContext(),
                                searchRet,
                                R.layout.history_item,
                                new String[]{KEY_ID, KEY_LOCATION, KEY_TIME, KEY_LNG_LAT_WGS, KEY_LNG_LAT_CUSTOM}, // 与下面数组元素要一一对应
                                new int[]{R.id.LocationID, R.id.LocationText, R.id.TimeText, R.id.WGSLatLngText, R.id.BDLatLngText});
                        mRecordListView.setAdapter(simAdapt);
                    } else {
                        GoUtils.DisplayToast(HistoryActivity.this, getResources().getString(R.string.history_error_search));
                        SimpleAdapter simAdapt = new SimpleAdapter(
                                HistoryActivity.this.getBaseContext(),
                                mAllRecord,
                                R.layout.history_item,
                                new String[]{KEY_ID, KEY_LOCATION, KEY_TIME, KEY_LNG_LAT_WGS, KEY_LNG_LAT_CUSTOM}, // 与下面数组元素要一一对应
                                new int[]{R.id.LocationID, R.id.LocationText, R.id.TimeText, R.id.WGSLatLngText, R.id.BDLatLngText});
                        mRecordListView.setAdapter(simAdapt);
                    }
                }

                return false;
            }
        });
    }

    private void showDeleteDialog(String locID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");
        builder.setMessage("确定要删除该项历史记录吗?");
        builder.setPositiveButton("确定", (dialog, whichButton) -> {
            boolean deleteRet = deleteRecord(Integer.parseInt(locID));
            if (deleteRet) {
                GoUtils.DisplayToast(HistoryActivity.this, getResources().getString(R.string.history_delete_ok));
                updateRecordList();
            }
        });
        builder.setNegativeButton("取消", null);

        builder.show();
    }

    private void showInputDialog(String locID, String name) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(name);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("名称");
        builder.setView(input);
        builder.setPositiveButton("确认", (dialog, whichButton) -> {
            String userInput = input.getText().toString();
            DataBaseHistoryLocation.updateHistoryLocation(mHistoryLocationDB, locID, userInput);
            updateRecordList();
        });
        builder.setNegativeButton("取消", null);

        builder.show();
    }

    private String[] randomOffset(String longitude, String latitude) {
        double lon_max_offset = NumericSettings.getDouble(
                sharedPreferences, NumericSettings.Setting.LONGITUDE_OFFSET);
        double lat_max_offset = NumericSettings.getDouble(
                sharedPreferences, NumericSettings.Setting.LATITUDE_OFFSET);
        double lon = Double.parseDouble(longitude);
        double lat = Double.parseDouble(latitude);

        double randomLonOffset = (Math.random() * 2 - 1) * lon_max_offset;  // Longitude offset (meters)
        double randomLatOffset = (Math.random() * 2 - 1) * lat_max_offset;  // Latitude offset (meters)

        lon += randomLonOffset / 111320;    // (meters -> longitude)
        lat += randomLatOffset / 110574;    // (meters -> latitude)

        String offsetMessage = String.format(Locale.US, "经度偏移: %.2f米\n纬度偏移: %.2f米", randomLonOffset, randomLatOffset);
        GoUtils.DisplayToast(this, offsetMessage);

        return new String[]{String.valueOf(lon), String.valueOf(lat)};
    }

    private void initRecordListView() {
        noRecordText = findViewById(R.id.record_no_textview);
        mSearchLayout = findViewById(R.id.search_linear);
        mRecordListView = findViewById(R.id.record_list_view);
        mRecordListView.setOnItemClickListener((adapterView, view, i, l) -> {
            String bd09Longitude;
            String bd09Latitude;
            String name;
            name = (String) ((TextView) view.findViewById(R.id.LocationText)).getText();
            String bd09LatLng = (String) ((TextView) view.findViewById(R.id.BDLatLngText)).getText();
            bd09LatLng = bd09LatLng.substring(bd09LatLng.indexOf('[') + 1, bd09LatLng.indexOf(']'));
            String[] latLngStr = bd09LatLng.split(" ");
            bd09Longitude = latLngStr[0].substring(latLngStr[0].indexOf(':') + 1);
            bd09Latitude = latLngStr[1].substring(latLngStr[1].indexOf(':') + 1);

            // Random offset
            if(sharedPreferences.getBoolean("setting_random_offset", false)) {
                String[] offsetResult = randomOffset(bd09Longitude, bd09Latitude);
                bd09Longitude = offsetResult[0];
                bd09Latitude = offsetResult[1];
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra(RESULT_LOCATION_NAME, name);
            resultIntent.putExtra(RESULT_LOCATION_LONGITUDE, bd09Longitude);
            resultIntent.putExtra(RESULT_LOCATION_LATITUDE, bd09Latitude);
            setResult(RESULT_OK, resultIntent);
            this.finish();
        });

        mRecordListView.setOnItemLongClickListener((parent, view, position, id) -> {
            PopupMenu popupMenu = new PopupMenu(HistoryActivity.this, view);
            popupMenu.setGravity(Gravity.END | Gravity.BOTTOM);
            popupMenu.getMenu().add("编辑");
            popupMenu.getMenu().add("删除");

            popupMenu.setOnMenuItemClickListener(item -> {
                String locID = ((TextView) view.findViewById(R.id.LocationID)).getText().toString();
                String name = ((TextView) view.findViewById(R.id.LocationText)).getText().toString();
                switch (item.getTitle().toString()) {
                    case "编辑":
                        showInputDialog(locID, name);
                        return true;
                    case "删除":
                        showDeleteDialog(locID);
                        return true;
                    default:
                        return false;
                }
            });

            popupMenu.show();
            return true;
        });

        updateRecordList();
    }

    private void updateRecordList() {
        mAllRecord = fetchAllRecord();

        if (mAllRecord.isEmpty()) {
            mRecordListView.setVisibility(View.GONE);
            mSearchLayout.setVisibility(View.GONE);
            noRecordText.setVisibility(View.VISIBLE);
        } else {
            noRecordText.setVisibility(View.GONE);
            mRecordListView.setVisibility(View.VISIBLE);
            mSearchLayout.setVisibility(View.VISIBLE);

            try {
                SimpleAdapter simAdapt = new SimpleAdapter(
                        this,
                        mAllRecord,
                        R.layout.history_item,
                        new String[]{KEY_ID, KEY_LOCATION, KEY_TIME, KEY_LNG_LAT_WGS, KEY_LNG_LAT_CUSTOM},
                        new int[]{R.id.LocationID, R.id.LocationText, R.id.TimeText, R.id.WGSLatLngText, R.id.BDLatLngText});
                mRecordListView.setAdapter(simAdapt);
            } catch (Exception e) {
                Log.e("HistoryActivity", "ERROR - updateRecordList");
            }
        }
    }
}
