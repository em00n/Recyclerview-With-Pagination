package com.emon.recyclerviewwithpagination.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.emon.recyclerviewwithpagination.R;
import com.emon.recyclerviewwithpagination.model.Result;

import java.util.List;


public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w500";
    private boolean isLoaderVisible = false;
    private List<Result> resultList;
    Context context;

    public MovieAdapter(Context context, List<Result> resultList) {
        this.resultList = resultList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof ViewHolder)) {
            return;
        }
        ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.titleTV.setText(resultList.get(position).getTitle());
        viewHolder.descriptionTV.setText(resultList.get(position).getOverview());
        Glide.with(context.getApplicationContext()).asBitmap().placeholder(R.drawable.ic_launcher_foreground).load(BASE_URL_IMG+resultList.get(position).getPosterPath()).into(((ViewHolder) holder).posterIV);

    }


    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == resultList.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }

    public void addItems(List<Result> postItems) {
        resultList.addAll(postItems);
        notifyDataSetChanged();
    }

    public void addLoading() {
        isLoaderVisible = true;
        resultList.add(new Result());
        notifyItemInserted(resultList.size() - 1);
    }

    public void removeLoading() {
        isLoaderVisible = false;
        int position = resultList.size() - 1;
        Result model = getItem(position);
        if (model != null) {
            resultList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        resultList.clear();
        notifyDataSetChanged();
    }

    Result getItem(int position) {
        return resultList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTV;
        TextView descriptionTV;
        ImageView posterIV;

        ViewHolder(View itemView) {
            super(itemView);
           titleTV= itemView.findViewById(R.id.titleTV);
            descriptionTV=itemView.findViewById(R.id.descriptionTV);
            posterIV=itemView.findViewById(R.id.posterIV);

        }
    }

    public static class ProgressHolder extends RecyclerView.ViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);

        }

    }
}