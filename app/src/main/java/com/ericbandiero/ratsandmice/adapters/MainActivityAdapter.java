package com.ericbandiero.ratsandmice.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ericbandiero.ratsandmice.R;
import com.ericbandiero.ratsandmice.Reports;

import java.util.List;


/**
 * Created by ${"Eric Bandiero"} on 3/21/2016.
 */
@Deprecated
public class MainActivityAdapter extends ArrayAdapter<Reports>{

    private Context context;
    private int layoutResourceId;
    private List<Reports> data;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public MainActivityAdapter(Context context, int resource, List<Reports> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
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

        ReportNameHolder holder;
        if(row == null)
        {
            //   System.out.println("Resorceid:"+layoutResourceId);
            //System.out.println("Row:"+row.toString());
            //   LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ReportNameHolder();
           // holder.txtReportName = (TextView)row.findViewById(R.id.txtview_main_screen_report);
            row.setTag(holder);
        }
        else
        {
            holder = (ReportNameHolder)row.getTag();
        }

        holder.txtReportName.setText(data.get(position).getReportName());
        int color=parent.getContext().getResources().getColor(R.color.White);
        holder.txtReportName.setBackgroundColor(color);

       // row.setBackgroundColor( R.color.LightYellow);

       // return super.getView(position, convertView, parent);
        return row;
    }

    static class ReportNameHolder
    {
        TextView txtReportName;
    }
}
