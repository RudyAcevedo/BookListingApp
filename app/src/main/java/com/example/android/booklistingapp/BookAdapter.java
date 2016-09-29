package com.example.android.booklistingapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Rudster on 9/28/2016.
 */
public class BookAdapter extends ArrayAdapter<BookListing> {

    public BookAdapter(Activity context, ArrayList<BookListing> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Check if the existing view is being reused, otherwise infate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate
                    (R.layout.book_list_activity, parent, false);

            BookListing currentBook = getItem(position);

            TextView titleTextview = (TextView) listItemView.findViewById(R.id.book_title);
            titleTextview.setText(currentBook.getTitle());

            TextView authorTextView = (TextView) listItemView.findViewById(R.id.book_author);
            authorTextView.setText(currentBook.getAuthor());

            TextView urlTextView = (TextView) listItemView.findViewById(R.id.book_url);
            urlTextView.setText(currentBook.getUrl());

        }
        return listItemView;


    }
}