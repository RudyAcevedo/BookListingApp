package com.example.android.booklistingapp;

/**
 * Created by Rudster on 9/21/2016.
 */
public class BookListing {

    //Title of book
    public final String mTitle;

    //Author of book
    public final String mAuthor;

    //url for book
    public final String mUrl;


    /**
     * @param bookListingTitle  is the title of book
     * @param bookListingAuthor is the author of the book
     * @param bookListingUrl
     */
    public BookListing(String bookListingTitle, String bookListingAuthor, String bookListingUrl) {
        mTitle = bookListingTitle;
        mAuthor = bookListingAuthor;
        mUrl = bookListingUrl;
    }

    //Return book title
    public String getTitle() {
        return mTitle;
    }

    //Return book author
    public String getAuthor() {
        return mAuthor;
    }

    //return book url
    public String getUrl() {
        return mUrl;
    }
}
