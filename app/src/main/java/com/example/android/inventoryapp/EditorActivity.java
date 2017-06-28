package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the product's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the supplier's name
     */
    private EditText mSupplierNameEditText;

    /**
     * EditText field to enter the supplier's email address
     */
    private EditText mSupplierEmailEditText;

    /**
     * EditText field to enter the product section
     */
    private Spinner mSectionSpinner;

    /**
     * ImageView field to enter the product image
     */
    private ImageView mImageView;

    /**
     * EditText field to enter additional quantity
     */
    private EditText mAddQuantityEditText;

    /**
     * Section of the product. The possible valid values are in the ProductContract.java file:
     * {@link ProductEntry#SECTION_UNKNOWN}, {@link ProductEntry#SECTION_MAKEUP},
     * {@link ProductEntry#SECTION_SKINCARE}or {@link ProductEntry#SECTION_HAIR}.
     */
    private int mSection = ProductEntry.SECTION_UNKNOWN;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * Identifier for the product data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    /**
     * Boolean flag that keeps track of whether the input has correct values(true) or not (false)
     */
    private boolean mCorrectInput = true;

    private static final int PICK_IMAGE_REQUEST = 0;

    /**
     * Content URI for the current product image
     */
    private Uri mImageUri;

    /**
     * Values for quantity calculations
     */
    private String quantityString;
    private int quantityInt;
    private String addQuantityString;
    private int addQuantityInt;
    private int newQuantityInt;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a product"
            setTitle(getString(R.string.new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Details"
            setTitle(getString(R.string.details));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_product_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_product_supplier_email);
        mSectionSpinner = (Spinner) findViewById(R.id.spinner_section);
        mAddQuantityEditText = (EditText) findViewById(R.id.edit_product_change_quantity);
        mAddQuantityEditText.setText(getString(R.string.default_add_quantity));
        Button mIncreaseQuantity = (Button) findViewById(R.id.button_increase_quantity);
        Button mDecreaseQuantity = (Button) findViewById(R.id.button_decrease_quantity);
        mImageView = (ImageView) findViewById(R.id.imageview);
        Button mImageButton = (Button) findViewById(R.id.button_add_photo);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mSectionSpinner.setOnTouchListener(mTouchListener);
        mIncreaseQuantity.setOnTouchListener(mTouchListener);
        mDecreaseQuantity.setOnTouchListener(mTouchListener);
        mImageButton.setOnTouchListener(mTouchListener);

        setupSpinner();

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });

        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQuantity();
            }
        });

        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity();
            }
        });
    }

    /**
     * Increase quantity
     */
    public void increaseQuantity() {
        addQuantityString = mAddQuantityEditText.getText().toString().trim();
        addQuantityInt = Integer.valueOf(addQuantityString);
        quantityString = mQuantityEditText.getText().toString().trim();
        if (quantityString.isEmpty()) {
            quantityInt = 0;
        } else {
            quantityInt = Integer.valueOf(quantityString);
        }
        newQuantityInt = quantityInt + addQuantityInt;
        mQuantityEditText.setText(String.valueOf(newQuantityInt));
    }

    /**
     * Decrease quantity
     */
    public void decreaseQuantity() {
        addQuantityString = mAddQuantityEditText.getText().toString().trim();
        addQuantityInt = Integer.valueOf(addQuantityString);
        quantityString = mQuantityEditText.getText().toString().trim();
        if (quantityString.isEmpty()) {
            quantityInt = 0;
        } else {
            quantityInt = Integer.valueOf(quantityString);
        }
        newQuantityInt = quantityInt - addQuantityInt;
        if (newQuantityInt < 0) {
            newQuantityInt = 0;
            Toast.makeText(EditorActivity.this, "There is already 0 products", Toast.LENGTH_SHORT).show();
        }
        mQuantityEditText.setText(String.valueOf(newQuantityInt));
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mImageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());

                mImageView.setImageURI(mImageUri);
            }
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the section of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter sectionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_sections_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        sectionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSectionSpinner.setAdapter(sectionSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.section_makeup))) {
                        mSection = ProductEntry.SECTION_MAKEUP; // Makeup
                    } else if (selection.equals(getString(R.string.section_skincare))) {
                        mSection = ProductEntry.SECTION_SKINCARE; // Skincare
                    } else if (selection.equals(getString(R.string.section_hair))) {
                        mSection = ProductEntry.SECTION_HAIR; // Hair
                    } else {
                        mSection = ProductEntry.SECTION_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSection = 0; // Unknown
            }
        });
    }

    /**
     * Get user input from editor and save new product into database
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        Log.i(EditorActivity.class.getSimpleName(), "nameString " + nameString);
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();
        Log.i(EditorActivity.class.getSimpleName(), "email address: " + supplierEmailString);
        String imageString = "";

        // Checking to ensure the image field is not null.
        if (mImageUri != null) {
            imageString = mImageUri.toString();
        }

        float floatValue = Float.parseFloat(priceString);
        priceString = String.format("%.2f", floatValue);
        priceString = priceString.replace(",", ".");

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierEmailString);
        values.put(ProductEntry.COLUMN_SECTION, mSection);
        values.put(ProductEntry.COLUMN_IMAGE, imageString);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Get user input from editor and check if values are missing
     */
    private void validateInput() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();

        String imageString = "";
        // Checking to ensure the image field is not null.
        if (mImageUri != null) {
            imageString = mImageUri.toString();
        }

        if (nameString.isEmpty() || quantityString.isEmpty() || priceString.isEmpty() ||
                supplierNameString.isEmpty() || supplierEmailString.isEmpty() || imageString.isEmpty()) {
            Toast.makeText(this, getString(R.string.values_missing), Toast.LENGTH_SHORT).show();
            mCorrectInput = false;
        } else {
            mCorrectInput = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" and "Order" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemDelete.setVisible(false);
            MenuItem menuItemOrder = menu.findItem(R.id.action_order);
            menuItemOrder.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(EditorActivity.class.getSimpleName(), "inside on menu click");
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                Log.i(EditorActivity.class.getSimpleName(), "inside action save");
                validateInput();
                if (mCorrectInput) {
                    // Save product to database
                    saveProduct();
                    // Exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            //Respond to a click on the "Order" menu option
            case R.id.action_order:
                newOrder();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_SECTION,
                ProductEntry.COLUMN_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,         // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int sectionColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SECTION);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierEmail = cursor.getString(supplierEmailColumnIndex);
            int section = cursor.getInt(sectionColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            if (image != null) {
                mImageUri = Uri.parse(image);
                mImageView.setImageURI(mImageUri);
            }

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(price);
            mSupplierNameEditText.setText(supplierName);
            mSupplierEmailEditText.setText(supplierEmail);

            // Section is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Makeup, 2 is Skincare, 3 is Hair).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (section) {
                case ProductEntry.SECTION_MAKEUP:
                    mSectionSpinner.setSelection(1);
                    break;
                case ProductEntry.SECTION_SKINCARE:
                    mSectionSpinner.setSelection(2);
                    break;
                case ProductEntry.SECTION_HAIR:
                    mSectionSpinner.setSelection(3);
                    break;
                default:
                    mSectionSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierEmailEditText.setText("");
        mSectionSpinner.setSelection(0); // Select "Unknown" section
    }

    /**
     * Prepare the email to order the product
     */
    public void newOrder() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        String emailAddress[] = {mSupplierEmailEditText.getText().toString().trim()};
        String productName = mNameEditText.getText().toString().trim();

        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body) + productName + getString(R.string.email_signature));

        startActivity(emailIntent);
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}
