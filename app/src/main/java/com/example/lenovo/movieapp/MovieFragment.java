package com.example.lenovo.movieapp;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.example.lenovo.movieapp.data.MovieContract;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private GridView mGridView;
    private ProgressBar mProgressBar;

    private MovieAdapter mMovieAdapter;
    private ArrayList<GridItem> mGridData;

    public MovieFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String MovieSetting = Utility.getPreferredLocation(getActivity());

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridview = (GridView) rootView.findViewById(R.id.GridView);

        mGridView = (GridView) rootView.findViewById(R.id.GridView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        //Initialize with empty data
        mGridData = new ArrayList<>();
        // Sort order:  Ascending, by date.
        String sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_POSTER;
        Uri movieUri = MovieContract.MovieEntry.buildMovieUri(1);

        Cursor cur = getActivity().getContentResolver().query(movieUri,
                null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mMovieAdapter = new MovieAdapter(getActivity(), cur, 0);
      //  mGridAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mMovieAdapter);
        mProgressBar.setVisibility(View.VISIBLE);
        return rootView;
    }

    private void updateWeather() {
//        FetchMovieTask MoviesTask = new FetchMovieTask(getActivity(), mMovieAdapter,mGridData,mProgressBar);
//        SharedPreferences sharedPrefs =
//                PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String Options = sharedPrefs.getString(
//                getString(R.string.pref_units_key),
//                getString(R.string.pref_units_label_Popular));
        FetchMovieTask movieTask = new FetchMovieTask(getActivity());
           String Options = Utility.getPreferredLocation(getActivity());
        movieTask.execute(Options);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }
}