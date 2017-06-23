package com.example.android.inventoryapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

public class DetailsActivity extends AppCompatActivity {

    /** EditText field to enter the product section */
    private Spinner mSectionSpinner;

    /**
     * Section of the product. The possible valid values are in the ProductContract.java file:
     * {@link ProductEntry#SECTION_UNKNOWN}, {@link ProductEntry#SECTION_MAKEUP},
     * {@link ProductEntry#SECTION_SKINCARE}or {@link ProductEntry#SECTION_HAIR}.
     */
    private int mSection = ProductEntry.SECTION_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mSectionSpinner = (Spinner) findViewById(R.id.spinner_section);

        setupSpinner();
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
}
