package apps.trichain.gtalauncher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import apps.trichain.gtalauncher.databinding.ItemGameBinding;
import apps.trichain.gtalauncher.model.Game;

public class GameListAdapter extends RecyclerView.Adapter<GameListAdapter.ViewHolder> {

    private List<Game> gamesList;
    private Context context;

    public GameListAdapter(List<Game> gamesList, Context context) {
        this.gamesList = gamesList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemGameBinding itemGameBinding = ItemGameBinding.inflate(inflater, parent, false);
        return new ViewHolder(itemGameBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Game game = gamesList.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return gamesList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemGameBinding binding;

        ViewHolder(ItemGameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Game game) {
            binding.setGame(game);
            binding.executePendingBindings(); //Very Important
        }
    }
}
