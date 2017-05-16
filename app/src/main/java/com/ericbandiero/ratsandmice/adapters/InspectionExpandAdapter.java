package com.ericbandiero.ratsandmice.adapters;

import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ericbandiero.ratsandmice.AppConstant;
import com.ericbandiero.ratsandmice.PreferenceUtility;
import com.ericbandiero.ratsandmice.interfaces.IMarkable;
import com.ericbandiero.ratsandmice.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 3/12/2016.
 */
public class InspectionExpandAdapter extends BaseExpandableListAdapter {
    private Context _context;
    private List<Inspections> _listDataHeader; // header titles
    private SortedMap<Inspections, List<Inspections>> _mapChildData;  // child data in format of header title, child title
    private int colorGroupViewOriginal;
    private int colorViolationOriginalNotCritical;
    private int colorViolationOriginalCritical;
    private int color_Violation_Back_Color;
    private int color_Violation_Back_Color_User_Marked;
    private int color_Violation_Back_Color_ReportViolationFound;

    private boolean headerRecord;

    private IMarkable markViolations;

    //Use this to look up if header inspection contains violations report is based on - we mark it
    private Map<Integer,Integer> mapMarker=new HashMap<>();

    //This will hold a list of group positions that has children containing violations to be marked.
    private List<Integer> listOfMarkerHits=new ArrayList<>();

    //This holds violation codes the user wants marked all the time.
    private Set<String> setOfUseMarkViolationWatch=new HashSet<>();

    //A list of violation codes that report used - what about user created?
    List<String> listOfReportViolationCodes= HealthDataRestaurants.getViolationCodesArray()!=null? Arrays.asList(HealthDataRestaurants.getViolationCodesArray()):new ArrayList<String>();

    public InspectionExpandAdapter(Context context, List<Inspections> listDataHeader,
                                   SortedMap<Inspections, List<Inspections>> mapChildData, boolean isHeaderRecord) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._mapChildData = mapChildData;
        this.headerRecord = isHeaderRecord;

        //User wants to see these marked.
        setOfUseMarkViolationWatch= PreferenceUtility.getUserViolationsToMark();

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Report violations:"+listOfReportViolationCodes.toString());

        LayoutInflater layoutInflater = (LayoutInflater) this._context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.view_row_items_inspection_data, null);

        LinearLayout ll = (LinearLayout) v
                .findViewById(R.id.linerTop);

        if (headerRecord) {
            //We passed in a null value - so now need to initialize it.
            _listDataHeader = new ArrayList<>();
            _listDataHeader.add(new Inspections());
//            insert(new Inspections(), 0);
//            data = new ArrayList<>();
            //           data.add(new Inspections());
        }


        colorGroupViewOriginal = context.getResources().getColor(R.color.White);
        colorViolationOriginalNotCritical = context.getResources().getColor(R.color.app_color_violation_not_critical_textcolor);
        colorViolationOriginalCritical = context.getResources().getColor(R.color.app_color_violation_critical_textcolor);
        color_Violation_Back_Color = context.getResources().getColor(R.color.White);
        color_Violation_Back_Color_User_Marked = context.getResources().getColor(R.color.app_color_violation_user_marked_backcolor);
        color_Violation_Back_Color_ReportViolationFound = context.getResources().getColor(R.color.app_color_violation_report_code_backcolor);

        // if (AppConfig.DEBUG) Log.i(this.getClass().getSimpleName()+">","Convert view:"+((ColorDrawable)ll.getBackground()).getColor());
        // colorGroupViewOriginal=((ColorDrawable)ll.getBackground()).getColor();

//        if (headerRecord==false) {
//            markViolations.markData(_listDataHeader,_mapChildData, listOfMarkerHits);
//        }


    }



//    public InspectionExpandAdapter(InspectionActivity context, int view_row_items_inspection_data, boolean b) {
//
//    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._mapChildData.get(this._listDataHeader.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "Group position:" + groupPosition);
        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "Child position:" + childPosition);


        final String violation = (String) ((Inspections) getChild(groupPosition, childPosition)).getViolation_description();
        final String code = (String) ((Inspections) getChild(groupPosition, childPosition)).getViolation_code();
        final String childText_Violation;

        //If violation is empty then we swap in inspection type - example:trans fat
        if (violation == null) {
            childText_Violation = "No violation:" + (String) ((Inspections) getChild(groupPosition, childPosition)).getInspection_type();
        } else {
            childText_Violation = violation;
        }

        final boolean isCritical = (boolean) ((Inspections) getChild(groupPosition, childPosition)).getCritical_flag().toLowerCase().startsWith("critical");

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_row_items_inspection_violations, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);

        txtListChild.setText(childText_Violation);


        if (listOfReportViolationCodes.contains(code)){
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Code is report violation code:"+code);
        }

        //TODO Should be case statement
        //We want the user code watchlist to have a different background.
        if (setOfUseMarkViolationWatch.contains(code)){
            txtListChild.setBackgroundColor(color_Violation_Back_Color_User_Marked);
        }
        else {
            txtListChild.setBackgroundColor(color_Violation_Back_Color);
        }

        //We want the report violation codes to have a different background.
        if (listOfReportViolationCodes.contains(code) & !setOfUseMarkViolationWatch.contains(code)){
            txtListChild.setBackgroundColor(color_Violation_Back_Color_ReportViolationFound);
        }



        //Critical violations have different text colors.
        if (isCritical) {
            txtListChild.setTextColor(colorViolationOriginalCritical);
        } else {
            txtListChild.setTextColor(colorViolationOriginalNotCritical);
        }

        return convertView;
    }



    @Override
    public int getChildrenCount(int groupPosition) {
        //For column header this is null
        if (_mapChildData == null) {
            return 0;
        }
        return this._mapChildData.get(this._listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        //TODO This gets called twice because we use match parent in our xml - need to fix.
        String inspectionDate = null;
        String score = "";
        String grade = "";
        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "In group view for position:" + groupPosition);

        if (!headerRecord) {
            inspectionDate = AppConstant.DATE_FORMAT_HEADER.format(((Inspections) getGroup(groupPosition)).getInspection_date());
            score = Integer.toString(((Inspections) getGroup(groupPosition)).getScore());
            grade = ((Inspections) getGroup(groupPosition)).getGrade();
        }
        //String headerTitle = ((Inspections) getGroup(groupPosition)).getDba();
        //String headerTitle = inspectionDate;
//        if (convertView == null) {
//            LayoutInflater infalInflater = (LayoutInflater) this._context
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = infalInflater.inflate(R.layout.list_group, null);
//        }
//
//        TextView lblListHeader = (TextView) convertView
//                .findViewById(R.id.lblListHeader);
//        lblListHeader.setTypeface(null, Typeface.BOLD);
//        lblListHeader.setText(inspectionDate);


        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.view_row_items_inspection_data, null);

//            LinearLayout ll = (LinearLayout) convertView
//                    .findViewById(R.id.linerTop);
//            if (AppConfig.DEBUG) Log.i(this.getClass().getSimpleName()+">","Convert view:"+((ColorDrawable)ll.getBackground()).getColor();
        }

        TextView tvDate = (TextView) convertView
                .findViewById(R.id.textViewInspectionDate);
        //tvDate.setTypeface(null, Typeface.BOLD);


        TextView tvScore = (TextView) convertView
                .findViewById(R.id.textViewInspectionScore);


        TextView tvGrade = (TextView) convertView
                .findViewById(R.id.textViewInspectionGrade);


        TextView tvMark=(TextView) convertView
                .findViewById(R.id.textViewInspectionMark);

        if (headerRecord) {
            tvDate.setText("Date");
            tvScore.setText("Score");
            tvGrade.setText("Grade");
            convertView.setBackgroundColor(parent.getResources().getColor(R.color.LightGrey));
            return convertView;
        } else {
            tvDate.setText(inspectionDate);
            tvScore.setText(score);
            String gradeTransformed = grade.toLowerCase().equals("n/a") ? "Ungraded" : grade;
            tvGrade.setText(gradeTransformed);
        }

        Inspections inspection = (Inspections) getGroup(groupPosition);
        //((Inspections) getGroup(groupPosition)).getGrade();
        if (AppConstant.DEBUG)
            Log.d(this.getClass().getSimpleName() + ">", "Violation:" + inspection.getAction());

        //This is a check mark
        //tvMark.setText("\u2714");
        //This is a heavy exclamation
        //tvMark.setText("\u2757");
        //This is an X
        //tvMark.setText("\u274B");

        //"\u274B" asterisk
        //"\u2716" X
        //System.out.println(getChildrenFromGroupPosition());

       if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Violation code:"+inspection.getViolation_code());


        //Get a list of children - we want to see if any violation has user marked code
        int childCount=getChildrenCount(groupPosition);
        boolean isAlsoAUSerMarkedViolation=false;
        for (int i = 0; i <childCount ; i++) {
            Inspections detailViolation = (Inspections) getChild(groupPosition, i);
            if (setOfUseMarkViolationWatch.contains(detailViolation.getViolation_code())){
                isAlsoAUSerMarkedViolation=true;
                break;
            }
        }


        //System.out.println("Child type:"+child.getClass().getName());


//        if (listOfMarkerHits.contains(groupPosition)){
//            tvMark.setText("\u274B");
//        }

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","List of marker hits:"+setOfUseMarkViolationWatch.toString());
       if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","User codes to mark:"+setOfUseMarkViolationWatch.toString());


        //Right now we don't always build a listOfMarkerHits - depends on report
        //So listOfMarkerHits can be empty

        tvMark.setText("");


        //Find if both by counting position frequency in listOfMakerHits
        int countOfHits=Collections.frequency(listOfMarkerHits,groupPosition);


        //Just marking for only a user flagged violation
//        if (listOfMarkerHits.isEmpty()&isAlsoAUSerMarkedViolation){
//            tvMark.setText("\u2716");
//        }

        if (isAlsoAUSerMarkedViolation){
           // tvMark.setText(Html.fromHtml("\u2716"));
            tvMark.setText(Html.fromHtml("X"));
            tvMark.setTextColor(Color.RED);
        }

        //This has a report violation but not a user flagged one
        if (listOfMarkerHits.contains(groupPosition) & !isAlsoAUSerMarkedViolation){
            tvMark.setText(Html.fromHtml("\u274B"));
            tvMark.setTextColor(Color.RED);
        }


        //This has both
//        if (listOfMarkerHits.contains(groupPosition)&isAlsoAUSerMarkedViolation){
//            tvMark.setText("\u274B"+"\u2716");
//        }
        if (countOfHits>1){
            tvMark.setText(Html.fromHtml("\u274B"+"X"));
            tvMark.setTextColor(Color.RED);
        }

//        if (isAlsoAUSerMarkedViolation){
//           if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Both violations");
//           // tvMark.setText("\u274B"+"\u2714");
//            //tvMark.setText("\u2714");
//           tvMark.setText("\u2716");
//        }
//        else{
//            tvMark.setText("\u274B");
//        }

       if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","List of violations:"+listOfMarkerHits.toString());



        if (listOfMarkerHits.contains(groupPosition)|isAlsoAUSerMarkedViolation){
            tvMark.setVisibility(View.VISIBLE);
        }
        else{
            tvMark.setVisibility(View.INVISIBLE);
        }

        if (inspection.getAction()!=null && inspection.getAction().toLowerCase().contains(_context.getString(R.string.Closed))) {
            convertView.setBackgroundColor(parent.getResources().getColor(R.color.Orange));
        } else {
//            if (hasViolation) {
//                convertView.setBackgroundColor(Color.BLUE);
//            } else {
                convertView.setBackgroundColor(colorGroupViewOriginal);
//            }
            // convertView.setBackgroundColor(Color.YELLOW);
        }

        //getChildrenFromGroupPosition(groupPosition);
        // convertView.setBackgroundColor(Color.GREEN);
        //convertView.setBackgroundColor(colorGroupViewOriginal);
        return convertView;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public List<Inspections> getChildrenFromGroupPosition(int groupPosition) {

//        List<Inspections> listViolations = new ArrayList<>();
//        List<Inspections> inspectionsList = this._mapChildData.get(this._listDataHeader.get(groupPosition))
//                .subList(0, 1);
//
//
//        if (AppConfig.DEBUG)
//            Log.i(this.getClass().getSimpleName() + ">", "Number of violations for inspection by date:" + this._mapChildData.get(this._listDataHeader.get(groupPosition))
//                    .size());
//
//        if (AppConfig.DEBUG)
//            Log.i(this.getClass().getSimpleName() + ">", "Violation list:" + inspectionsList.toString());
        //listViolations.addAll(_mapChildData.get(getGroup(groupPosition)))
        return null;
    }

    //Calls marker routine to find which header has children with violations codes to mark.
    //Note: the routine changes listOfMakerHits
    public void setMarkViolations(IMarkable markViolations) {
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","Hitting the mark");
        this.markViolations = markViolations;
        if (!headerRecord) {
            listOfMarkerHits=markViolations.markData(_listDataHeader,_mapChildData);
        }
    }
}
