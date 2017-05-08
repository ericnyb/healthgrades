package com.ericbandiero.ratsandmice;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import healthdeptdata.InspectionSorts;
import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 4/27/2016.
 */
public class HeaderSorter {
    private static Comparator<Inspections> sortOrder;
    private static boolean needToReverseSortOrder;
    public void sortHeader(View view,List<Inspections> dataList,ArrayAdapter dataViewerAdapter){
        int id=view.getId();
        if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName() + ">", "Clicked!");
        switch (id) {
            case R.id.txt_dba:
                if (sortOrder== InspectionSorts.SORT_BY_NAME){
                    needToReverseSortOrder =!needToReverseSortOrder;
                }
                else{
                    needToReverseSortOrder =false;
                }
                sortOrder=InspectionSorts.SORT_BY_NAME;
                break;
            case R.id.txt_ins_date:
                if (sortOrder==InspectionSorts.SORT_BY_INSPECTION_DATE_RECENT_FIRST){
                    needToReverseSortOrder =!needToReverseSortOrder;
                }
                else{
                    needToReverseSortOrder =false;
                }
                sortOrder=InspectionSorts.SORT_BY_INSPECTION_DATE_RECENT_FIRST;
                break;
            case R.id.txt_score:
                if (sortOrder==InspectionSorts.SORT_BY_SCORE){
                    needToReverseSortOrder =!needToReverseSortOrder;
                }
                else{
                    needToReverseSortOrder =false;
                }
                sortOrder=InspectionSorts.SORT_BY_SCORE;
                break;
            case R.id.txt_grade:
                if (sortOrder==InspectionSorts.SORT_BY_GRADE){
                    needToReverseSortOrder =!needToReverseSortOrder;
                }
                else{
                    needToReverseSortOrder =false;
                }
                sortOrder=InspectionSorts.SORT_BY_GRADE;
                break;
        }

        if (!needToReverseSortOrder) {
            Collections.sort(dataList, sortOrder);
        }
        else{
            Collections.sort(dataList, Collections.reverseOrder(sortOrder));
        }
        dataViewerAdapter.notifyDataSetChanged();
    }

}
