package apps.trichain.gtalauncher.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private SharedPreferences sharedPreferences;
    private SharedPrefsManager instance;

    private SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences("gta_launcher", Context.MODE_PRIVATE);
    }

    public synchronized SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager(context);
        }
        return instance;
    }
}
