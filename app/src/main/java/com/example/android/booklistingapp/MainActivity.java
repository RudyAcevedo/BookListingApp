package com.example.android.booklistingapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    /**
     * URL to query Google Books API dataset for booklisting info
     */
    private static final String GOOGLE_BOOKS_REQUEST_URL =
            "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Perform network request
        BookAsyncTask task = new BookAsyncTask();
        task.execute();
    }

    /**
     * Update the screen to display info from the give {@link BookListing}.
     */
    private void updateUi(BookListing book){
        //Display the booklisting title in UI
        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(book.title);

        //Display the book's author in the UI
        TextView authorTextView = (TextView) findViewById(R.id.author);
        authorTextView.setText(book.author);



    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first book in the response.
     */

    private class BookAsyncTask extends AsyncTask<URL, Void, BookListing> {

        @Override
        protected BookListing doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(GOOGLE_BOOKS_REQUEST_URL);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }
            BookListing book = extractBookListingFromJson(jsonResponse);
            return book;
        }

        /**
         * Update the screen with the given book listing (which was the result of the
         * {@link BookAsyncTask}).
         */
        @Override
        protected void onPostExecute(BookListing book) {
            if (book == null) {
                return;
            }

            updateUi(book);
        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringURL) {
            URL url = null;
            try {
                url = new URL(stringURL);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Make HTTP request to the given URL
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            //If the URL is null, then return early
            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000)/*milliseconds*/;
                urlConnection.setConnectTimeout(15000/*milliseconds*/);
                urlConnection.connect();

                //If url was successful (response 200),
                //then read the input stream and parse response.
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code : " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the book listing JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    //function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Convert the {@link InputStream} into a String which contains the
         * whole JSON response from the server.
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Return a (@Link BookListing) object by parsing out info about the first book listing
         * from the input bookListingJSON
         */
        private BookListing extractBookListingFromJson(String bookListingJSON) {
            //If the JSON string is empty or null, then return early
            if (TextUtils.isEmpty(bookListingJSON)) {
                return null;
            }

            try {
                JSONObject baseJsonResponse = new JSONObject((bookListingJSON));
                JSONArray bookArray = baseJsonResponse.getJSONArray("items");

                //If there are results in the features array
                if (bookArray.length() > 0) {
                    //Extract out the first item (which is an book)
                    JSONObject firstItem = bookArray.getJSONObject(0);
                    JSONObject volumeInfo = firstItem.getJSONObject("volumeInfo");

                    //Extract out the book title, and authors
                    String title = volumeInfo.getString("title");
                    String author = volumeInfo.getString("authors");


                    //Create a new {@link BookListing} object
                    return new BookListing(title, author);

                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problems parsing the Book Listing JSON results", e);
            }
            return null;
        }
    }
}
