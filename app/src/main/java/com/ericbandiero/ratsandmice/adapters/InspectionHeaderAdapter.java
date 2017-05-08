package com.ericbandiero.ratsandmice.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.R;

import java.util.ArrayList;
import java.util.List;

import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 3/7/2016.
 */
@Deprecated
public class InspectionHeaderAdapter extends ArrayAdapter<Inspections>{


    private Context context;
    private int layoutResourceId;
    private List<Inspections> data;
    private boolean headerRecord;


    //Standard colors when we created this
    int rowBackGroundColor;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public InspectionHeaderAdapter(Context context, int resource, List<Inspections> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","In constructor");
    }

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     */
    public InspectionHeaderAdapter(Context context, int resource, boolean header) {
        super(context, resource);
        this.layoutResourceId = resource;
        this.context = context;
        headerRecord=header;
        if (headerRecord) {
            insert(new Inspections(), 0);
            data = new ArrayList<>();
            data.add(new Inspections());
        }
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
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        InspectionHolder holder;
        if (row==null) {
            row = inflater.inflate(layoutResourceId, parent, false);
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">", "Row:" + position);


            holder = new InspectionHolder();
            holder.txtScore = (TextView)row.findViewById(R.id.textViewInspectionScore);
            holder.txtGrade = (TextView)row.findViewById(R.id.textViewInspectionGrade);
            holder.txtInspectionDate = (TextView)row.findViewById(R.id.textViewInspectionDate);

            rowBackGroundColor=row.getDrawingCacheBackgroundColor();
            row.setTag(holder);
        }
        else
        {
            holder = (InspectionHolder)row.getTag();
        }

        Inspections restaurant = data.get(position);
        //Score

        if(headerRecord){
            holder.txtInspectionDate.setText("Date");
            holder.txtGrade.setText("Grade");
            holder.txtScore.setText("Score");
            row.setBackgroundColor(Color.LTGRAY);
            return row;
        }
            holder.txtScore.setText(Integer.toString(restaurant.getScore()));
            holder.txtInspectionDate.setText(AppConstant.DATE_FORMAT_HEADER.format(restaurant.getInspection_date()));

            if (restaurant.getAction().toLowerCase().startsWith("establishment closed by dohmh")){
                row.setBackgroundColor(Color.YELLOW);
            }
        else
            {
                row.setBackgroundColor(rowBackGroundColor);
            }

            holder.txtGrade.setText(restaurant.getGrade().equals("N/A")?"Ungraded":restaurant.getGrade());

        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "DBA:" + restaurant.getDba());
        Log.d(this.getClass().getSimpleName() + ">", "Score:" + restaurant.getScore());
        return row;
    }

    public void getDataSize(){
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Data size:"+data.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Data changed");
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Data size using getcount:"+getCount());
    }


    static class InspectionHolder
    {
        TextView txtInspectionDate;
        TextView txtScore;
        TextView txtGrade;
    }

}
