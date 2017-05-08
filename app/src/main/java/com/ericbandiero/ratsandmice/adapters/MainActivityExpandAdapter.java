package com.ericbandiero.ratsandmice.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.ericbandiero.ratsandmice.PreferenceUtility;
import com.ericbandiero.ratsandmice.R;
import com.ericbandiero.ratsandmice.Reports;

import java.util.HashMap;
import java.util.List;


/**
 * Created by ${"Eric Bandiero"} on 3/22/2016.
 */
public class MainActivityExpandAdapter extends BaseExpandableListAdapter{

    List<Reports> listDataHeader;
    HashMap<Reports, List<Reports>> mapChildData;
    Context context;
    int reHeaderTextColor;


    public MainActivityExpandAdapter(Context _context, List<Reports> _listDataHeader,
                                   HashMap<Reports, List<Reports>> _mapChildData) {

        listDataHeader=_listDataHeader;
        mapChildData=_mapChildData;
        context=_context;
        reHeaderTextColor=context.getResources().getColor(R.color.app_color_inspection_text_color);
    }

    /**
     * Gets the number of groups.
     *
     * @return the number of groups
     */
    @Override
    public int getGroupCount() {
    //  return 0;
        return listDataHeader.size();
    }

    /**
     * Gets the number of children in a specified group.
     *
     * @param groupPosition the position of the group for which the children
     *                      count should be returned
     * @return the children count in the specified group
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        //For column header this is null
        if (mapChildData==null||mapChildData.isEmpty()){
            return 0;
        }

        //Not every parent will have a child
        if (this.mapChildData.get(this.listDataHeader.get(groupPosition))==null){
            return 0;
        }

        return this.mapChildData.get(this.listDataHeader.get(groupPosition))
                .size();
    }

    /**
     * Gets the data associated with the given group.
     *
     * @param groupPosition the position of the group
     * @return the data child for the specified group
     */
    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    /**
     * Gets the data associated with the given child within the given group.
     *
     * @param groupPosition the position of the group that the child resides in
     * @param childPosition the position of the child with respect to other
     *                      children in the group
     * @return the data of the child
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mapChildData.get(this.listDataHeader.get(groupPosition))
                .get(childPosition);
    }

    /**
     * Gets the ID for the group at the given position. This group ID must be
     * unique across groups. The combined ID (see
     * {@link #getCombinedGroupId(long)}) must be unique across ALL items
     * (groups and all children).
     *
     * @param groupPosition the position of the group for which the ID is wanted
     * @return the ID associated with the group
     */
    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    /**
     * Gets the ID for the given child within the given group. This ID must be
     * unique across all children within the group. The combined ID (see
     * {@link #getCombinedChildId(long, long)}) must be unique across ALL items
     * (groups and all children).
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group for which
     *                      the ID is wanted
     * @return the ID associated with the child
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * Indicates whether the child and group IDs are stable across changes to the
     * underlying data.
     *
     * @return whether or not the same ID always refers to the same object
     * @see android.widget.Adapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Gets a View that displays the given group. This View is only for the
     * group--the Views for the group's children will be fetched using
     * {@link #getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)}.
     *
     * @param groupPosition the position of the group for which the View is
     *                      returned
     * @param isExpanded    whether the group is expanded or collapsed
     * @param convertView   the old view to reuse, if possible. You should check
     *                      that this view is non-null and of an appropriate type before
     *                      using. If it is not possible to convert this view to display
     *                      the correct data, this method can create a new view. It is not
     *                      guaranteed that the convertView will have been previously
     *                      created by
     *                      {@link #getGroupView(int, boolean, android.view.View, android.view.ViewGroup)}.
     * @param parent        the parent that this view will eventually be attached to
     * @return the View corresponding to the group at the specified position
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.view_row_items_main_screen_reports_parent, null);
        }

        Reports repHeader=(Reports) getGroup(groupPosition);

        TextView tvMore=(TextView) convertView
                .findViewById(R.id.txtview_main_screen_report_more);


        //Want user to see if there are children reports
        if (getChildrenCount(groupPosition)==0){
            tvMore.setText("None");
            //tvMore.setVisibility(View.INVISIBLE);
        }
         else{
              //tvMore.setText(isExpanded?"Less":repHeader.getReportName().equals(Reports.USER_FAVORITES_HEADER)?"("+getChildrenCount(groupPosition)+")":"More");
              tvMore.setText(isExpanded?"Less":"More");
           // tvMore.setText(isExpanded?"-":"+");
            tvMore.setVisibility(View.VISIBLE);
        }


        //For favorite restaurants we show how many there are
        //We run report that shows the favorites
        if (repHeader.getReportName().equals(Reports.FAVORITE_RESTAURANTS)){
            int favsCnt= PreferenceUtility.getFavoritesCount();
                tvMore.setText(favsCnt>0?"("+favsCnt+")":"None");
                tvMore.setVisibility(View.VISIBLE);
        }

        TextView tvRepHeader = (TextView) convertView
                .findViewById(R.id.txtview_main_screen_report_parent);

        tvRepHeader.setText(repHeader.getReportName());

        //TODO Decide about this color scheme
        if (repHeader.getReportId()<0){
          //  tvRepHeader.setTextColor(context.getResources().getColor(R.color.Green));
            tvRepHeader.setTextColor(Color.BLACK);
        }
        else{
            tvRepHeader.setTextColor(Color.BLACK);
        }

        return convertView;
    }

    /**
     * Gets a View that displays the data for the given child within the given
     * group.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child (for which the View is
     *                      returned) within the group
     * @param isLastChild   Whether the child is the last child within the group
     * @param convertView   the old view to reuse, if possible. You should check
     *                      that this view is non-null and of an appropriate type before
     *                      using. If it is not possible to convert this view to display
     *                      the correct data, this method can create a new view. It is not
     *                      guaranteed that the convertView will have been previously
     *                      created by
     *                      {@link #getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)}.
     * @param parent        the parent that this view will eventually be attached to
     * @return the View corresponding to the child at the specified position
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String subReportName=(String) ((Reports)getChild(groupPosition, childPosition)).getReportName();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.view_row_items_main_screen_reports_child, null);
        }

        TextView txtviewSubReport=(TextView) convertView
                .findViewById(R.id.txtview_main_screen_report_child);

        txtviewSubReport.setText(subReportName);
        return convertView;
    }

    /**
     * Whether the child at the specified position is selectable.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group
     * @return whether the child is selectable.
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void removeChildItem(int groupPosition,int childPosition){
        this.mapChildData.get(this.listDataHeader.get(groupPosition)).remove(childPosition);
        notifyDataSetChanged();
    }

}
