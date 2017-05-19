package com.ericbandiero.ratsandmice;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import healthdeptdata.DataPoints;
import healthdeptdata.HealthDataRestaurants;
import healthdeptdata.Inspections;
import healthdeptdata.DataAnalyzerSingleCamis;
import healthdeptdata.interfaces.IAnalyzeDataSingleCamis;
import com.ericbandiero.myframework.Utility;


/**
 * Checks out inspection data and return a gut feeling.
 * Created by ${"Eric Bandiero"} on 8/5/2016.
 */

public class GutFeeling {

/*
Always a NO
-Closed on last inspection
-If last SCORE is C level

Always at least a MAYBE
-Closed in last year
-Never graded

Always a YES
-If last SCORE is A level
-And never closed in last year

//TODO need to have a previous grade because last grade will show current grade as last.
 */

    public static final int OK=1;
    public static final int MAYBE=0;
    public static final int NO=-1;
    private static final String CLOSED = DataAnalyzerSingleCamis.CLOSED_BY_DOHMH;

    private int analysisResult=0;

    //Can be Grade Pending - it has  a Grade Date
    //This is when they can dispute a grade.
    //We use score as if A,B or C was given.
    //If no grade (was null) we have this as Inspections.NO_GRADE_INSEPCTION_TEXT
    //The latest grade ever received - may be from previous inspection. Can be Grade pending.
    private String lastGrade;

    private boolean lastInspectionWasGraded;

    private String reasonForGutFeeling; //For UI feedback to user.

    private int lastInspectionScore; //Score for last inspection

    List <Inspections> listInspectionByDate=new ArrayList<>();

    private IAnalyzeDataSingleCamis dataAnalyzerSingleCamis;

    private boolean has_c_withinLastYear;

    /**
     * Should be ALL data so we can get analysis down to violation level from IAnalyzeDataSingleCamis
     * @param p_listToInspect - List of Inspections containing every record of data.
     */
    public GutFeeling(List<Inspections> p_listToInspect) {
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","------------------------------------------");
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Data size - number of inspections:"+p_listToInspect.size());
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Camis:"+p_listToInspect.get(0).getCamis());
        dataAnalyzerSingleCamis=new DataAnalyzerSingleCamis(p_listToInspect);
        //listInspectionByDate =p_listToInspect;
        //We get back just a roll-up of inspections by date.
        listInspectionByDate = dataAnalyzerSingleCamis.getInspectionsByDate();
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Ave score by year:"+dataAnalyzerSingleCamis.getAverageScoreByYear().toString());
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Number of closures:"+dataAnalyzerSingleCamis.getCountOfClosures());
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Number of years:"+dataAnalyzerSingleCamis.getDistinctYearsFromInspectionDate().toString());
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Number of closures:"+dataAnalyzerSingleCamis.getListOfInspectionClosures().size());
        DataPoints scoreMetrics = dataAnalyzerSingleCamis.getScoreMetrics();
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Data score metrics:"+scoreMetrics.getAllData());
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Count of critical violations:"+dataAnalyzerSingleCamis.countOfCriticalViolations());
    }

    public int analyze(){
        lastGrade=dataAnalyzerSingleCamis.lastGrade();
        has_c_withinLastYear = withinLastYearHasC();

        //lastInspectionWasUngraded =listInspectionByDate.get(0).getGrade().equals(Inspections.NO_GRADE_INSEPCTION_TEXT) | listInspectionByDate.get(0).getGrade().contains("Not Yet Graded");
        lastInspectionWasGraded = dataAnalyzerSingleCamis.wasLastInspectionGraded();

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">","Last graded inspection grade:"+lastGrade );
        lastInspectionScore = listInspectionByDate.get(0).getScore();
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">","Last inspection score:"+lastInspectionScore );
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">","Last inspection grade:"+listInspectionByDate.get(0).getGrade());
        for (Inspections inspections : listInspectionByDate) {
        //    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Grade:"+inspections.getGrade());
        //    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Action taken:"+inspections.getAction());
        }
        return runAnalysis();
        //return analysisResult;
    }

    private boolean withinLastYearHasC(){
        for (Inspections inspections : listInspectionByDate) {
            if (inspections.getGrade().equals("C") & Utility.isDateWithinLastYear(inspections.getInspection_date())){
                if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Has a C");
                return true;
            }
        }
        return false;
    }

    private boolean closedInLastYear(){
        boolean result;

        List<Inspections> listAllInspections=new ArrayList<>(listInspectionByDate);

        for (Inspections inspection : listAllInspections) {
            if (inspection.getInspection_date()==null){
                if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","last inspection was null");
                continue;
            }
            if (inspection.getAction().toLowerCase().contains(CLOSED)){
                //if(inspection.getInspection_date().getTime()>dateLastYear.getTimeInMillis()){
                return Utility.isDateWithinLastYear(inspection.getInspection_date());
//                if(isDateWithinLastYear(inspection.getInspection_date())){
//                    if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Closed within last year");
//                    return true;
//                }
//                if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","");
            }
        }
        return false;
    }

    private int runAnalysis(){
       // Calendar dateLastYear=Calendar.getInstance();
      //  dateLastYear.add(Calendar.YEAR,-1 );
       // if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Last year:"+dateLastYear);

        //TODO If current is graded A look at previous grade, not just score - put grades into array?

        boolean wasClosedInLastYear=closedInLastYear();

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","First grade text:"+listInspectionByDate.get(0).getGrade());

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","last inspection was graded:"+ lastInspectionWasGraded);

        if (lastGrade.equals(Inspections.NO_GRADE_INSEPCTION_TEXT)){
            if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","last grade ungraded!"+lastGrade);
        }

        //If last inspection was a closure we give it a red.
        if (listInspectionByDate.get(0).getAction()!=null && listInspectionByDate.get(0).getAction().toLowerCase().contains(CLOSED)) {
            reasonForGutFeeling=("Closed on last inspection.");
            return NO;
        }

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Metric - last score:"+lastInspectionScore);
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","Metric - grade a b or c:"+lastGrade.matches("[ABC]"));

        //Want to find a case where last score was 13 or lower but wasn't graded - and last grade was b or c
        if (lastInspectionScore <= HealthDataRestaurants.A_GRADE_UPPER_BOUND_SCORE) {
            //They may have gotten an A score but have yet to be officially graded
            if(wasNeverGraded()){
                reasonForGutFeeling=("Never graded are at best a maybe.");
                return MAYBE;
            }

            //If closed in last year best they can do is a maybe
            if (wasClosedInLastYear) {
                reasonForGutFeeling=("Was closed within last year.");
                return MAYBE;
            }

            if (has_c_withinLastYear){
                reasonForGutFeeling=("Had a C with the last year.");
                return MAYBE;
            }

            //They may have gotten an A score but previous grade was a B or C or were closed in last year
            if (lastGrade.matches("[BC]")) {
                reasonForGutFeeling=("Last grade was a B or C.");
                return MAYBE;
            }


            else{
                //See if they had a bad score on previous inspection
                if(listInspectionByDate.size()>1 && listInspectionByDate.get(1).getScore()>HealthDataRestaurants.B_GRADE_UPPER_BOUND_SCORE){
                    reasonForGutFeeling=("Previous score was in C range.");
                    return MAYBE;
                }
            //They have no blemishes we can find.
                reasonForGutFeeling=("Looks OK");
                return OK;
            }
        }

        //This is a B level score but not graded
        if (lastInspectionScore <= HealthDataRestaurants.B_GRADE_UPPER_BOUND_SCORE & lastInspectionScore > HealthDataRestaurants.A_GRADE_UPPER_BOUND_SCORE & !listInspectionByDate.get(0).getGrade().equals("B") ) {
            reasonForGutFeeling=("You decide for a B level score.");
            return MAYBE;
        }

        //The had a score that would result in a C if graded.
        //They get a NO unless previous grade was an A
        if (lastInspectionScore > HealthDataRestaurants.B_GRADE_UPPER_BOUND_SCORE) {
            reasonForGutFeeling=("Last inspection score is C level.");
            return NO;
        }

        if (listInspectionByDate.get(0).getGrade().equals("B") ||lastGrade.equals("B")){
            if (wasClosedInLastYear){
                reasonForGutFeeling=("B grade and was closed within last year.");
                return NO;
            }
            reasonForGutFeeling=("You decide for a B grade.");
            return MAYBE;
        }



        if (listInspectionByDate.get(0).getGrade().equals("C") ){
            reasonForGutFeeling=("Grade C get a no.");
            return NO;
        }

        //They made it this far...must have had a decent last score
        //A new place without a grade or ungraded always gets a yellow
        if (Reports.getLastReportRun().getReportName().equals(Reports.NEW_RESTAURANTS) & !lastGrade.matches("[ABC]")) {
            //textGutFeeling.setBackgroundColor(colorMaybe);
            //textGutFeeling.setText("Never graded are maybe at best!");
            reasonForGutFeeling=("We never make it here?");
            return MAYBE;
        }

        if (lastGrade.matches("[ABC]")) {
            reasonForGutFeeling=("Last grade an abc");
        }

        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName()+">","No criteria found!");
        return -1;
       // return checkForYes().checkForMaybe();
    }

    private GutFeeling checkForYes(){
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">","check Yes" );
        return this;
    }

    private GutFeeling checkForMaybe(){
        if (AppConstant.DEBUG) Log.d(this.getClass().getSimpleName() + ">","check Maybe" );
        return this;
    }

    public String getReasonForGutFeeling() {
        return reasonForGutFeeling;
    }

    public void setReasonForGutFeeling(String reasonForGutFeeling) {
        this.reasonForGutFeeling = reasonForGutFeeling;
    }

    private boolean wasNeverGraded(){
        return listInspectionByDate.get(0).getGrade().contains("Not Yet Graded");
    }

}
