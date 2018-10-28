package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;
/**
 * Created by takahashiteruyuki on 2017/09/02.
 */
public class InventoryCursorAdapter extends CursorAdapter {
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
     }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView numberTextView = (TextView) view.findViewById(R.id.number);
        int idIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
        int numberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NUMBER);
        int id = cursor.getInt(idIndex);
        String inventoryName = cursor.getString(nameColumnIndex);
        String inventoryPrice = cursor.getString(priceColumnIndex);
        final int inventoryNumber = cursor.getInt(numberColumnIndex);
        final String inventoryNumberString = Integer.toString(inventoryNumber);
        final Uri uri = Uri.parse(InventoryEntry.CONTENT_URI + "/" + id);
        Button sell = (Button) view.findViewById(R.id.sell);
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        ContentValues updateValues = new ContentValues();
                        if (inventoryNumber - 1 < 0) {
                            Toast.makeText(context, "Item is empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updateValues.put(InventoryEntry.COLUMN_INVENTORY_NUMBER, inventoryNumber - 1);
                        int rowsAffected = context.getContentResolver().update(uri, updateValues, null, null);

                        if (rowsAffected > 0) {
                            context.getContentResolver().notifyChange(uri, null);
                        } else {
                            Toast.makeText(context, "Sale could not be recorded", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        nameTextView.setText(inventoryName);
        priceTextView.setText(inventoryPrice+"  $");
        numberTextView.setText(inventoryNumberString+"  packs");
    }
}
