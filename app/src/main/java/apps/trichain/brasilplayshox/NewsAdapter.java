package apps.trichain.brasilplayshox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import apps.trichain.brasilplayshox.databinding.ItemNewsBinding;
import apps.trichain.brasilplayshox.model.NewsModel;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsModel> newsModelList;
    private Context context;
    private static final String TAG = "NewsAdapter";

    public NewsAdapter(List<NewsModel> newsModelList, Context context) {
        this.newsModelList = newsModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemNewsBinding itemGameBinding = ItemNewsBinding.inflate(inflater, parent, false);
        return new ViewHolder(itemGameBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsModel newsModel = newsModelList.get(position);
        holder.bind(newsModel);
        holder.binding.llLaunchGame.setOnClickListener(v -> {
            try {
                Log.d(TAG, "onBindViewHolder: url 1: " + newsModel.getUrl());
                String url = !newsModel.getUrl().startsWith("https://") ? "https://" + newsModel.getUrl() : newsModel.getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
                Log.d(TAG, "onBindViewHolder: url 2: " + url);
            } catch (Exception mue) {
                mue.printStackTrace();
                Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsModelList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemNewsBinding binding;

        ViewHolder(ItemNewsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NewsModel newsModel) {
            binding.setNewsItem(newsModel);
            binding.executePendingBindings(); //Very very Important
        }
    }
}
