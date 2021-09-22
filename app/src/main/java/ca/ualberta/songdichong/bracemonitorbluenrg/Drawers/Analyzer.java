package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;
import java.util.List;
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

File Name          : Analyzer.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description   : This is the abstract class of a analyzer.

Data Structure:
                    Analyzer:   List<Days>: contains days information
                                List<Records>: contains all records information

                    Days:       List<Records>: contains records information for that specific day

                    Records(abstract)----!isHeader---->NonHeaderRecords(abstract)------->ActiveRecords
                       |                                                        |------->PassiveRecords
                      |----------------------isHeader---------------------------------->HeaderRecords
*/
public abstract class Analyzer {
    List<Days> myDaysList;
    List<Records> myRecordsList;
    int subjectNumber;
    int sampleRate;
    float targetForce;//Note that for passive one this is force, for active one this is pressure
    float targetTemperature = 25.0f;
    boolean corrupted = false;//If there is any data starts at year 2000, the data is corrupted
    String deviceName;
    public List<Days> getMyDaysList() {
        return myDaysList;
    }

    float calculateAvg(float[] arrays){
        float total = 0;
        for(int i=0; i<arrays.length; i++){
            total = total + arrays[i];
        }
        return total/arrays.length;
    }


    public double getTargetForce() {
        return targetForce;
    }
    public void setTargetForce(float targetForce) {
         this.targetForce = targetForce;
    }
    public double getTargetTemperature() {
        return targetTemperature;
    }
    public void setTargetTemperature(float targetTemperature) {
        this.targetTemperature = targetTemperature;
    }
    public List<Records> getMyRecordsList() {
        return myRecordsList;
    }
    public boolean isBlankData(){return corrupted;}

}
