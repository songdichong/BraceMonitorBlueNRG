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

File Name          : ActiveAnalyzer.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description   : The class inherits from Analyzer. The analyzer object is used
                    to analyze the data recorded by the active brace monitor

Data Structure:
                    Analyzer:   List<Days>: contains days information
                                List<Records>: contains all records information

                    Days:       List<Records>: contains records information for that specific day

                    Records(abstract)----!isHeader---->NonHeaderRecords(abstract)------->ActiveRecords
                       |                                                        |------->PassiveRecords
                      |----------------------isHeader---------------------------------->HeaderRecords
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActiveAnalyzer extends Analyzer {
    //For active we use target +- allowance. Ex: target = 30, allowance = 10.
    // Then lower bound = 20, upper bound = 40
    private int allowance;
    List<Records> adjustmentRecordsList;
    //use the file in the directory to generate analyzer class
    public ActiveAnalyzer(File file){
        myDaysList = new ArrayList<>();
        myRecordsList = new ArrayList<>();
        targetForce = 30.0f;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            //first line device name
            String line = br.readLine();
            deviceName = line;
            List<Records> downloadedData = new ArrayList<>();
            line = br.readLine();
            //start with third line
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || line.split(",").length==0) continue;

                try{
                    String[] values = line.split("\\s*,\\s*");
                    float force = Float.parseFloat(values[0]);
                    float temperature = Float.parseFloat(values[1]);
                    int longTermFlag = Integer.parseInt(values[2]);
                    int year = Integer.parseInt(values[3]);
                    int month = Integer.parseInt(values[4]);
                    int day = Integer.parseInt(values[5]);
                    int hour = Integer.parseInt(values[6]);
                    int minute = Integer.parseInt(values[7]);
                    NonHeaderRecords myRecords = new ActiveRecords(year,month,day,hour,minute,force,temperature,longTermFlag);
                    if (myRecords.getDate() != null){
                        downloadedData.add(myRecords);
                    }
                    else {
                        corrupted = true;
                    }
                }
                //either NumberFormatException or ArrayIndexOutOfBoundsException
                catch (Exception e1){
                    try
                    {
                        String[] header = line.split("[\\s:,]+");
                        String subject_number_string = header[2];
                        String target_force_string = header[6];
                        String allowance_string = header[8];
                        String sample_rate_string = header[11];
                        subjectNumber = Integer.parseInt(subject_number_string);
                        targetForce = Float.parseFloat(target_force_string);
                        sampleRate = Integer.parseInt(sample_rate_string);
                        allowance = Integer.parseInt(allowance_string);
                        Records r = new HeaderRecords(subjectNumber, targetForce, sampleRate, allowance);
                        downloadedData.add(r);
                    }
                    catch (Exception e2)
                    {
                        break;
                    }
                }
            }
            formAnalyzer(downloadedData);
        }
        catch(IOException e){

        }
    }
    public int getAllowance() {
        return allowance;
    }

    private void formAnalyzer(List<Records> downloadedData)
    {
        myDaysList = new ArrayList<>();
        myRecordsList = new ArrayList<>();
        adjustmentRecordsList = new ArrayList<>();
        if (downloadedData.size() == 0)
        {
            return;
        }
        Days myDays = new Days(0, 0, 0);//create a dummy date
        for (int i = 0; i < downloadedData.size(); i++)
        {
            Records myRecords = downloadedData.get(i);
            myRecordsList.add(myRecords);
            if (!myRecords.isHeader)
            {
                NonHeaderRecords rec = (NonHeaderRecords)myRecords;
                if (!myDays.belongsTo(rec))
                {
                    myDays = new Days(rec.getDate().getYear()+1900, rec.getDate().getMonth()+1, rec.getDate().getDate());
                    if (myDays.date != null) {
                        myDaysList.add(myDays);
                    }
                }
                myDays.getRecordsList().add(rec);
                if ((rec.longTermFlag &= (0b11<<8))  != 0){
                    adjustmentRecordsList.add(rec);
                }
            }
            else
            {
                sampleRate = ((HeaderRecords)myRecords).sampleRate;
                subjectNumber = ((HeaderRecords)myRecords).subjectNumber;
                targetForce = ((HeaderRecords)myRecords).targetForce;
            }
        }
        if (myDaysList.size()==0 && myDays.date != null){
            myDaysList.add(myDays);
        }

        float[] averageF = new float[5];
        float[] averageT = new float[5];
        int counter = 0;
        for (Records record: myRecordsList){
            if (!record.isHeader){
                NonHeaderRecords rec = (NonHeaderRecords) record;
                averageF[counter%5] = rec.getForceVal();
                averageT[counter%5] = rec.getTempVal();
                if (counter == 5){
                    for (int i = 0; i < 5; i++)
                    {
                        rec.setAverageForceVal(calculateAvg(averageF));
                        rec.setAverageTempVal(calculateAvg(averageT));
                    }
                }
                if (counter > 5){
                    rec.setAverageForceVal(calculateAvg(averageF));
                    rec.setAverageTempVal(calculateAvg(averageT));
                }
                counter++;
            }
        }
    }
    public ActiveAnalyzer(List<Records> downloadedData){
        //first line must be Header+year
        myDaysList = new ArrayList<>();
        formAnalyzer(downloadedData);
    }

    public List<Records> getAdjustmentRecordsList() {
        return adjustmentRecordsList;
    }

}
