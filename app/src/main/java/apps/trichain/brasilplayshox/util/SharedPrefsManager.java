package apps.trichain.brasilplayshox.util;

import android.content.Context;
import android.content.SharedPreferences;

import apps.trichain.brasilplayshox.model.Links;

public class SharedPrefsManager {
    private static SharedPreferences sharedPreferences;
    private static SharedPrefsManager instance;

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences("gta_launcher", Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context);
        }
        return instance;
    }

    public void setIsFirstTimeLaunch(boolean isFirstTimeLaunch) {
        sharedPreferences.edit().putBoolean("is_first_time_launch", isFirstTimeLaunch).apply();
    }

    public boolean isFirstTimeLaunch() {
        return sharedPreferences.getBoolean("is_first_time_launch", true);
    }

    public void updateLinks(Links links) {
        sharedPreferences.edit().putString("links", links.serialize()).apply();
    }

    public String getLinks() {
        return sharedPreferences.getString("links", null);
    }

    public void setIsDataFileDownloaded(boolean isDataFileDownloaded) {
        sharedPreferences.edit().putBoolean("is_data_file_downloaded", isDataFileDownloaded).apply();
    }

    public boolean checkIsDataFileDownloaded() {
        return sharedPreferences.getBoolean("is_data_file_downloaded", false);
    }

    public void setIsOBBFileDownloaded(boolean isOBBFileDownloaded) {
        sharedPreferences.edit().putBoolean("is_obb_file_downloaded", isOBBFileDownloaded).apply();
    }

    public boolean checkIsOBBFileDownloaded() {
        return sharedPreferences.getBoolean("is_obb_file_downloaded", false);
    }

    public void setHasDownloadedAllFiles(boolean b) {
        sharedPreferences.edit().putBoolean("has_downloaded_files", b).apply();
    }

    public boolean checkHasDownloadedAllFiles() {
        return sharedPreferences.getBoolean("has_downloaded_files", false);
    }

    public void saveUpdateDate(String date) {
        sharedPreferences.edit().putString("last_updated", date).apply();
    }

    public String getLastUpdatedDate() {
        return sharedPreferences.getString("last_updated", "");
    }

    public void setNickName(String nickName) {
        sharedPreferences.edit().putString("nick_name", nickName).apply();
    }

    public String getNickName() {
        return sharedPreferences.getString("nick_name", null);
    }
}
