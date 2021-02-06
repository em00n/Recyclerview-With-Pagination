package com.emon.recyclerviewwithpagination;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.emon.recyclerviewwithpagination.adapter.MovieAdapter;
import com.emon.recyclerviewwithpagination.model.Result;
import com.emon.recyclerviewwithpagination.model.TopRatedMovies;
import com.emon.recyclerviewwithpagination.retrofit.ApiInterface;
import com.emon.recyclerviewwithpagination.retrofit.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emon.recyclerviewwithpagination.PaginationListener.PAGE_START;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

Context context;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout swipeRefresh;
    private MovieAdapter adapter;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;
    int itemCount = 0;

    ApiInterface apiInterface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;

        mRecyclerView=findViewById(R.id.recyclerView);
        swipeRefresh=findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(this);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new MovieAdapter(context,new ArrayList<>());
        mRecyclerView.setAdapter(adapter);

        apiInterface= ApiService.getClient().create(ApiInterface.class);

        loadFirstPage();


         // add scroll listener while user reach in bottom load more will call

        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                loadNextPage();
            }
            @Override
            public boolean isLastPage() {
                return isLastPage;
            }
            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });


    }

    /**
     * do api call here to fetch data from server
     * In example i'm adding data manually
     */


    private void loadFirstPage() {


        apiInterface.getTopRatedMovies("ec01f8c2eb6ac402f2ca026dc2d9b8fd","en_US", currentPage).enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                // Got data. Send it to adapter

                List<Result> results = fetchResults(response);
               // progressBar.setVisibility(View.GONE);
                adapter.addItems(results);

                if (currentPage <= totalPage) adapter.addLoading();
                else isLastPage = true;

                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();

            }
        });

    }


    private List<Result> fetchResults(Response<TopRatedMovies> response) {
        TopRatedMovies topRatedMovies = response.body();
        return topRatedMovies.getResults();
    }

    private void loadNextPage() {

        apiInterface.getTopRatedMovies("ec01f8c2eb6ac402f2ca026dc2d9b8fd","en_US", currentPage).enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                adapter.removeLoading();
                isLoading = false;

                List<Result> results = fetchResults(response);
                adapter.addItems(results);

                if (currentPage != totalPage) adapter.addLoading();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();

            }
        });
    }

    @Override
    public void onRefresh() {
        itemCount = 0;
        currentPage = PAGE_START;
        isLastPage = false;
        adapter.clear();
        loadFirstPage();
    }
}