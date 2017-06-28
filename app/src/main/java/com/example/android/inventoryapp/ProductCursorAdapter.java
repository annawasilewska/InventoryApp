package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_imageview);

        //Find the columns of product attributes  that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_IMAGE);

        // Read the product attributes from the Cursor for the current product
       final String name = cursor.getString(nameColumnIndex);
        String quantity = cursor.getString(quantityColumnIndex);
        String price = cursor.getString(priceColumnIndex);
        String image = cursor.getString(imageColumnIndex);

        // Populate fields with extracted properties
        if (image != null) {
            Uri imgUri = Uri.parse(image);
            imageView.setImageURI(imgUri);
        }
        nameTextView.setText(name);
        quantityTextView.setText(quantity);
        priceTextView.setText(price);

        final int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry._ID));
        final int itemQty = cursor.getInt(cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));

        final Button saleButton = (Button) view.findViewById(R.id.list_item_button_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                if (itemQty > 0) {
                    int mItemQty;
                    mItemQty = (itemQty - 1);
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, mItemQty);
                    Uri uri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, itemId);
                    context.getContentResolver().update(uri, values, null, null);
                    String saleMessage = "One unit of " + name + " sold.";
                    Toast.makeText(context, saleMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, name + context.getString(R.string.quantity_run_out), Toast.LENGTH_SHORT).show();
                }
                context.getContentResolver().notifyChange(ProductContract.ProductEntry.CONTENT_URI, null);
            }
        });
    }
}
