package com.example.lenovo.movieapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.lenovo.movieapp.data.MovieContract;
import com.example.lenovo.movieapp.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

public class FetchMovieTask extends AsyncTask<String, Void, Void > {


    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    // private GridView mGridView;
    private ProgressBar mProgressBar;
    private final Context mContext;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    long movieId;
    /*   * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
             *
             * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public FetchMovieTask(Context context) {
        mContext = context;
    }

    private void getMoviesPostersFromJson(String forecastJsonStr,String query)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_RESULTLIST = "results";
        final String OWM_POSTER = "poster_path";
        final String OWM_RELEAE_DATE = "release_date";
        final String OWM_OVERVIEW = "overview";
        final String OWM_TITLE = "original_title";
        final String OWM_vote = "vote_average";
        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray MoviesArray = forecastJson.getJSONArray(OWM_RESULTLIST);
            String[] resultStrs = new String[20];
            GridItem item;
            Vector<ContentValues> cVVector = new Vector<ContentValues>(MoviesArray.length());
            for (int i = 0; i < MoviesArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                //long movieId = 0;
                String overview;
                String Poster, newPoster;
                String Release_Date;
                String Original_title;
                Double vote_average;
                String size = "w185";
                String ImageBaseurl = "http://image.tmdb.org/t/p/";
                // Get the JSON object representing the movie
                JSONObject Movie = MoviesArray.getJSONObject(i);
                Poster = Movie.getString(OWM_POSTER);
                newPoster = ImageBaseurl + size + Poster;
                overview = Movie.getString(OWM_OVERVIEW);
                Release_Date = Movie.getString(OWM_RELEAE_DATE);
                Original_title = Movie.getString(OWM_TITLE);
                vote_average = Movie.getDouble(OWM_vote);
                movieId = addMovie(Original_title, vote_average, newPoster, Release_Date, overview);
                // Insert the new movie information into the database
                ContentValues movieValues = new ContentValues();
                movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, Original_title);
                movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
                movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, newPoster);
                movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, Release_Date);
                movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
                cVVector.add(movieValues);
                item = new GridItem();
                item.setTitle(Original_title);
                resultStrs[i] = ImageBaseurl + size + Poster;
                item.setImage(resultStrs[i]);
                mGridData.add(item);

            }
            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted");

//        for (String s : resultStrs) {
//            Log.v(LOG_TAG, "Movies entry: " + s);
//        }
            //  return mGridData;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

   public long addMovie(String Title,Double vote, String Poster, String Date,String overview) {
       // long movieId;

        // First, check if the movie with this Title name exists in the db
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID},
                MovieContract.MovieEntry.COLUMN_TITLE ,
                null,
                null);

        if (movieCursor.moveToFirst()) {
            int movieIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry._ID);
            movieId = movieCursor.getLong(movieIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues movieValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, Title);
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote);
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER, Poster);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, Date);
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);



            // Finally, insert movie data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieValues
            );

            // The resulting URI contains the ID for the row.  Extract the movieId from the Uri.
            movieId = ContentUris.parseId(insertedUri);
        }

        movieCursor.close();
        // Wait, that worked?  Yes!
        return movieId;
    }
    @Override
    protected Void  doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            final String FORECAST_BASE_URL ="http://api.themoviedb.org/3/discover/movie?&api_key=097013c19c88173758516d9e57b656b2";
            final String QUERY_PARAM =  params[0];

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0]).build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());
            // Construct the URL for the themoviedb query
            // Possible parameters are avaiable at OWM's movies API page, at
            // http://themoviedb.org/API#movies
            // URL url = new URL("https://www.themoviedb.org//discover/movie?sort_by=popularity.desc&api_key=56769021c3a368167a005c31");

            // Create the request to themoviedb.org, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            getMoviesPostersFromJson(forecastJsonStr, QUERY_PARAM);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return null;
    }
}

//    @Override
//    protected void onPostExecute(ArrayList<GridItem>  result) {
//        //   mGridAdapter.clear();
//        if (result != null) {
//            // mGridAdapter.clear();
//            for(GridItem item : result) {
//                mGridAdapter.setGridData(result);
//            }
//            // New data is back from the server.  Hooray!
//        }
//        //Hide progressbar
//        mProgressBar.setVisibility(View.GONE);
//
//    }
//}