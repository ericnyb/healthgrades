package com.ericbandiero.ratsandmice.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ericbandiero.ratsandmice.R;

/**
 * Created by ${"Eric Bandiero"} on 11/13/2015.
 */
public class DialogSorterFragment extends DialogFragment {

    private SortAlertListener sortAlertListener;


    public DialogSorterFragment() {

    }

    public interface SortAlertListener {
        void onFinishSortDialog(String user);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] sortItems1 = getResources().getStringArray(R.array.sort_items);


//        String [] sortoption = new String[2];
//        sortoption[0]="One";
//        sortoption[1]="Two";
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.m_pick_sort)
                //        builder.setMessage("Pick a song")
//                .setPositiveButton("Hello", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // FIRE ZE MISSILES!
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }})

                .setItems(sortItems1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sortAlertListener.onFinishSortDialog(sortItems1[which]);
                        //System.out.println(sortItems1[which]);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
             sortAlertListener = (SortAlertListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
}
}
