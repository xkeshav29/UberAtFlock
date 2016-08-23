package com.uberforflock.model;

/**
 * Created by kumarke on 8/22/16.
 */
public class Ride {

    private String status;

    private Driver driver;


    class Driver{
        private String phone_number;
        private String rating;
    }



//    {
//        "status": "accepted",
//            "destination": {
//        "latitude": 12.9887782914,
//                "longitude": 77.64
//    },
//        "product_id": "db6779d6-d8da-479f-8ac7-8068f4dade6f",
//            "request_id": "a0ece17a-f5df-4662-bd37-06ceef6a4938",
//            "driver": {
//        "phone_number": "(555)555-5555",
//                "phone_number": 4.9,
//                "picture_url": "https://d1a3f4spazzrp4.cloudfront.net/uberex-sandbox/images/driver.jpg",
//                "name": "John",
//                "sms_number": null
//    },
//        "pickup": {
//        "latitude": 12.96,
//                "eta": 1,
//                "longitude": 77.64
//    },
//        "eta": 1,
//            "location": {
//        "latitude": 12.96,
//                "bearing": -150,
//                "longitude": 77.64
//    },
//        "vehicle": {
//        "make": "Toyota",
//                "picture_url": "https://d1a3f4spazzrp4.cloudfront.net/uberex-sandbox/images/prius.jpg",
//                "model": "Prius",
//                "license_plate": "UBER-PLATE"
//    },
//        "surge_multiplier": 1,
//            "shared": false
//    }
}
