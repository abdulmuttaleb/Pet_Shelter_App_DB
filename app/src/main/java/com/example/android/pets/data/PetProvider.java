package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;

import java.sql.SQLData;

/**
 * Created by ahmed on 3/29/2018.
 */

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    PetDbHelper mDbHelper;


    //URI Matchers

    public static final int PETS = 100;
    public static final int PET_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);

        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#",PET_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return false;
    }

    @Override
    public String getType( Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        switch (match){
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion isn't supported for "+uri);

        }
    }

    @Override
    public int update( Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePet(uri, contentValues,selection, selectionArgs );

            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues,selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update isn't supported for uri " + uri);
        }
    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        switch(match){
            case PETS:

                return database.delete(PetEntry.TABLE_NAME,selection,selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return database.delete(PetEntry.TABLE_NAME,selection,selectionArgs);

            default:
                throw new IllegalArgumentException("Deletion isn't supported for uri "+uri);
        }
    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch(match){

        	case PETS:
                 cursor = database.query(PetEntry.TABLE_NAME, projection, null, null, null, null, null);
                break;
        	case PET_ID:
        		selection = PetContract.PetEntry._ID + "=?";
        		selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetEntry.TABLE_NAME,projection, selection, selectionArgs, null, null, sortOrder);
        		break;
        	default:
        		throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        return cursor;
    }

    private Uri insertPet(Uri uri, ContentValues values){

        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        // Check that the gender is valid
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        // No need to check the breed, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(PetEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        if(values.size() == 0){
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        return database.update(PetEntry.TABLE_NAME,values,selection, selectionArgs);
    }
}
