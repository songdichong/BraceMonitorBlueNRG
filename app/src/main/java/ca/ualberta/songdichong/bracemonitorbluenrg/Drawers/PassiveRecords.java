package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import android.util.Log;

import java.util.*;

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

File Name          : PassiveRecords.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description   :  The class inherits from NonHeaderRecords. The PassiveRecords object is used
                    to form a single record from the data by the passive brace monitor.

Data Structure:
                    Analyzer:   List<Days>: contains days information
                                List<Records>: contains all records information

                    Days:       List<Records>: contains records information for that specific day

                    Records(abstract)----!isHeader---->NonHeaderRecords(abstract)------->ActiveRecords
                       |                                                        |------->PassiveRecords
                      |----------------------isHeader---------------------------------->HeaderRecords*/
public class PassiveRecords extends NonHeaderRecords {
    public PassiveRecords(int year, int month, int date, int hour, int minute, float force, float temperature){
        try{
            this.date = new Date(year-1900,month-1,date,hour,minute);
            this.forceVal = force;
            this.tempVal = temperature;
            this.averageForceVal = force;
            this.averageTempVal = temperature;
            this.longTermFlag = null;
        }
        catch (Exception e){

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassiveRecords records = (PassiveRecords) o;
        return date.equals(records.date) &&
                forceVal == records.forceVal&&
                tempVal == records.tempVal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, forceVal, tempVal);
    }

    public boolean dateEquals(Date r){
        return date.equals(r) ;
    }
}
