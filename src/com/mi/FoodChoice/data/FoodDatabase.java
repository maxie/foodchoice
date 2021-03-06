package com.mi.FoodChoice.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FoodDatabase {

    public static final String TABLE_UNEXPECTED_SHOP = "unexpected_shop";

    public static final String[] PROJECTION_UNEXPECTED_SHOP = new String[] {
            UnexpectedShop._ID, UnexpectedShop.BUSINESS_ID, UnexpectedShop.SHOP_NAME,
            UnexpectedShop.IS_EXCLUDED, UnexpectedShop.ADD_DATE
    };

    public static class UnexpectedShop implements BaseColumns {

        public static final Uri URI_UNEXPECTED_TABLE = Uri
                .parse(FoodDbProvider.CONTENT_URI + "/unexpected_shop");

        public static final String BUSINESS_ID = "business_id";

        public static final String SHOP_NAME = "shop_name";

        public static final String IS_EXCLUDED = "is_excluded";

        public static final String ADD_DATE = "add_date";

    }
}
