package apps.trichain.gtalauncher.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.util.Locale;

public class util {
    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void hideView(View v) {
        if (v.getVisibility() == View.VISIBLE) {
            v.animate()
                    .alpha(0f)
                    .setDuration(350)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public static void showView(View v) {
        if (v.getVisibility() == View.GONE || v.getVisibility() == View.INVISIBLE) {
            v.setAlpha(0f);
            v.setVisibility(View.VISIBLE);
            v.animate()
                    .alpha(1f)
                    .setDuration(350)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            v.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    public static File getDownloadedPath() {
        File downloaded_path = new File(Environment.getExternalStorageDirectory() + "/Brasil Play Shox");
        if (!downloaded_path.exists()) {
            downloaded_path.mkdirs();
        }
        return downloaded_path;
    }

    public static File getOBBDir() {
        File obb_target = new File(Environment.getExternalStorageDirectory() + "/Android/obb/");
        if (!obb_target.exists()) {
            obb_target.mkdirs();
        }
        return obb_target;
    }

    public static File getDataDir() {
        File data_target = new File(Environment.getExternalStorageDirectory() + "/Android/data/");
        if (!data_target.exists()) {
            data_target.mkdirs();
        }
        return data_target;
    }

    public static String humanify(long bytes) {
        long n = 1000;
        String s = "";
        double kb = bytes / n;
        double mb = kb / n;
        double gb = mb / n;
        double tb = gb / n;
        if (bytes < n) {
            s = bytes + " Bytes";
        } else if (bytes >= n && bytes < (n * n)) {
            s = String.format(Locale.US, "%.2f", kb) + " KB";
        } else if (bytes >= (n * n) && bytes < (n * n * n)) {
            s = String.format(Locale.US, "%.2f", mb) + " MB";
        } else if (bytes >= (n * n * n) && bytes < (n * n * n * n)) {
            s = String.format(Locale.US, "%.2f", gb) + " GB";
        } else if (bytes >= (n * n * n * n)) {
            s = String.format(Locale.US, "%.2f", tb) + " TB";
        }
        return s;
    }
}
