package com.ericbandiero.ratsandmice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ericbandiero.ratsandmice.interfaces.ISetUpData;

/**
* Created by ${"Eric Bandiero"} on 4/3/2016.
*/
public class HealthDataMainReceiver extends BroadcastReceiver {


    ISetUpData setUpData;

    public HealthDataMainReceiver(ISetUpData iSetUpData) {
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Setup class:"+iSetUpData.getClass().getName());
        this.setUpData = iSetUpData;
    }

    //This will be registered by the activity that wants to receive broadcast.
    //This is called from DataProvider - after it gets data it calls this if registered.


    //We do this when a broadcast is sent with the process response.
    @Override
    public void onReceive(Context context, Intent intent) {

        if (AppConstant.DEBUG)
            Log.i(this.getClass().getSimpleName() + ">", "In "+this.getClass().getSimpleName());

        String responseMessage = intent.getStringExtra(DataProvider.RESPONSE_MESSAGE);

        Reports repFromParcel = (Reports) intent.getParcelableExtra(Reports.PARCEBLABLE_TEXT_FOR_EXTRA);

        if (repFromParcel != null) {
            if (AppConstant.DEBUG)
                Log.i(this.getClass().getSimpleName() + ">", "Parcel report name:" + repFromParcel.getReportName());
            Log.i(this.getClass().getSimpleName() + ">", "Parcel id::" + repFromParcel.getReportId());
        }


       if(intent.getBooleanExtra(AppConstant.ERROR_GETTING_DATA,false)){
           if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","There was an error getting data");
           if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Error:"+DataProvider.errorMessage);
       }

        setUpData.setUpData();

    }
}
