package com.example.android.booklistingapp;

/**
 * Created by Rudster on 9/21/2016.
 */
public class BookListing {

    //Title of book
    public final String title;

    //Author of book
    public final String author;


    /**
     *
     * @param bookListingTitle is the title of book
     * @param bookListingAuthor is the author of the book
     */
    public BookListing(String bookListingTitle, String bookListingAuthor){
        title = bookListingTitle;
        author = bookListingAuthor;
    }
}
