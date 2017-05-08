package com.ericbandiero.ratsandmice.parent;

import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.HealthDataMainReceiver;
import com.ericbandiero.ratsandmice.R;

/**
 * Created by ${"Eric Bandiero"} on 4/3/2016.
 */
public class ParentActivity extends ActionBarActivity{
    protected static ProgressDialog progressDialog;
    protected TextView textViewHeader;
    protected TextView textViewLabels;
    protected HealthDataMainReceiver receiver;

    public ParentActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        int actionbarColor=getResources().getColor(R.color.RoyalBlue);
        actionBar.setBackgroundDrawable(new ColorDrawable(actionbarColor));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">", "In on Pause");
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (receiver!=null) {
            unRegisterOurReceiver();
        }
    }

    protected void unRegisterOurReceiver(){
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
                //if (AppConstant.DEBUG) Log.e(this.getClass().getSimpleName() + ">", e.getMessage());
            }
        }
    }
}
