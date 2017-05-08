package com.ericbandiero.ratsandmice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ericbandiero.ratsandmice.activities.DataViewerActivity;

/**
 * Created by ${"Eric Bandiero"} on 10/24/2016.
 */
public class ReportFieldBuilder {

    private static String valueToFind;
    private static String fieldNameToSearch;
    private static String description;
    private Context context;
    public static String ARRAY_PREFIX="fr";
    public static int PREFIX_LENGTH=5;


    //We use this to pass info along to save reports
    private static String [] aData=new String[4];

    public ReportFieldBuilder(String valueToFind, String fieldNameToSearch, String description, Context context) {
        this.valueToFind = valueToFind;
        this.fieldNameToSearch = fieldNameToSearch;
        this.description = description;
        this.context = context;


        aData[0]=ARRAY_PREFIX+"01_"+"field_report";
        aData[1]=ARRAY_PREFIX+"02_"+valueToFind;
        aData[2]=ARRAY_PREFIX+"03_"+fieldNameToSearch;
        aData[3]=ARRAY_PREFIX+"04_"+description;

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Search value:"+valueToFind);
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Field to search:"+fieldNameToSearch);
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Description:"+description);
    }

    public void runFieldReport() {
        //TODO Not sure if we hit an error running report from here. Maybe need try catch.
        Intent intent = new Intent();
        intent.setClass(context, DataViewerActivity.class);
        Intent serviceToRun;
        Reports r = new Reports();
        Bundle bundle = new Bundle();
        serviceToRun = new Intent(context, DataProvider.class);
        //Determines if filter is used - in field search we always want filter - otherwise error occurs.
        if (Reports.getLastReportRun().getReportName().equals(Reports.META_CUISINES_BY_BORO)) {
            serviceToRun.putExtra(DataProvider.STRICT_SEARCH_MUST_USE_FILTER, true);
        } else {
            serviceToRun.putExtra(DataProvider.STRICT_SEARCH_MUST_USE_FILTER, true);
        }
        r.setReportName(Reports.FIELD_SEARCH);
        r.setReportDesc(description);
        Reports.setLastReportRun(r);
        bundle.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, r);
        // serviceToRun.putExtra("cuisine",query);
        serviceToRun.putExtra("search_query", valueToFind);
        serviceToRun.putExtra("field", fieldNameToSearch);
        serviceToRun.putExtras(bundle);
        context.startService(serviceToRun);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String[] getaData() {
        return aData;
    }

    public static String removePrefix(String stringWithPrefix){

        return stringWithPrefix.contains(ARRAY_PREFIX)?stringWithPrefix.substring(5):stringWithPrefix;
    }
}
