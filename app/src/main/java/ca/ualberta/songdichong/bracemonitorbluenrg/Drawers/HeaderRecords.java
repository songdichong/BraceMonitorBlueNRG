package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;
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

File Name          : HeaderRecords.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description   :  The class inherits from Records. The Header records subjectNumber, sampleRate,
                      targetForce (for active this value is actual pressure), allowance (active only)

Data Structure:
                    Analyzer:   List<Days>: contains days information
                                List<Records>: contains all records information

                    Days:       List<Records>: contains records information for that specific day

                    Records(abstract)----!isHeader---->NonHeaderRecords(abstract)------->ActiveRecords
                       |                                                        |------->PassiveRecords
                      |----------------------isHeader---------------------------------->HeaderRecords
*/
public class HeaderRecords extends Records {
    public int subjectNumber;
    public int sampleRate;
    public float targetForce;

    public int allowance;

    public HeaderRecords(int subjectNumber, float targetForce, int sampleRate){
        this.subjectNumber = subjectNumber;
        this.sampleRate = sampleRate;
        this.targetForce = targetForce;
        this.isHeader = true;
    }

    public HeaderRecords(int subjectNumber, float targetPressure, int sampleRate, int allowance){
        this.subjectNumber = subjectNumber;
        this.sampleRate = sampleRate;
        this.targetForce = targetPressure;
        this.allowance = allowance;
        this.isHeader = true;
    }

    @Override
    public String getString(boolean isActive)
    {
        if (isActive){
            return "Subject number: " + subjectNumber + " Target Pressure (mmHg): " + String.format("%.0f",targetForce) +
                    " Allowance: " + allowance + " Sample rate: " + sampleRate + "\n";
        }else {
            return "Subject number: " + subjectNumber + " Target Force (N): " + String.format("%.2f",targetForce) + " Sample rate: " + sampleRate + "\n";
        }
    }
}
