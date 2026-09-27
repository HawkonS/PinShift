package tech.hawkon.pinshift.utils;

import android.content.Context;
import android.content.Intent;

public class ShareUtils {
    public static void shareText(Context context, String title, String text) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        share.putExtra(Intent.EXTRA_SUBJECT, title);
        context.startActivity(Intent.createChooser(share, title));
    }
}

