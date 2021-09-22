package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import java.util.Arrays;
import java.util.Date;
/*
Copyright © 2020, University of Alberta. All Rights Reserved.

This software is the confidential and proprietary information
of the Department of Electrical and Computer Engineering at the University of Alberta (UofA).
You shall not disclose such Confidential Information and shall use it only in accordance with the
terms of the license agreement you entered into at the UofA.

No part of the project, including this file, may be copied, propagated, or
distributed except with the explicit written permission of Dr. Edmond Lou
(elou@ualberta.ca).

Project Name       : Brace Monitor Android User Interface - Single

File Name          : NonHeaderRecords.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description   :  The class inherits from Records. The analyzer object is used
                    to analyze the data recorded by the active brace monitor

Data Structure:
                    Analyzer:   List<Days>: contains days information
                                List<Records>: contains all records information

                    Days:       List<Records>: contains records information for that specific day

                    Records(abstract)-------->NonHeaderRecords(abstract)------->ActiveRecords
                       |                                               |------->PassiveRecords
                      |-------------------------------------------------------->HeaderRecords
*/
public abstract class NonHeaderRecords extends Records implements Comparable<Date>{
    Integer longTermFlag = null;
    Date date = null;
    float forceVal;
    float tempVal;
    float averageForceVal;
    float averageTempVal;

    public Date getDate(){
        return date;
    }

    public int[] getTime(){
        return new int[]{date.getHours(),date.getMinutes()};
    }

    public float getForceVal() {
        return forceVal;
    }

    public void setForceVal(float forceVal) {
        this.forceVal = forceVal;
    }

    public float getTempVal() {
        return tempVal;
    }

    public void setTempVal(float tempVal) {
        this.tempVal = tempVal;
    }

    public float getAverageForceVal() {
        return averageForceVal;
    }

    public void setAverageForceVal(float averageForceVal) {
        this.averageForceVal = averageForceVal;
    }

    public float getAverageTempVal() {
        return averageTempVal;
    }

    public void setAverageTempVal(float averageTempVal) {
        this.averageTempVal = averageTempVal;
    }

    @Override
    public int compareTo(Date o) {
        return this.date.compareTo(o);
    }

    public Integer getLongTermFlag() {
        return longTermFlag;
    }

    @Override
    public String getString(boolean isActive)
    {
        if (isActive){
            String[] outputArray = new String[]{String.format("%.1f",forceVal),String.format("%.1f",tempVal),
                    String.valueOf(longTermFlag),
                    String.valueOf(date.getYear()+1900), String.valueOf(date.getMonth()+1),
                    String.valueOf(date.getDate()),String.valueOf(date.getHours()),
                    String.valueOf(date.getMinutes())};
            String output = Arrays.toString(outputArray);
            return output;
        }else {
            String[] outputArray = new String[]{String.format("%.1f",forceVal),String.format("%.1f",tempVal),
                    String.valueOf(date.getYear()+1900), String.valueOf(date.getMonth()+1),
                    String.valueOf(date.getDate()),String.valueOf(date.getHours()),
                    String.valueOf(date.getMinutes())};
            String output = Arrays.toString(outputArray);
            return output;
        }
    }

    public String getReadableString(){
        return "Time: "+date.toString()+"\nPressure: "+String.format("%.1f",forceVal)+"\nTemperature: "+String.format("%.1f",tempVal);
    }
}
