package com.ericbandiero.ratsandmice;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by ${"Eric Bandiero"} on 4/7/2016.
 */
public class PreferenceUtility {

    private static String thisClass;

    public static String SEARCH_SCREEN_DEFAULT ="";
    public static String FAVORITE_RESTAURANT ="favorite_restaurant";


    public PreferenceUtility() {
        thisClass=this.getClass().getName();
    }

    public static void saveLastFilterName(Activity a) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(AppConstant.LAST_FILTER_NAME, DataProvider.getHealthDataFilter().getFilterName());
        editor.commit();
    }

    /**
     * Used for user selected violation favorites list
     *
     * @param a
     */
    public static void saveUserViolationReports(Activity a, String [] codes, String reportName) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context

        for (int i = 0; i < codes.length; i++) {
            String code = codes[i];
            if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Saving array order:"+code.toString());

        }
        if (AppConstant.DEBUG) Log.i("App config" + ">", "Saving reports");
        SharedPreferences preferences = a.getSharedPreferences("USER_VIOLATIONS_REPORTS", 0);
        SharedPreferences.Editor editor = preferences.edit();
        //editor.putString(codes, reportName);
        editor.putStringSet(reportName, new HashSet<String>(Arrays.asList(codes)));
        editor.commit();
    }

    /**
     * Used for user selected violation favorites list
     *
     * @param
     */
    public static java.util.Map<String, ?> getUserViolationReports() {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences preferences = AppConstant.getApplicationContextMain().getSharedPreferences("USER_VIOLATIONS_REPORTS", 0);
        return preferences.getAll();
    }

    public static void deleteAllUserViolationReports() {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences preferences = AppConstant.getApplicationContextMain().getSharedPreferences("USER_VIOLATIONS_REPORTS", 0);
        preferences.edit().clear().commit();
    }

    public static void deleteUserViolationReports(String key) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        if (AppConstant.DEBUG) Log.i(thisClass + ">", "To delete:" + key);
        SharedPreferences preferences = AppConstant.getApplicationContextMain().getSharedPreferences("USER_VIOLATIONS_REPORTS", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key).apply();

        Toast toast = Toast.makeText(AppConstant.getApplicationContextMain(), "Item was removed.", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    public static void saveSharedPreferenceStrings(String category, String key, String value){
        SharedPreferences preferences = AppConstant.getApplicationContextMain().getSharedPreferences(category, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getSharedPreferenceStrings(String category, String key, String value){
        SharedPreferences preferences = AppConstant.getApplicationContextMain().getSharedPreferences(category, 0);
        String preferencesString = preferences.getString(key, value);
        return preferencesString;
    }

    //Same file used - get String
    public static String getDefaultSharedPreferenceStrings(String key, String default_value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain());
        String preferencesString = preferences.getString(key, default_value);
        return preferencesString;
    }

    //Same file used - save an item
    public static void saveDefaultSharedPreferenceStrings(String key, String value){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    //Same file used - remove an item
    public static void removeDefaultSharedPreferenceStrings(String key){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
        //return preferencesString;
    }

    /**
     * Used for strings
     *
     * @param
     */
    public static java.util.Map<String, ?> getAllPreferenecesStrings(String category) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences preferences = AppConstant.getApplicationContextMain().getSharedPreferences(category, 0);
        return preferences.getAll();
    }

    public static void deleteSharedPreferenceStrings(String category, String key) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        if (AppConstant.DEBUG) Log.i(thisClass + ">", "To delete:" + key);
        SharedPreferences preferences = AppConstant.getApplicationContextMain().getSharedPreferences(category, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key).apply();

        Toast toast = Toast.makeText(AppConstant.getApplicationContextMain(), "Item was removed.", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static int getFavoritesCount() {
        Map<String, ?> allPreferencesStrings = PreferenceUtility.getAllPreferenecesStrings(PreferenceUtility.FAVORITE_RESTAURANT);
        return allPreferencesStrings.size();
    }

    public static boolean checkThatWeHaveFavorites() {
        Map<String, ?> allPreferencesStrings = PreferenceUtility.getAllPreferenecesStrings(PreferenceUtility.FAVORITE_RESTAURANT);
        return allPreferencesStrings.size()>0;
    }

    public static boolean checkIfARestaurantIsFavorite(String camisToSearchFor) {
        Map<String, ?> allPreferencesStrings = PreferenceUtility.getAllPreferenecesStrings(PreferenceUtility.FAVORITE_RESTAURANT);
        return allPreferencesStrings.keySet().contains(camisToSearchFor);
    }

    public static java.util.Map<String, ?> getAllDefaultPreferences(){
        return PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain()).getAll();
    }

    public static java.util.Map<String, ?> getUserZipCodeFilters(){
        Map<String, ?> allEntries = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain()).getAll();

        Iterator it = allEntries.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            if (! pair.getKey().toString().startsWith(AppConstant.USER_PREF_ZIP_FILTER_PREFIX)) {
                //Log.d("Map info", "Key:"+entry.getKey() + " Value: " + entry.getValue().toString());
               // allEntries.remove(pair.getKey());
                it.remove(); // avoids a ConcurrentModificationException
            }

        }
        return allEntries;
    }


    public static void showAllDefaultSavedPreferences(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("Map info", "Key:"+entry.getKey() + " Value: " + entry.getValue().toString());
        }
    }

    public static void getAllZipFilters() {
        Map<String, ?> allDefaultPreferences = getAllDefaultPreferences();
        for (Map.Entry<String, ?> entry : allDefaultPreferences.entrySet()) {
            if( entry.getKey().startsWith(AppConstant.USER_PREF_ZIP_FILTER_PREFIX)) {
                Log.d("Map USER values - key", entry.getKey() + " value: " + entry.getValue().toString());
            }
        }
    }

    public static void saveUserHideFilterChoices(Set<String> setHidden){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(AppConstant.USER_PREF_HIDE_FILTER_SET, setHidden);
        editor.apply();
    }

    public static Set<String> getUserHideFilterChoices(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain());
        Set<String> stringSet = preferences.getStringSet(AppConstant.USER_PREF_HIDE_FILTER_SET, Collections.<String>emptySet());
        return stringSet;
    }


    public static void saveUserViolationsToMark(Set<String> setHidden){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(AppConstant.USER_PREF_VIOLATIONS_TO_MARK, setHidden);
        editor.apply();
    }

    public static Set<String> getUserViolationsToMark(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppConstant.getApplicationContextMain());
        Set<String> stringSet = preferences.getStringSet(AppConstant.USER_PREF_VIOLATIONS_TO_MARK, Collections.<String>emptySet());
        return stringSet;
    }

}
