package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

/**
 * Created by ahmed on 4/6/2018.
 */

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c){
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){

        TextView petName = view.findViewById(R.id.pet_name);
        TextView petSummary = view.findViewById(R.id.summary);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_NAME));
        String summary = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_BREED));

        petName.setText(name);
        petSummary.setText(summary);

    }
}
