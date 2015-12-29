/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lenovo.movieapp.data;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.example.lenovo.movieapp.FetchMovieTask;

public class TestFetchMovieTask extends AndroidTestCase{
    static final String ADD_MOVIE_SETTING = "Ant-Man";
    /*
        Students: uncomment testAddMOVIE after you have written the AddMOVIE function.
        This test will only run on API level 11 and higher because of a requirement in the
        content provider.
     */
    @TargetApi(11)
    public void testAddMovie() {
        // start from a clean state
        getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_TITLE+ " = ?",
                new String[]{ADD_MOVIE_SETTING});

        FetchMovieTask fwt = new FetchMovieTask(getContext(), null,null,null);
        long movieId = fwt.addMovie(ADD_MOVIE_SETTING);

        // does addmovie return a valid record ID?
        assertFalse("Error: addmovie returned an invalid ID on insert",
                movieId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our movie?
            Cursor movieCursor = getContext().getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI,
                    new String[]{
                            MovieContract.MovieEntry.COLUMN_TITLE,
                            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                            MovieContract.MovieEntry.COLUMN_MOVIE_POSTER,
                            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
                            MovieContract.MovieEntry.COLUMN_OVERVIEW

            },
                    MovieContract.MovieEntry.COLUMN_TITLE + " = ?",
                    new String[]{ADD_MOVIE_SETTING},
                    null);

            // these match the indices of the projection
            if (movieCursor.moveToFirst()) {
                assertEquals("Error: the queried value of movie setting is incorrect",
                        movieCursor.getString(0), "Ant-Man");
                assertEquals("Error: the queried value of movie city is incorrect",
                        movieCursor.getDouble(1), 20.3);
                assertEquals("Error: the queried value of latitude is incorrect",
                        movieCursor.getString(2), "kkk");
                assertEquals("Error: the queried value of longitude is incorrect",
                        movieCursor.getString(3),"10-10-2010" );
                assertEquals("Error: the queried value of longitude is incorrect",
                        movieCursor.getString(4), "Hi");
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a movie query",
                    movieCursor.moveToNext());

            // add the movie again
            long newmovieId = fwt.addMovie(ADD_MOVIE_SETTING );

            assertEquals("Error: inserting a movie again should return the same ID",
                    movieId, newmovieId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_TITLE + " = ?",
                new String[]{ADD_MOVIE_SETTING});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(MovieContract.MovieEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}
