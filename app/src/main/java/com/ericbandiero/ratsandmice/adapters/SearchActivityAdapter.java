package com.ericbandiero.ratsandmice.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ericbandiero.ratsandmice.AppUtility;
import com.ericbandiero.ratsandmice.R;

import java.util.List;

import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 3/31/2016.
 */
public class SearchActivityAdapter extends ArrayAdapter{

    private int layoutResourceId;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public SearchActivityAdapter(Context context, int resource, List objects) {
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

        //We could do it this way - but below looks cleaner and only do lookup once
        //textViewRestaurantName=(TextView)findViewById(R.id.inc_inspection_restaurant_header).findViewById(R.id.textViewRestaurantName);
//        View c = findViewById(R.id.inc_inspection_restaurant_header);
//        textViewRestaurantName = (TextView) c.findViewById(R.id.textViewRestaurantName);
//        textViewRestaurantPhone = (TextView) c.findViewById(R.id.textViewRestaurantPhone);
//        textViewRestaurantAddress = (TextView) c.findViewById(R.id.textViewRestaurantAddress);
//        textViewRestaurantAddress.setText("Yo!");

        SearchHolder holder;
        View row = convertView;

        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        if(row == null)
        {
            //   System.out.println("Resorceid:"+layoutResourceId);
            //System.out.println("Row:"+row.toString());
            //   LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SearchHolder();
            holder.txtDBA = (TextView)row.findViewById(R.id.textViewRestaurantName);
            holder.txtDetailData_Address =(TextView)row.findViewById(R.id.textViewRestaurantAddress);
            holder.txtDetailData_Phone =(TextView)row.findViewById(R.id.textViewRestaurantPhone);
            row.setTag(holder);
        }
        else{
                holder = (SearchHolder)row.getTag();
           }

        Inspections inspection= (Inspections) getItem(position);
        holder.txtDBA.setText(inspection.getDba());
        holder.txtDetailData_Address.setText(inspection.getStreet()+" "+inspection.getBuilding());

        AppUtility.inspectionBuildAddress(inspection, holder.txtDetailData_Address,true);

        holder.txtDetailData_Phone.setText(inspection.getPhone());



        return row;
    }


    static class SearchHolder
    {
        TextView txtDBA;
        TextView txtDetailData_Address;
        TextView txtDetailData_Phone;
    }
}
