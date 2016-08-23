package com.uberforflock.model;

import java.util.List;

public class Availability {
    private Times times;

    public Times getTimes() {
        return times;
    }

    private class Times{
        private List<Product> productsList;
    }

    private class Product{
        private String localized_display_name;
        private int estimate;
        private String displayName;
        private String product_id;

        public String getLocalized_display_name() {
            return localized_display_name;
        }

        public int getEstimate() {
            return estimate;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getProduct_id() {
            return product_id;
        }
    }
}
