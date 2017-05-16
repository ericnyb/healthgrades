package com.ericbandiero.ratsandmice;

import android.os.Bundle;

import com.ericbandiero.ratsandmice.parent.ParentActivity;

/**
 * Created by ${"Eric Bandiero"} on 4/7/2016.
 */
public class SettingsActivity extends ParentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

}
