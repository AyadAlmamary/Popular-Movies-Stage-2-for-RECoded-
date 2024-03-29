package com.example.popularmoviess2recoded.utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.popularmoviess2recoded.model.MoviesClass;
import com.example.popularmoviess2recoded.model.ReviewClass;
import com.example.popularmoviess2recoded.model.TrailerClass;


import java.util.ArrayList;

public class JsonUtils {
    private static final String TAG = JsonUtils.class.getSimpleName();

    public static ArrayList<MoviesClass> parseMoviesJson(String json) {
        try {

            MoviesClass movie;
            JSONObject json_object = new JSONObject(json); //(json.replace("\\",""));

            JSONArray resultsArray = new JSONArray(json_object.optString("results","[\"\"]"));

            ArrayList<MoviesClass> movieitems = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {
                String thisitem = resultsArray.optString(i, "");
                JSONObject movieJson = new JSONObject(thisitem);

                movie = new MoviesClass(
                        movieJson.optString("id","Not Available"),
                        movieJson.optString("original_title","Not Available"),
                        movieJson.optString("release_date","Not Available"),
                        movieJson.optString("vote_average","Not Available"),
                        movieJson.optString("popularity","Not Available"),
                        movieJson.optString("overview","Not Available"),
                        movieJson.optString("poster_path","Not Available"),
                        movieJson.optString("backdrop_path","Not Available")
                );

                movieitems.add(movie);
            }
            return movieitems;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static ArrayList<ReviewClass> parseReviewsJson(String json) {
        try {

            ReviewClass review;
            JSONObject json_object = new JSONObject(json); //(json.replace("\\",""));

            JSONArray resultsArray = new JSONArray(json_object.optString("results","[\"\"]"));

            ArrayList<ReviewClass> reviewitems = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {
                String thisitem = resultsArray.optString(i, "");
                JSONObject movieJson = new JSONObject(thisitem);

                review = new ReviewClass(
                        movieJson.optString("author","Not Available"),
                        movieJson.optString("content","Not Available"),
                        movieJson.optString("id","Not Available"),
                        movieJson.optString("url","Not Available")
                );

                reviewitems.add(review);
            }
            return reviewitems;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static ArrayList<TrailerClass> parseTrailersJson(String json) {
        try {
            TrailerClass trailer;
            JSONObject json_object = new JSONObject(json); //(json.replace("\\",""));
            JSONArray resultsArray = new JSONArray(json_object.optString("results","[\"\"]"));

            ArrayList<TrailerClass> traileritems = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {
                String thisitem = resultsArray.optString(i, "");
                JSONObject movieJson = new JSONObject(thisitem);

                trailer = new TrailerClass(
                        movieJson.optString("name","Not Available"),
                        movieJson.optString("site","Not Available"),
                        movieJson.optString("key","Not Available")
                );

                traileritems.add(trailer);
            }
            return traileritems;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
