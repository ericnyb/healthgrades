package com.ericbandiero.ratsandmice.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ericbandiero.ratsandmice.R;

import java.util.List;

import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 4/10/2016.
 */
public class ViolationsAdapter extends ArrayAdapter{

    private int layoutResourceId;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public ViolationsAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        ViolationHolder holder;
        if(row == null)
        {
            //   System.out.println("Resorceid:"+layoutResourceId);
            //System.out.println("Row:"+row.toString());
            //   LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder=new ViolationHolder();

            holder.txtDBA = (TextView)row.findViewById(R.id.text_row_violations);
            row.setTag(holder);

        }

        else{
            holder = (ViolationHolder)row.getTag();
        }

        Inspections inspection= (Inspections) getItem(position);
        holder.txtDBA.setText(inspection.getViolation_description());

        return row;
    }

    static class ViolationHolder
    {
        TextView txtDBA;
        TextView txtDetailData_Address;
        TextView txtDetailData_Phone;
    }
}
