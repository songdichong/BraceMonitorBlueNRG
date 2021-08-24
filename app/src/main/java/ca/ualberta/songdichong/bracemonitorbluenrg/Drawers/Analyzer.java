package ca.ualberta.songdichong.bracemonitorbluenrg.Drawers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author songdichong
 *
 * @object Analyzer: analyze the given file or downloaded data and get analyzer object in order to draw graph
 *
 * @constructor     1.downloadedData: List<String[]> from BluetoothLeService.
 *             or:  2.file: txt file where data are recorded
 *
 * @infomation List<Days>
 *
 * @usage ConfigDrawerFragment
 */
public class Analyzer {
    List<Days> myDaysList  = new ArrayList<>();
    List<Records> myRecordsList = new ArrayList<>();
    private int subjectNumber;
    private int sampleRate;
    private double targetForce;
    private String deviceName;
    //use the file in the directory to generate analyzer class
    public Analyzer(File file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            //first line device name
            String line = br.readLine();
            deviceName = line;
            List<Records> downloadedData = new ArrayList<>();
            int year = 0;
            int month = 0;
            int day = 0;
            int hour;
            int minute;
            double force;
            double temperature;
            Days myDays = new Days(year,month,day);
            Records myRecords;
            line = br.readLine();
            //start with third line
            while ((line = br.readLine()) != null) {
                Log.v("line",line);
                if (line.isEmpty()) continue;
                try{
                    String[] values = line.split(", ");

                    force = Double.valueOf(values[0]);
                    temperature = Double.valueOf(values[1]);
                    year = Integer.valueOf(values[2]);
                    month = Integer.valueOf(values[3]);
                    day = Integer.valueOf(values[4]);
                    hour = Integer.valueOf(values[5]);
                    minute = Integer.valueOf(values[6]);
                    myRecords = new Records(year,month,day,hour,minute,force,temperature);
                    downloadedData.add(myRecords);
                }
                //either NumberFormatException or ArrayIndexOutOfBoundsException
                catch (Exception e1){
                    String[] header = line.split("[\\s:]+");
                    Log.v("header", Arrays.toString(header));
                    try
                    {
                        header = line.split("[\\s:]+");
                        String subject_number_string = header[2];
                        String target_force_string = header[6];
                        String sample_rate_string = header[9];
                        subjectNumber = Integer.valueOf(subject_number_string);
                        targetForce = Double.valueOf(target_force_string);
                        sampleRate = Integer.valueOf(sample_rate_string);
                        Records r = new Records(subjectNumber, targetForce, sampleRate);
                        downloadedData.add(r);
                    }
                    catch (Exception e2)
                    {
                        Log.v("e2",e2.getMessage());
                        break;
                    }
                }
            }
            formAnalyzer(downloadedData);
        }
        catch(IOException e){

        }
    }

    private void formAnalyzer(List<Records> downloadedData)
    {
        if (downloadedData.size() == 0)
        {
            return;
        }
        int year = 0;
        int month = 0;
        int day = 0;
        int oldDay = day;
        Days myDays = new Days(year, month, day);
        for (int i = 0; i < downloadedData.size(); i++)
        {
            Records myRecords = downloadedData.get(i);
            myRecordsList.add(myRecords);
            if (!myRecords.isHeader)
            {
                day = myRecords.date;
                month = myRecords.month;
                year = myRecords.year;
                //create new day
                if (oldDay != day)
                {
                    myDays = new Days(year, month, day);
                    myDaysList.add(myDays);
                    //check month
                    Log.v("days", year+"-"+month+"-"+day);
                    oldDay = day;
                }
                myDays.getRecordsList().add(myRecords);
            }
            else
            {
                sampleRate = myRecords.sampleRate;
                subjectNumber = myRecords.subjectNumber;
                targetForce = myRecords.targetForce;
            }
        }
        if (myDaysList.size()==0){
            myDaysList.add(myDays);
        }

        double[] averageF = new double[5];
        double[] averageT = new double[5];
        int counter = 0;
        for (Days days:myDaysList){
            for (Records rec:days.getRecordsList()){
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
    public List<Days> getMyDaysList() {
        return myDaysList;
    }

    private double calculateAvg(double[] arrays){
        double total = 0;
        for(int i=0; i<arrays.length; i++){
            total = total + arrays[i];
        }
        return total/arrays.length;
    }


    public Days getDaysFromDaysList(int year, int month, int day){
        for (Days days:myDaysList){
            if (days.isEqual(year,month,day)){
                return days;
            }
        }
        return null;
    }

    public int[] getStartEndIndex(int[] startTime, int[] endTime){
        int[] result = new int[]{0,0};
        int counter = 0;
        Records startRecord = new Records(startTime[0],startTime[1],startTime[2],startTime[3],startTime[4],0,0);
        Records endRecord = new Records(endTime[0],endTime[1],endTime[2],endTime[3],endTime[4],0,0);
        for (Records rec: myRecordsList){
            if (!rec.isHeader) {
                if (rec.dateEquals(startRecord)){
                    result[0] = counter;
                }
                if (rec.dateEquals(endRecord)){
                    result[1] = counter;
                    break;
                }
            }
            counter++;
        }
        Log.v("start",Arrays.toString(startTime));
        Log.v("end",Arrays.toString(endTime));
        Log.v("counter",counter+"");
        return result;
    }

    //use the downloaded data object to generate analyzer class
    public Analyzer(List<Records> downloadedData){
        //first line must be Header+year
        formAnalyzer(downloadedData);
    }

    public double getTargetForce() {
        return targetForce;
    }

    public List<Records> getMyRecordsList() {
        return myRecordsList;
    }
}
