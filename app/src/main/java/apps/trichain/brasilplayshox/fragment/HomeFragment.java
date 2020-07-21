package apps.trichain.brasilplayshox.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import apps.trichain.brasilplayshox.NewsAdapter;
import apps.trichain.brasilplayshox.R;
import apps.trichain.brasilplayshox.databinding.FragmentHomeBinding;
import apps.trichain.brasilplayshox.model.NewsModel;
import apps.trichain.brasilplayshox.util.SharedPrefsManager;

public class HomeFragment extends Fragment {


    private FragmentHomeBinding b;
    private Context c;
    SharedPrefsManager sharedPrefsManager;
    private ValueEventListener newsEventListener;
    DatabaseReference dbReference;
    ArrayList<NewsModel> newsList = new ArrayList<>();
    NewsAdapter adapter;

    private static final String TAG = "HomeFragment";

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        c = getContext();
        sharedPrefsManager = SharedPrefsManager.getInstance(getContext());

        dbReference = FirebaseDatabase.getInstance().getReference();

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

        b.recyclerViewNews.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NewsAdapter(newsList, getContext());
        b.recyclerViewNews.setAdapter(adapter);

        downloadNewsItems();
        return b.getRoot();
    }

    private void downloadNewsItems() {
        newsEventListener = dbReference.child("news").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange: data snapshot: " + dataSnapshot.toString());
                newsList.clear();
                for (DataSnapshot newsSnapShot : dataSnapshot.getChildren()) {
                    NewsModel childModel = newsSnapShot.getValue(NewsModel.class);
                    Log.e(TAG, "onDataChange: news item: " + childModel.getNewsID());
                    if (null != childModel && childModel.getNewsID().length() > 0) {
                        newsList.add(childModel);
                    }
                }

                adapter.notifyDataSetChanged();

                if (newsList.size() <= 0) {
                    Snackbar.make(b.homeToolBar, "No news yet", BaseTransientBottomBar.LENGTH_INDEFINITE)
                            .setAction("Okay", v -> {
                                Log.i(TAG, "onDataChange: Snackbar dismissed");
                            }).show();
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

