package com.ericbandiero.ratsandmice;

import android.util.Log;

import com.ericbandiero.ratsandmice.interfaces.IMarkable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import healthdeptdata.Inspections;

/**
 * Created by ${"Eric Bandiero"} on 3/28/2016.
 * This goes through violations and sees which group has them.
 * Violation codes come from those used to run a report and those on user marked watchlist.
 *
 */
public class MarkViolations implements IMarkable {

    private String [] violationsCodesToLookFor;

    public MarkViolations(String[] _violationCodesToLookFor) {
        this.violationsCodesToLookFor =_violationCodesToLookFor;
        setUpUserFlagMarks();
    }

    //Maybe not the best place for this - but we use it.
    //Add to the violations already in list
    //End result is we have report violation PLUS user violations codes to mark.
    private void setUpUserFlagMarks() {

        //These come from the report that was run
        List<String> listViolationCodes=new ArrayList<>();

        if (violationsCodesToLookFor!=null) {
            for (int i = 0; i < violationsCodesToLookFor.length; i++) {
                listViolationCodes.add(violationsCodesToLookFor[i]);
            }
        }
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Violation for report:"+listViolationCodes.toString());

        //Get the user violations marked for flagging
        Set<String> userViolationsToMark = PreferenceUtility.getUserViolationsToMark();

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Marked violations by user:"+userViolationsToMark.toString());

        //Add them to the list
        for (String s : userViolationsToMark) {
            listViolationCodes.add(s);
        }

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","All marked violations:"+listViolationCodes.toString());

        //Put into an array
        violationsCodesToLookFor =listViolationCodes.toArray(new String[0]);

        //For debugging
        for (int i = 0; i < violationsCodesToLookFor.length; i++) {
            String s = violationsCodesToLookFor[i];
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Codes = " + s);
        }
    }

    /**
     * This goes through children of inspection header and looks for violations found in list sent in constructor.
     * This isn't always called yet - we do it on a report by report basis.
     * @param _listDataHeader
     * @param sortedMapChildren
        */
    @Override
    public List<Integer> markData(List<Inspections> _listDataHeader,SortedMap<Inspections, List<Inspections>> sortedMapChildren) {
        List<Integer>listOfHits=new ArrayList<>();

    if (AppConstant.DEBUG) Log.i(this.getClass().getSimpleName()+">","In marker");
        SortedMap<Inspections, List<Inspections>> _sortedMapChildren=new TreeMap<>(sortedMapChildren);

        for (List<Inspections> listViolations : _sortedMapChildren.values()) {
            for (Inspections inspection : listViolations) {
             //   if (inspection.getViolation_description()!=null&&inspection.getViolation_description().toLowerCase().contains(violationsToLookFor.toString())) {
                if (inspection.getViolation_code()!=null && searchForTextHit(inspection)) {
                    for (int i = 0; i < _listDataHeader.size(); i++) {
                        if(inspection.getInspection_date().equals(_listDataHeader.get(i).getInspection_date())){
                            listOfHits.add(i);
                        }
                    }
                }
            }
        }
        return listOfHits;
    }

    private boolean searchForTextHit(Inspections inspect){
        for (String violationCode : violationsCodesToLookFor) {
            if (inspect.getViolation_code().toLowerCase().contains(violationCode.toLowerCase())){
                return true;
            }
        }
        return false;
    }
}
