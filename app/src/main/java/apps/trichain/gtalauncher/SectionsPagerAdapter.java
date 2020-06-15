package apps.trichain.gtalauncher;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> settingsSubFragmentsList;

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.one};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, List<Fragment> settingsSubFragmentsList, FragmentManager fm) {
        super(fm);
        mContext = context;
        this.settingsSubFragmentsList = settingsSubFragmentsList;
    }

    @Override
    public Fragment getItem(int position) {
        return settingsSubFragmentsList.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}