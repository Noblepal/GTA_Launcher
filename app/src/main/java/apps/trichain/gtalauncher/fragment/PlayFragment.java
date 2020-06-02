package apps.trichain.gtalauncher.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import apps.trichain.gtalauncher.GameListAdapter;
import apps.trichain.gtalauncher.R;
import apps.trichain.gtalauncher.databinding.FragmentPlayBinding;
import apps.trichain.gtalauncher.model.Game;

public class PlayFragment extends Fragment {

    private FragmentPlayBinding b;
    private List<Game> gamesList = new ArrayList<>();
    private GameListAdapter gamesAdapter;

    private FirebaseDatabase database;
    private DatabaseReference dbReference;
    private ValueEventListener gamesListener;
    private static final String TAG = "PlayFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_play, container, false);

        database = FirebaseDatabase.getInstance();
        dbReference = database.getReference();

        setUpRecyclerView();

        return b.getRoot();
    }

    private void setUpRecyclerView() {
        gamesAdapter = new GameListAdapter(gamesList, getContext());
        b.recyclerViewGames.setLayoutManager(new LinearLayoutManager(getContext()));
        b.recyclerViewGames.setAdapter(gamesAdapter);

        downloadGames();

    }

    private void downloadGames() {
        b.pbGamesLoading.setVisibility(View.VISIBLE);
        gamesListener = dbReference.child("games").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gamesList.clear();
                for (DataSnapshot gameSnapShot : dataSnapshot.getChildren()) {
                    gamesList.add(gameSnapShot.getValue(Game.class));
                }
                gamesAdapter.notifyDataSetChanged();
                b.pbGamesLoading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                b.pbGamesLoading.setVisibility(View.GONE);
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            dbReference.removeEventListener(gamesListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
