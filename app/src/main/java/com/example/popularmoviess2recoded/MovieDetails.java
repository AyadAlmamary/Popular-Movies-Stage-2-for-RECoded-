package com.example.popularmoviess2recoded;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import com.example.popularmoviess2recoded.database.FavoriteMovie;
import com.example.popularmoviess2recoded.database.MovieDatabase;
import com.example.popularmoviess2recoded.model.MoviesClass;
import com.example.popularmoviess2recoded.model.ReviewClass;
import com.example.popularmoviess2recoded.model.TrailerClass;
import com.example.popularmoviess2recoded.utilities.JsonUtils;
import com.example.popularmoviess2recoded.utilities.NetworkUtils;



import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MovieDetails extends AppCompatActivity implements TrailerAdapter.ListItemClickListener{
    private static final String TAG = MovieDetails.class.getSimpleName();
    private MoviesClass movieItem;
    private ArrayList<ReviewClass> reviewList;
    private ArrayList<TrailerClass> trailerList;

    //trailers
    private RecyclerView mTrailerRecyclerView;
    private TrailerAdapter mTrailerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Favorite movies
    private MovieDatabase mDb;
    private ImageView mFavButton;
    private Boolean isFav = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError("Intent is null");
        }

        movieItem = (MoviesClass) intent.getSerializableExtra("movieItem");
        if (movieItem == null) {
            closeOnError(getString(R.string.Error_NoMovieData));
            return;
        }

        // Recycler view for trailers
        mTrailerRecyclerView = findViewById(R.id.rv_trailers);
        mTrailerAdapter = new TrailerAdapter(this, trailerList, this);
        mTrailerRecyclerView.setAdapter(mTrailerAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        mTrailerRecyclerView.setLayoutManager(mLayoutManager);

        // favorite movies
        mFavButton = findViewById(R.id.iv_favButton);
        mDb = MovieDatabase.getInstance(getApplicationContext());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final FavoriteMovie fmov = mDb.movieDao().loadMovieById(Integer.parseInt(movieItem.getId()));
                setFavorite((fmov != null)? true : false);
            }
        });

        // Fetch data from the Web API
        getMoreDetails(movieItem.getId());

    }

    private void setFavorite(Boolean fav){
        if (fav) {
            isFav = true;
            mFavButton.setImageResource(R.drawable.ic_fav_true);
        } else {
            isFav = false;
            mFavButton.setImageResource(R.drawable.ic_fav_24dp);
        }
    }

    private static class SearchURLs {
        URL reviewSearchUrl;
        URL trailerSearchUrl;
        SearchURLs(URL reviewSearchUrl, URL trailerSearchUrl){
            this.reviewSearchUrl = reviewSearchUrl;
            this.trailerSearchUrl = trailerSearchUrl;
        }
    }
    private static class ResultsStrings {
        String reviewString;
        String trailerString;
        ResultsStrings(String reviewString, String trailerString){
            this.reviewString = reviewString;
            this.trailerString = trailerString;
        }
    }
    private void getMoreDetails(String id) {
        String reviewQuery = id + File.separator + "reviews";
        String trailerQuery = id + File.separator + "videos";
        SearchURLs searchURLs = new SearchURLs(
                NetworkUtils.buildUrl(reviewQuery, getText(R.string.api_key).toString()),
                NetworkUtils.buildUrl(trailerQuery, getText(R.string.api_key).toString())
        );
        new ReviewsQueryTask().execute(searchURLs);
    }


    // AsyncTask to perform query
    public class ReviewsQueryTask extends AsyncTask<SearchURLs, Void, ResultsStrings> {
        @Override
        protected ResultsStrings doInBackground(SearchURLs... params) {
            URL reviewsearchUrl = params[0].reviewSearchUrl;
            URL trailersearchUrl = params[0].trailerSearchUrl;

            String reviewResults = null;
            try {
                reviewResults = NetworkUtils.getResponseFromHttpUrl(reviewsearchUrl);
                reviewList = JsonUtils.parseReviewsJson(reviewResults);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String trailerResults = null;
            try {
                trailerResults = NetworkUtils.getResponseFromHttpUrl(trailersearchUrl);
                trailerList = JsonUtils.parseTrailersJson(trailerResults);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ResultsStrings results = new ResultsStrings(reviewResults,trailerResults);

            return results;
        }

        @Override
        protected void onPostExecute(ResultsStrings results) {
            String searchResults = results.reviewString;
            if (searchResults != null && !searchResults.equals("")) {
                reviewList = JsonUtils.parseReviewsJson(searchResults);
                populateDetails();
            }
        }
    }

    private void watchYoutubeVideo(String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        webIntent.putExtra("finish_on_ended", true);
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    @Override
    public void OnListItemClick(TrailerClass trailerItem) {
        Log.d(TAG,trailerItem.getKey());
        watchYoutubeVideo(trailerItem.getKey());
    }

    private void populateDetails() {

        ((TextView)findViewById(R.id.tv_title)).setText(movieItem.getTitle());
        ((TextView)findViewById(R.id.tv_header_rating)).append(" ("+movieItem.getVote()+"/10)");
        ((RatingBar)findViewById(R.id.rbv_user_rating)).setRating(Float.parseFloat(movieItem.getVote()));
        ((TextView)findViewById(R.id.tv_release_date)).setText(movieItem.getReleaseDate());
        ((TextView)findViewById(R.id.tv_synopsis)).setText(movieItem.getSynopsis());

        // Favorite
        mFavButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {
                final FavoriteMovie mov = new FavoriteMovie(
                        Integer.parseInt(movieItem.getId()),
                        movieItem.getTitle(),
                        movieItem.getReleaseDate(),
                        movieItem.getVote(),
                        movieItem.getPopularity(),
                        movieItem.getSynopsis(),
                        movieItem.getImage(),
                        movieItem.getBackdrop()
                );
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isFav) {
                            // delete item
                            mDb.movieDao().deleteMovie(mov);
                        } else {
                            // insert item
                            mDb.movieDao().insertMovie(mov);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setFavorite(!isFav);
                            }
                        });
                    }

                });
            }
        });



        // Trailers
        mTrailerAdapter.setTrailerData(trailerList);

        // Reviews
        ((TextView)findViewById(R.id.tv_reviews)).setText("");
        for(int i=0; i<reviewList.size(); i++) {
            ((TextView)findViewById(R.id.tv_reviews)).append("\n");
            ((TextView)findViewById(R.id.tv_reviews)).append(reviewList.get(i).getContent());
            ((TextView)findViewById(R.id.tv_reviews)).append("\n\n");
            ((TextView)findViewById(R.id.tv_reviews)).append(" - Reviewed by ");
            ((TextView)findViewById(R.id.tv_reviews)).append(reviewList.get(i).getAuthor());
            ((TextView)findViewById(R.id.tv_reviews)).append("\n\n--------------\n");
        }

        String backdropPathURL = NetworkUtils.buildPosterUrl(movieItem.getBackdrop());

        try {
            Picasso.with(this)
                    .load(backdropPathURL)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into((ImageView)this.findViewById(R.id.iv_backdrop));
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        String imagePathURL = NetworkUtils.buildPosterUrl(movieItem.getImage());

        try {
            Picasso.with(this)
                    .load(imagePathURL)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into((ImageView)this.findViewById(R.id.iv_image));
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

    }

    private void closeOnError(String msg) {
        finish();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
