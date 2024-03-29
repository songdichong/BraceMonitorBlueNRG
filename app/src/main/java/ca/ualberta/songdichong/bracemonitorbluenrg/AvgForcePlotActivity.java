package ca.ualberta.songdichong.bracemonitorbluenrg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;

import org.achartengine.ChartFactory;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Analyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.NonHeaderRecords;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.PassiveAnalyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.TimeWithinDay;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.ConfigureDrawerFragment;
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

File Name          : AvgForcePlotActivity.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description: This file creates an activity for viewing details of brace monitor's average force/pressure within a day
*/
public class AvgForcePlotActivity extends Activity  {
    double max = 0.0;
    List<Date> xAxis = new ArrayList<>();
    List<Float> yAxis = new ArrayList<>();
    Map<TimeWithinDay, List<Float>> DailyForces = new HashMap<>();
    Date startTime;
    Date endTime;
    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calLinePlotData();
        setContentView(R.layout.activity_plot_force);
        XYMultipleSeriesRenderer renderer = getTruitonRenderer();
        myChartSettings(renderer);
        Intent intent = ChartFactory.getLineChartIntent(getBaseContext(), getTruitonDataset(), renderer);
        startActivity(intent);
        finish();
    }

    public XYMultipleSeriesDataset getTruitonDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        ArrayList<String> legendTitles = new ArrayList<String>();
        if (ConfigureDrawerFragment.getAnalyzeMode() == 1){
            legendTitles.add("Daily Average Pressure Values");
        } else{
            legendTitles.add("Daily Average Force Values");
        }

        TimeSeries series = new TimeSeries(legendTitles.get(0));
        for (int i = 0; i < xAxis.size();i++){
            series.add(xAxis.get(i),yAxis.get(i));
        }

        dataset.addSeries(series);
        return dataset;
    }

    public void myChartSettings(XYMultipleSeriesRenderer renderer) {
        if (ConfigureDrawerFragment.getAnalyzeMode() == 1){
            renderer.setChartTitle("Daily Average Pressure Values");
            renderer.setYTitle("Average Pressure(mmHg)");
        } else{
            renderer.setChartTitle("Daily Average Force Values");
            renderer.setYTitle("Average Force(N)");
        }
        renderer.setXAxisMin(startTime.getTime());
        renderer.setXAxisMax(endTime.getTime());
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(max+0.5);
        for (int i = 0;i<=24*60*60*1000;i+=240*60*1000){
            int hour = i/(3600*1000);
            String labelHour = String.valueOf(hour);
            if (hour<10){
                labelHour = "0"+labelHour;
            }
            renderer.addXTextLabel(startTime.getTime()+i,labelHour);
        }

        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setXTitle("Time of Day(HH)");
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GRAY);
        renderer.setXLabels(0); // sets the number of integer labels to appear
        renderer.setZoomEnabled(false, false);
        renderer.setPanEnabled(true, true);
        renderer.setPanLimits(new double[]{startTime.getTime(),endTime.getTime(),0,max+1});
    }

    public XYMultipleSeriesRenderer getTruitonRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(60);
        renderer.setChartTitleTextSize(80);
        renderer.setLabelsTextSize(50);
        renderer.setShowLegend(false);
        renderer.setMargins(new int[] { 120, 130, 20, 80 });
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(Color.BLUE);
        r.setLineWidth(5);
        renderer.addSeriesRenderer(r);

        return renderer;
    }

    private void calLinePlotData(){
        Analyzer analyzer = ConfigureDrawerFragment.analyzer;
        int[] startEndIndex = getIntent().getIntArrayExtra("startEndIndex");

        Date startDate = new Date (startEndIndex[0]-1900 ,startEndIndex[1] - 1,startEndIndex[2],startEndIndex[3],startEndIndex[4]);
        Date endDate = new Date (startEndIndex[5]-1900 ,startEndIndex[6]-1,startEndIndex[7],startEndIndex[8],startEndIndex[9]);
        startTime= new Date (startEndIndex[0] -1900 ,startEndIndex[1] - 1,startEndIndex[2],0,0);
        endTime = new Date (startEndIndex[0]-1900  ,startEndIndex[1] - 1,startEndIndex[2]+1,0,0);
        List<Records> recordsList = analyzer.getMyRecordsList();
        for (int i = 0;i<recordsList.size();i++){
            if (!recordsList.get(i).isHeader) {
                NonHeaderRecords nonHeaderRecords = (NonHeaderRecords) recordsList.get(i);
                if (nonHeaderRecords.compareTo(startDate) >= 0 && nonHeaderRecords.compareTo(endDate) <= 0) {
                    List<Float> vals = DailyForces.getOrDefault(new TimeWithinDay(nonHeaderRecords.getTime()),new ArrayList<Float>());
                    vals.add(nonHeaderRecords.getForceVal());
                    DailyForces.put(new TimeWithinDay(nonHeaderRecords.getTime()),vals);
                }
            }
        }
        for (Map.Entry<TimeWithinDay, List<Float>> entry : DailyForces.entrySet()) {
            Date dt = new Date(startEndIndex[0]-1900,startEndIndex[1]-1,startEndIndex[2],entry.getKey().getHour(),entry.getKey().getMin());
            float averageY = calculateAverage(entry.getValue());
            xAxis.add(dt);
            yAxis.add(averageY);
        }
        for (Float y:yAxis){
            if (max<y) max = y;
        }
    }

    private float calculateAverage(List <Float> marks) {
        float sum = 0;
        if(!marks.isEmpty()) {
            for (float mark : marks) {
                sum += mark;
            }
            return sum / marks.size();
        }
        return sum;
    }
}
