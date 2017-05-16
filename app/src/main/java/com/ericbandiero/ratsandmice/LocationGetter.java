package com.ericbandiero.ratsandmice;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ericbandiero.librarymain.UtilsShared;
import com.ericbandiero.ratsandmice.activities.DataViewerActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * We use this to get the user's location.
 * Created by ${"Eric Bandiero"} on 9/20/2016.
 */
public class LocationGetter {

    private static final String MOVING = "moving";

    public static int PERMISSION_FINE_REQUEST_CODE=5;


    private static LocationListener locationListener;
   // private static Context context;
    private static LocationManager locationManager;


    //right now none minute
    @SuppressWarnings("PointlessArithmeticExpression")
    private static final int REQUEST_NEW_UPDATE_ELAPSED_TIME = (1000 * 60) * 1;
    private static final int MAX_NUMBER_OF_ADDRESS = 5;

    private static String bestProvider;

    private static String PERMISSION_MESSAGE="Location permission needs to be granted to use the filter 'Current Zip Code'. You can go into Device settings->App->This app-> and Permissions";
    private static String PERMISSION_TITLE="Location Permission Required";


    private static boolean isLocationEnabled() {

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (AppConstant.DEBUG) Log.d(new Object() {
            }.getClass().getEnclosingClass() + ">", "GPS is enabled.");
        } catch (Exception ex) {
            if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Error:"+ex.getMessage());
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (AppConstant.DEBUG) Log.d(new Object() {
            }.getClass().getEnclosingClass() + ">", "Network is enabled.");
        } catch (Exception ex) {
            if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Error:"+ex.getMessage());
        }

        /*
        if (!gps_enabled && !network_enabled) {
            return false;
        }
        return true;
        */
        return !(!gps_enabled && !network_enabled);
    }

    public static boolean getLocation(Context p_context) {

     final Context  context = p_context;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        int i = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (i== PackageManager.PERMISSION_DENIED){
            UtilsShared.AlertMessageSimple(context,PERMISSION_TITLE,PERMISSION_MESSAGE);
            return false;
        }



        //Location is not enabled - we do nothing unless user turns it on.
        if (!isLocationEnabled()) {
            showNoLocationDialog(context);
            return false;
        }

        //We want to try to use network.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setSpeedRequired(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        bestProvider = locationManager.getBestProvider(criteria, true);

        if (AppConstant.DEBUG) Log.d(new Object() {
        }.getClass().getEnclosingClass() + ">", "Best provider..." + bestProvider);


        if (locationManager.isProviderEnabled(bestProvider)) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);
            if (lastKnownLocation != null) {

                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Last known from lat:" + bestProvider + " " + lastKnownLocation.getLatitude());
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Last known from long:" + bestProvider + " " + lastKnownLocation.getLongitude());
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Last time of update:" + bestProvider + " " + new Date(lastKnownLocation.getTime()).toString());
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Last time to string:" + bestProvider + " " + lastKnownLocation.toString());

                Bundle bundle = lastKnownLocation.getExtras();

                String travelState = bundle.get("travelState") == null ? "n/a" : bundle.get("travelState").toString().toLowerCase();

                if (travelState.toLowerCase().equals(MOVING)) {
                    if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","use is moving...");
                   // Toast.makeText(context, "Travel state:"+travelState, Toast.LENGTH_LONG).show();
                }

                /*
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log.d(new Object() {
                    }.getClass().getEnclosingClass() + ">", String.format("%s %s (%s)", "Key:" + key,
                            "Value:" + value.toString(), value.getClass().getName()));
                }
                */

                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();


                List<Address> listOfAddresses = AppUtility.getAddressesFromGeoCoder(context, latitude, longitude, MAX_NUMBER_OF_ADDRESS);


                if (!listOfAddresses.isEmpty()) {
                    //Address address = listOfAddresses.get(0);
                    Set<String> zips = new HashSet<>();

                    AppUtility.getZipCodesFromAddressList(listOfAddresses, zips);

                    //Toast.makeText(context, "Postal code from last know address:" + address.getPostalCode(), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(context, "Zip code(s) from last know address:" + zips.toString(), Toast.LENGTH_SHORT).show();
                    //if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Postal code from last know address:" + address.getPostalCode());
                    if (AppConstant.DEBUG) Log.d(new Object() {
                    }.getClass().getEnclosingClass() + ">", "Zip code(s) from last know address:" + zips.toString());


                    AppUtility.setLastKnownZipCode(zips);

                }


                long timeDelta = new Date().getTime() - lastKnownLocation.getTime();

                logSomeData(lastKnownLocation, timeDelta);


                //If this is less than one minute we can just use current zip if exists in AppUtility lastKnownZipCode
                //If more than we continue on to request an update.
                //We also get a new update if we are moving
                if ((timeDelta < REQUEST_NEW_UPDATE_ELAPSED_TIME) & (!travelState.equals(MOVING))) {
                    if (AppConstant.DEBUG) Log.d(new Object() {
                    }.getClass().getEnclosingClass() + ">", "Was less than " + REQUEST_NEW_UPDATE_ELAPSED_TIME + " seconds");
                    return true;
                } else {
                    if (AppConstant.DEBUG) Log.d(new Object() {
                    }.getClass().getEnclosingClass() + ">", "Was longer than " + REQUEST_NEW_UPDATE_ELAPSED_TIME + " seconds or we are moving");
                }
            } else {
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "No known last location");
                //showNoLocationDialog();
            }
        }


        //If we reached here then we need to ask for a location update
        locationListener = new LocationListenerCustom(context);

        //locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);


        Thread thread = new Thread() {
            public void run() {
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Thread Running");

                Looper.prepare();
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Start request time:" + Calendar.getInstance().getTime().toString());
                //Just get one update - we don't want to keep asking

                int i = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
                if (i== PackageManager.PERMISSION_DENIED){
                    UtilsShared.AlertMessageSimple(context,PERMISSION_TITLE,PERMISSION_MESSAGE);
                }
                else{
                    locationManager.requestSingleUpdate(bestProvider, locationListener, null);
                    Looper.loop();
                }
                //  locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
            }
        };

        thread.start();

        try {
            if (bestProvider.equals(LocationManager.GPS_PROVIDER)) {
                //AppUtility.toastIt("Using GPS", Toast.LENGTH_SHORT);
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Using GPS!");

            }

            if (AppUtility.getLastKnownZipCode() == null) {
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Last known zip code is null");

            } else {
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Last known zip code:" + AppUtility.getLastKnownZipCode());
            }

            if (AppUtility.getLastKnownZipCode() == null || AppUtility.getLastKnownZipCode().isEmpty()) {
                if (AppConstant.DEBUG) Log.d(new Object() {
                }.getClass().getEnclosingClass() + ">", "Last zip code is empty");
                Thread.sleep(6000);
            } else {
                Thread.sleep(bestProvider.equals(LocationManager.GPS_PROVIDER) ? 4000 : 3000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private static void logSomeData(Location lastKnownLocation, long timeDelta) {
        if (AppConstant.DEBUG) Log.d(new Object() {
        }.getClass().getEnclosingClass() + ">", "Time now:" + new Date().getTime());
        if (AppConstant.DEBUG) Log.d(new Object() {
        }.getClass().getEnclosingClass() + ">", "Time now:" + new Date().toString());
        if (AppConstant.DEBUG) Log.d(new Object() {
        }.getClass().getEnclosingClass() + ">", "Last known update time:" + new Date(lastKnownLocation.getTime()).toString());
        if (AppConstant.DEBUG) Log.d(new Object() {
        }.getClass().getEnclosingClass() + ">", "Last known update time:" + lastKnownLocation.getTime());
        if (AppConstant.DEBUG) Log.d(new Object() {
        }.getClass().getEnclosingClass() + ">", "Accuracy:" + lastKnownLocation.getAccuracy());
        if (AppConstant.DEBUG) Log.d(new Object() {
        }.getClass().getEnclosingClass() + ">", "Delta:" + timeDelta);
        if (AppConstant.DEBUG) Log.d(new Object() {
        }.getClass().getEnclosingClass() + ">", "Delta divided by one minute:" + (timeDelta / REQUEST_NEW_UPDATE_ELAPSED_TIME));
    }


    private static void stopUpdateRequest() {
        locationManager.removeUpdates(locationListener);
    }

    private static void showNoLocationDialog(final Context context) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Location is turned off!" +
                DataViewerActivity.NEWLINE +
                DataViewerActivity.NEWLINE +
                "To use the Current Zip Code filter, you must turn it on." +
                "If you don't want to turn it on, then create a new filter with one zip code." +
                DataViewerActivity.NEWLINE +
                DataViewerActivity.NEWLINE +
                "Turn location on?");


        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(viewIntent);
                if (AppConstant.DEBUG)
                    Log.d(this.getClass().getSimpleName() + ">", "user chose to turn location on...");
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });


// 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class LocationListenerCustom implements android.location.LocationListener {

        Context context2;

        public LocationListenerCustom(Context context) {
            context2=context;
        }

        /**
         * Called when the location has changed.
         * <p/>
         * <p> There are no restrictions on the use of the supplied Location object.
         *
         * @param location The new location, as a Location object.
         */
        @Override
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if (AppConstant.DEBUG) Log.d(new Object() {
            }.getClass().getEnclosingClass() + ">", "Request received time:" + Calendar.getInstance().getTime().toString());
            if (AppConstant.DEBUG)
                Log.d(this.getClass().getSimpleName() + ">", "Location updated:" + location.getLongitude());
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
           
            List<Address> listOfAddresses;
            
            Set<String> zips = new HashSet<>();

            //Get a list of Addresses
            listOfAddresses = AppUtility.getAddressesFromGeoCoder(context2, latitude, longitude, MAX_NUMBER_OF_ADDRESS);


            AppUtility.getZipCodesFromAddressList(listOfAddresses, zips);
            AppUtility.setLastKnownZipCode(zips);

            //Toast.makeText(context, "Zip code(s) from listener:" + zips.toString(), Toast.LENGTH_SHORT).show();

            if (AppConstant.DEBUG) Log.d(new Object() {
            }.getClass().getEnclosingClass() + ">", "Zip code(s) from listener:" + zips.toString());



            stopUpdateRequest();
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        /**
         * Called when the provider is enabled by the user.
         *
         * @param provider the name of the location provider associated with this
         *                 update.
         */
        @Override
        public void onProviderEnabled(String provider) {

            if (AppConstant.DEBUG) Log.d(new Object() {
            }.getClass().getEnclosingClass() + ">", "Location was turned on:");

        }

        /**
         * Called when the provider is disabled by the user. If requestLocationUpdates
         * is called on an already disabled provider, this method is called
         * immediately.
         *
         * @param provider the name of the location provider associated with this
         *                 update.
         */
        @Override
        public void onProviderDisabled(String provider) {

        }
    }

}

