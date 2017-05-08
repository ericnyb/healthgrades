package com.ericbandiero.ratsandmice;

import android.content.Context;

import java.text.SimpleDateFormat;

/**
 * Created by ${"Eric Bandiero"} on 4/21/2016.
 */
public class AppConstant {

    //TODO Make error log always show, even in production.
    //Set this false before creating release
    public static final boolean DEBUG = true;

    public static final String SHARED_PREF_RECORDS_HEADER_TEXT ="records header text";
    public static final String LAST_FILTER_NAME = "last filter name";

    public static final boolean TEMP_HACK = false;
    public static final String LAST_SORT_ORDER = "last sort order";
    public static final String PROCESS_RESPONSE = "com.ericbandiero.intent.action.PROCESS_RESPONSE";
    public static final String ERROR_GETTING_DATA="Error getting data.";
    public static final SimpleDateFormat DATE_FORMAT_HEADER = new SimpleDateFormat("MM-dd-yy");
    public static final String HEADER_DATE = "01-01-18";
    public static final String GETTING_DATA = "Getting data...";
    public static final String RADIO_MATCH_SELECTED = "radio match selected";
    public static final String SEARCH_FIELD_SELECTED="search field selected:";


    public static final String USER_PREF_ZIP_FILTER_PREFIX ="uzp_";
    public static final String USER_PREF_HIDE_FILTER_SET ="hidden filters";
    public static final String USER_PREF_VIOLATIONS_TO_MARK ="use violations to mark";

    public static final String CURRENT_ZIP_CODE_FILTER_NAME = "CURRENT ZIP CODE";

    public static Context applicationContextMain;

    public static Context getApplicationContextMain() {
        return applicationContextMain;
    }
}
