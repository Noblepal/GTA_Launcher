package apps.trichain.gtalauncher.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import apps.trichain.gtalauncher.R;
import apps.trichain.gtalauncher.SectionsPagerAdapter;

public class SettingsFragment extends Fragment {

    private List<Fragment> settingsSubFragmentsList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        //loadFragments();

        /*SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getActivity(), settingsSubFragmentsList, getChildFragmentManager());
        ViewPager viewPager = root.findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = root.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);*/

        return root;
    }

    private void loadFragments() {
        settingsSubFragmentsList.addAll(Arrays.asList(
                FirstFragment.newInstance(0),
                SecondFragment.newInstance(1)
        ));
    }
}
