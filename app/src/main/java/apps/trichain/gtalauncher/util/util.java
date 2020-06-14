package apps.trichain.gtalauncher.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

public class util {

    private static final String TAG = "util";
    public static final String GTA_SA_PACKAGE_NAME = "com.rockstargames.gtasa";
    public static final String DATA_FILE = "GTA_DATA_ZIP.zip";
    public static final String OBB_FILE = "GTA_DATA_ZIP.zip";
    public static final String APK_FILE = "GTA_APK.apk";
    public static final String DATA_FILE_PATH = "/" + DATA_FILE;
    public static final String OBB_FILE_PATH = "/" + OBB_FILE;
    public static final String ANDROID_DATA_DIR = "/Android/data/";
    public static final String ANDROID_OBB_DIR = "/Android/obb/";
    public static final String NICK_NAME_FILE_PATH = ANDROID_DATA_DIR + GTA_SA_PACKAGE_NAME + "/files/NickName.ini";
    public static final String BRASIL_PLAY_SHOX_DIR = "/Brasil Play Shox";
    private static SharedPrefsManager sharedPrefsManager;

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void hideView(View v, boolean withAnimation) {
        if (v.getVisibility() == View.VISIBLE) {
            if (withAnimation)
                v.animate()
                        .alpha(0f)
                        .setDuration(350)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                v.setVisibility(View.GONE);
                            }
                        });
            else v.setVisibility(View.GONE);
        }
    }

    public static void showView(View v, boolean withAnimation) {
        if (v.getVisibility() == View.GONE || v.getVisibility() == View.INVISIBLE) {
            if (withAnimation) {
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
            } else {
                v.setVisibility(View.VISIBLE);
            }
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

    @SuppressLint("StaticFieldLeak")
    public static void saveNickName(Context context, String nickName) {
        sharedPrefsManager = SharedPrefsManager.getInstance(context);
        File absFile = Environment.getExternalStorageDirectory();
        File fileNickName = new File(absFile, NICK_NAME_FILE_PATH);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    if (!fileNickName.exists()) {
                        Log.e(TAG, "saveNickName: User has not downloaded files, creating empty directory");
                        fileNickName.getParentFile().getParentFile().mkdirs();
                        fileNickName.createNewFile();
                    }
                    byte[] bytes = nickName.getBytes();
                    FileOutputStream oS = new FileOutputStream(fileNickName);
                    oS.write(bytes);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                sharedPrefsManager.setNickName(nickName);
                Toast.makeText(context, "Apelido atualizado", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}
