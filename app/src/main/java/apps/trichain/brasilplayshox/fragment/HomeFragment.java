package apps.trichain.brasilplayshox.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import apps.trichain.brasilplayshox.R;
import apps.trichain.brasilplayshox.databinding.FragmentHomeBinding;
import apps.trichain.brasilplayshox.util.SharedPrefsManager;

public class HomeFragment extends Fragment {


    private FragmentHomeBinding b;
    private Context c;
    SharedPrefsManager sharedPrefsManager;

    private static final String TAG = "HomeFragment";

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        c = getContext();
        sharedPrefsManager = SharedPrefsManager.getInstance(getContext());

        if (sharedPrefsManager.checkHasDownloadedAllFiles()) {
            String dte = "Last updated: " + sharedPrefsManager.getLastUpdatedDate();
            b.tvMDate.setText(dte);
            b.llLaunchGame.setOnClickListener(v -> {
                try {
                    Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.rockstargames.gtasa");
                    startActivity(launchIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(c, R.string.gta_not_installed_toast, Toast.LENGTH_SHORT).show();
                }
            });

        } else {
        }

        return b.getRoot();
    }
}
