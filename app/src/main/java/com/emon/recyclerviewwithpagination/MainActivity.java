package com.emon.recyclerviewwithpagination;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.emon.recyclerviewwithpagination.adapter.MovieAdapter;
import com.emon.recyclerviewwithpagination.model.Result;
import com.emon.recyclerviewwithpagination.model.TopRatedMovies;
import com.emon.recyclerviewwithpagination.retrofit.ApiService;
import com.emon.recyclerviewwithpagination.retrofit.ApiClient;
import com.emon.recyclerviewwithpagination.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.emon.recyclerviewwithpagination.PaginationListener.PAGE_START;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

    Context context;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout swipeRefresh;
    SearchView searchView;
    Toolbar toolbar;
    TextView titleTV;
    List<Result> movieList;
    private MovieAdapter adapter;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 10;
    private boolean isLoading = false;

    int itemCount = 0;

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        movieList = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        toolbar = findViewById(R.id.toolbar);
        titleTV = findViewById(R.id.toolbartitleTV);
        searchView = findViewById(R.id.searchview);

        setSupportActionBar(toolbar);

        swipeRefresh.setOnRefreshListener(this);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new MovieAdapter(context, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);

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


        titleTV.setText(getString(R.string.app_name));
        searchView.setQueryHint("Search Here");
        searchView.setOnQueryTextListener(this);
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText editText = (EditText) searchView.findViewById(id);
        editText.setTextColor(Color.WHITE);
        editText.setHintTextColor(Color.WHITE);
        if (searchView.isIconified()) titleTV.setVisibility(View.GONE);
    }

    /**
     * do api call here to fetch data from server
     * In example i'm adding data manually
     */


    private void loadFirstPage() {


        apiService.getTopRatedMovies(Utils.apiKey, Utils.language, currentPage).enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                // Got data. Send it to adapter

                List<Result> results = fetchResults(response);
                // progressBar.setVisibility(View.GONE);
                adapter.addItems(results);
                movieList.addAll(results);

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

        apiService.getTopRatedMovies(Utils.apiKey, Utils.language, currentPage).enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                adapter.removeLoading();
                isLoading = false;

                List<Result> results = fetchResults(response);
                adapter.addItems(results);
                // movieList.addAll(results);

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

    //search
    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.equals("")) {
            newText = newText.toLowerCase();
            List<Result> resultArrayList = new ArrayList<>();

            for (Result result : movieList) {
                String movieName = result.getTitle().toLowerCase();
                if (movieName.contains(newText)) {
                    resultArrayList.add(result);
                    Log.d("BAL", result.getTitle());
                    adapter.addLoading();
                    adapter.removeLoading();
                    //  isSearching=true;
                }
            }
            adapter.setFilter(resultArrayList);
            //           adapter.removeLoading();
        } else {
            adapter.setFilter(movieList);
        }

        return true;
    }

}