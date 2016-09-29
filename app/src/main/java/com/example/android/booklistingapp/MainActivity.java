package com.example.android.booklistingapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    /**
     * URL to query Google Books API dataset for booklisting info
     */
    private static final String GOOGLE_BOOKS_REQUEST_URL =
            "https://www.googleapis.com/books/v1/volumes?q=";

    //ListView Variables
    private EditText searchText;
    private Button searchButton;
    private ListView listView;
    private ArrayList<BookListing> bookArrayList;
    private BookAdapter adapter;
    private String userInput;
    private TextView emptyJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find the Search Button
        searchButton = (Button) findViewById(R.id.search_button);
        emptyJson = (TextView) findViewById(R.id.error_search);

        //Find the book_list_activity
        listView = (ListView) findViewById(R.id.list);
        bookArrayList = new ArrayList<BookListing>();
        adapter = new BookAdapter(this, bookArrayList);
        listView.setAdapter(adapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.clear();
                searchText = (EditText) findViewById(R.id.search_text);
                userInput = searchText.getText().toString().replace(" ", "+");

                if (userInput.isEmpty()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Nothing entered to be searched";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

                //perform network request
                BookAsyncTask task = new BookAsyncTask();
                task.execute();
            }
        });

    }

    /**
     * Update the screen to display info from the give {@link BookListing}.
     */
    private void updateUi(List<BookListing> book) {

        if (book.isEmpty()) {
            emptyJson.setText("No Results Found");
        } else {
            emptyJson.setText("");
            adapter.addAll(book);
            adapter.notifyDataSetChanged();
        }
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

    private List<BookListing> extractBookListingFromJson(String bookListingJSON) {
        List<BookListing> book = new ArrayList<>();
        //If the JSON string is empty or null, then return early
        if (TextUtils.isEmpty(bookListingJSON)) {
            return null;
        }

        try {
            JSONObject baseJsonResponse = new JSONObject((bookListingJSON));
            JSONArray bookArray = baseJsonResponse.getJSONArray("items");
            for (int i = 0; i < bookArray.length(); i++) {
                //Extract out the first item (which is an book)
                JSONObject currentBook = bookArray.getJSONObject(i);
                JSONObject volumeInfo = currentBook.getJSONObject("volumeInfo");


                //Extract out the book title, authors, and url
                String author = volumeInfo.getString("authors");
                String title = volumeInfo.getString("title");
                String url = volumeInfo.getString("infoLink");

                BookListing bookListing = new BookListing(title, author, url);
                book.add(bookListing);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problems parsing the Book Listing JSON results", e);
        }
        return book;
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the first book in the response.
     */

    private class BookAsyncTask extends AsyncTask<URL, Void, List<BookListing>> {

        @Override
        protected List<BookListing> doInBackground(URL... urls) {
            // Create URL object
            URL url = createUrl(GOOGLE_BOOKS_REQUEST_URL + userInput);

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<BookListing> book = extractBookListingFromJson(jsonResponse);
            return book;
        }

        /**
         * Update the screen with the given book listing (which was the result of the
         * {@link BookAsyncTask}).
         */
        @Override
        protected void onPostExecute(List<BookListing> book) {
            if (book == null) {
                return;
            }

            updateUi(book);
        }


        /**
         * Return a (@Link BookListing) object by parsing out info about the first book listing
         * from the input bookListingJSON
         */

    }
}
