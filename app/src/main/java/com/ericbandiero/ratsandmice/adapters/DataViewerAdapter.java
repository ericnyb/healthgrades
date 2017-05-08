package com.ericbandiero.ratsandmice.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.AppUtility;
import com.ericbandiero.ratsandmice.R;

import java.util.List;

import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 11/11/2015.
 */
public class DataViewerAdapter extends ArrayAdapter<Inspections> {
    private Context context;
    private int layoutResourceId;
    private List<Inspections> data;
    private int row_backcolor_original;
    boolean isColumnHeader;
    private String [] columnNames={"Name","Date","Score","Grade"};

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a layout to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public DataViewerAdapter(Context context, int resource, List<Inspections> objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
    }

    public DataViewerAdapter(Context context, int resource) {
        super(context, resource);
        this.layoutResourceId = resource;
        this.context = context;
        isColumnHeader = true;
        add(new Inspections());
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        InspectionHolder holder;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        if (row == null) {
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new InspectionHolder();
            holder.txtDBA = (TextView) row.findViewById(R.id.txt_dba);
            holder.txtInspectDate = (TextView) row.findViewById(R.id.txt_ins_date);
            holder.txtScore = (TextView) row.findViewById(R.id.txt_score);
            holder.txtDetailData_Address = (TextView) row.findViewById(R.id.txt_address);
            holder.txtDetailData_Phone = (TextView) row.findViewById(R.id.txt_phone);
            row_backcolor_original = row.getDrawingCacheBackgroundColor();
            holder.txtGrade = (TextView) row.findViewById(R.id.txt_grade);
            row.setTag(holder);
        }
        else {
            holder = (InspectionHolder) row.getTag();
        }

        //Column headers
        if (isColumnHeader) {
            holder.txtDBA.setText(columnNames[0]);
            holder.txtInspectDate.setText(columnNames[1]);
            holder.txtScore.setText(columnNames[2]);
            holder.txtGrade.setText(columnNames[3]);


            row.setBackgroundColor(context.getResources().getColor(R.color.app_color_column_header_back));
            return row;
        }

        Inspections restaurant = data.get(position);

        //Name
        holder.txtDBA.setText(restaurant.getDba());

        boolean haveInspectionDate = false;

        try {
            if (null != restaurant.getInspection_date()) {
                haveInspectionDate = true;
            }
        } catch (NullPointerException ex) {
            haveInspectionDate = false;
        }

        //Inspection date
        if (haveInspectionDate && AppConstant.DATE_FORMAT_HEADER.format(restaurant.getInspection_date()).equals(AppConstant.HEADER_DATE)) {
            holder.txtInspectDate.setText("Date");
        } else {
            if (haveInspectionDate) {
                holder.txtInspectDate.setText(AppConstant.DATE_FORMAT_HEADER.format(restaurant.getInspection_date()));
            } else {
                holder.txtInspectDate.setText("N/A");
            }
        }

        //Score of C or above
        if (restaurant.getScore() > HealthDataRestaurants.B_GRADE_UPPER_BOUND_SCORE) {
            row.setBackgroundColor(context.getResources().getColor(R.color.app_color_C_Score));
            holder.txtScore.setText(Integer.toString(restaurant.getScore()));
            //holder.txtScore.setTextColor(Color.WHITE);
            //row.setTextColor(Color.WHITE);
        } else {
            row.setBackgroundColor(row_backcolor_original);
            holder.txtScore.setText(Integer.toString(restaurant.getScore()));
        }


        if (restaurant.getAction()!=null && restaurant.getAction().toLowerCase().contains(context.getString(R.string.Closed))) {
            row.setBackgroundColor(parent.getResources().getColor(R.color.app_color_closed));
        }
        else {
            //If this was set above we don't want to step on that color change
           // if (((ColorDrawable)row.getBackground()).getColor()!= context.getResources().getColor(R.color.app_color_C_Score)) {
            if (((ColorDrawable)row.getBackground()).getColor()== row_backcolor_original) {
                row.setBackgroundColor(row_backcolor_original);
            }
            //holder.txtScore.setText(Integer.toString(restaurant.getScore()));
        }

        //Grade
        holder.txtGrade.setText(restaurant.getGrade());

        //Address
        AppUtility.inspectionBuildAddress(restaurant, holder.txtDetailData_Address,true);

        //Phone
        holder.txtDetailData_Phone.setText(restaurant.getPhone());

        return row;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    static class InspectionHolder {
        TextView txtDBA;
        TextView txtScore;
        TextView txtGrade;
        TextView txtInspectDate;
        TextView txtDetailData_Address;
        TextView txtDetailData_Phone;
    }
}
