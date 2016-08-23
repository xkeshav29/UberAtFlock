package com.uberforflock.model;

/**
 * Created by kumarke on 8/22/16.
 */
public class Ride {

    private String status;

    private Driver driver;

    private Vehicle vehicle;

    private int surge_multiplier;

    public String getStatus() {
        return status;
    }

    public Driver getDriver() {
        return driver;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public int getSurge_multiplier() {
        return surge_multiplier;
    }

    class Driver{
        private String phone_number;
        private String rating;
        private String picture_url;
        private String name;
        private int eta;

        public int getEta() {
            return eta;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public String getRating() {
            return rating;
        }

        public String getPicture_url() {
            return picture_url;
        }

        public String getName() {
            return name;
        }
    }

    class Vehicle{

        private String make;
        private String picture_url;
        private String model;
        private String license_plate;

        public String getMake() {
            return make;
        }

        public String getPicture_url() {
            return picture_url;
        }

        public String getModel() {
            return model;
        }

        public String getLicense_plate() {
            return license_plate;
        }
    }


}
