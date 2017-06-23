package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    /* Inner class that defines the table contents of the products table */
    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // Table name
        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_SUPPLIER_EMAIL = "email";
        public static final String COLUMN_SECTION = "section";

        public static final int SECTION_UNKNOWN = 0;
        public static final int SECTION_MAKEUP = 1;
        public static final int SECTION_SKINCARE = 2;
        public static final int SECTION_HAIR = 3;

        /**
         * Returns whether or not the given section is {@link #SECTION_UNKNOWN}, {@link #SECTION_MAKEUP},
         * {@link #SECTION_SKINCARE} or {@link #SECTION_HAIR}.
         */
        public static boolean isValidSection(int section) {
            if (section == SECTION_UNKNOWN || section == SECTION_MAKEUP || section == SECTION_SKINCARE || section == SECTION_HAIR) {
                return true;
            }
            return false;
        }
    }
}
