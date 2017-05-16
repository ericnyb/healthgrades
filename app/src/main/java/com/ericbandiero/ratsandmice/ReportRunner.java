package com.ericbandiero.ratsandmice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;

import com.ericbandiero.ratsandmice.activities.DataViewerActivity;
import com.ericbandiero.ratsandmice.activities.ViolationsActivity;

/**
 * Created by ${"Eric Bandiero"} on 9/29/2016.
 */
public class ReportRunner{

    private Bundle bundleHoldingParcelReport = new Bundle();
    private Context context;
    private Activity activity;
    private String thisClass=getClass().getName();

    public void runReport(Reports reportToRun, Activity activity,Spinner spinnerFilters){
        this.activity=activity;

        if(!AppUtility.checkNetwork()){
            return;
        }

        Intent intent = new Intent();
        context = activity.getApplicationContext();

        //We want to make sure location is on
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">", "Selected spinner:" + (spinnerFilters.getSelectedItem() != null ? spinnerFilters.getSelectedItem() : "Null"));


        if (spinnerFilters.getSelectedItem()!=null){
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Spinner selected in Main Activity:"+spinnerFilters.getSelectedItem().toString());
            if (spinnerFilters.getSelectedItem().toString().equals(AppConstant.CURRENT_ZIP_CODE_FILTER_NAME.toString())) {
                try {
                    if(!LocationGetter.getLocation(activity)){
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(this.getClass().getSimpleName()+">","Location getter tossed error:"+e.getMessage().toString());
                }
            }
        }

        //Some reports are just for internal use - we don't need to show them in an activity class.

        //We want to check this and make a switch on the run.
        //Violation reports
        if (reportToRun.getReportId() == Reports.REPORTS_VIOLATION_CODES_QUERY) {
            ViolationsActivity.aViolationCodes = reportToRun.getCodeArray();
        }

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "Report we are going to run:" + reportToRun.getReportName());

        bundleHoldingParcelReport.putParcelable(Reports.PARCEBLABLE_TEXT_FOR_EXTRA, reportToRun);

        //Store this so other activities can use it
        Reports.setLastReportRun(reportToRun);

        Intent serviceToRun = new Intent(context, DataProvider.class);

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Context:"+context.toString());

        //Switch for which activity to run
        //TODO Clean this up - give them an ID so we know what activity they use
        switch (reportToRun.getReportName()) {
            //This is when user is looking up a violation to run report on.
            case Reports.VIOLATIONS_USER_CREATE:
                intent.setClass(context, ViolationsActivity.class);
                // intent.putExtra("LOOKUP")
                break;
            case Reports.VIOLATIONS_USER_FLAG_SET:
                intent.setClass(context, ViolationsActivity.class);
                break;
            case Reports.META_CUISINES_BY_BORO:
                intent.setClass(context, ViolationsActivity.class);
                break;
            case Reports.META_CUISINES_BY_FILTER:
                intent.setClass(context, ViolationsActivity.class);
                break;
            case Reports.META_CHAINS:
                intent.setClass(context, ViolationsActivity.class);
                break;
            //serviceToRun.putExtra()
            case Reports.META_RESTAURANTS_BY_BORO:
                intent.setClass(context, ViolationsActivity.class);
                break;
            case Reports.META_RESTAURANTS_BY_FILTER:
                intent.setClass(context, ViolationsActivity.class);
                break;
            default:
                intent.setClass(context, DataViewerActivity.class);
        }

        //We pass along  the report as a bundle in almost all cases
        if (reportToRun.getReportId()!= Reports.REPORTS_NO_DATA_ACTIVITY_NEEDED) {
            intent.putExtras(bundleHoldingParcelReport);
            intentRunner(intent);
        }


        serviceToRun.putExtras(bundleHoldingParcelReport);
        context.startService(serviceToRun);
    }

    private void intentRunner(Intent intent) {
        //Right now this is always the DataViewerActivity.class
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Intent:"+intent.getComponent().getShortClassName().replace(".",""));
        intent.putExtra("STARTED_FROM_MAIN_ACTIVITY", "TRUE");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        //  intent.putExtras(bundleHoldingParcelReport);
        // startActivity(intent);



        switch (Reports.getLastReportRun().getReportName()){
            case Reports.VIOLATIONS_USER_CREATE:
                if (context instanceof Activity) {
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Violation request code.");
                    ((Activity) context).startActivityForResult(intent, ViolationsActivity.REQUEST_CODE);
                }
                else{
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Violation request code - activity");
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Activity intent:"+activity.getIntent().toString());
                    activity.startActivityForResult(intent, ViolationsActivity.REQUEST_CODE);

                }
                break;

            case Reports.VIOLATIONS_USER_FLAG_SET:
                if (context instanceof Activity) {
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Violation flag set.");
                    ((Activity) context).startActivityForResult(intent, ViolationsActivity.REQUEST_CODE_VIOLATION_FLAG);
                }
                else{
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Violation flag set activity.");
                    activity.startActivityForResult(intent, ViolationsActivity.REQUEST_CODE_VIOLATION_FLAG);

                }
                break;
                default:
                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Running regular start activity.");
                    if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">",context.toString());

                    context.startActivity(intent);
        }


        if (1==1){
            return;
        }


        if (Reports.getLastReportRun().getReportName().equals(Reports.VIOLATIONS_USER_CREATE)) {
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, ViolationsActivity.REQUEST_CODE);
            }
            else{
                activity.startActivityForResult(intent, ViolationsActivity.REQUEST_CODE);
            }
        }
        else if(Reports.getLastReportRun().getReportName().equals(Reports.VIOLATIONS_USER_FLAG_SET)){
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, ViolationsActivity.REQUEST_CODE_VIOLATION_FLAG);
            }
            else{
                activity.startActivityForResult(intent, ViolationsActivity.REQUEST_CODE_VIOLATION_FLAG);
            }
        }
        else{
            if (AppConstant.DEBUG) Log.d(new Object() { }.getClass().getEnclosingClass()+">","Running regular start activity.");

            context.startActivity(intent);
        }
    }
}
