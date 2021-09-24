package ca.ualberta.songdichong.bracemonitorbluenrg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Analyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.NonHeaderRecords;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.PassiveAnalyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
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

Project Name       : Brace Monitor Android User Interface

File Name          : ForceTemperaturePlotActivity.java

Original Author    : Dichong Song

File Last Modification Date : 2021/09/16

File Description: This file creates an activity for viewing details of brace monitor's force/pressure and temperature compliance result
*/
public class ForceTemperaturePlotActivity extends Activity {
    int onCounter = 0;
    int noCounter = 0;
    double[] percentage = new double[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean shouldPlot = calPercentageData();
        if (shouldPlot){
            setContentView(R.layout.activity_plot_force);
            XYMultipleSeriesRenderer renderer = getTruitonRenderer();
            myChartSettings(renderer);
            Intent intent = ChartFactory.getBarChartIntent(this, getTruitonDataset(), renderer, BarChart.Type.DEFAULT);
            startActivity(intent);
        }else{
            runOnUiThread(new Runnable() {
                public void run() {
                    final Toast jam = Toast.makeText(getApplicationContext(), "No enough data", Toast.LENGTH_LONG);
                    jam.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            jam.cancel();
                        }
                    }, 2000);
                }
            });
        }
        finish();
    }

    public XYMultipleSeriesRenderer getTruitonRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(60);
        renderer.setChartTitleTextSize(80);
        renderer.setLabelsTextSize(50);
        renderer.setLegendTextSize(50);
        renderer.setShowLegend(true);
        renderer.setFitLegend(true);
        renderer.setBarWidth(200);
        renderer.setMargins(new int[] { 120, 130, 30, 0 });
        XYSeriesRenderer notWornRenderer = new XYSeriesRenderer();
        notWornRenderer.setColor(Color.rgb(255, 204, 0));
        notWornRenderer.setDisplayChartValues(true);
        notWornRenderer.setChartValuesTextSize(40);
        notWornRenderer.setChartValuesTextAlign(Paint.Align.RIGHT);
        renderer.addSeriesRenderer(notWornRenderer);
        XYSeriesRenderer wornRenderer = new XYSeriesRenderer();
        wornRenderer.setColor(Color.GREEN);
        wornRenderer.setDisplayChartValues(true);
        wornRenderer.setChartValuesTextSize(60);
        wornRenderer.setChartValuesTextAlign(Paint.Align.RIGHT);
        renderer.addSeriesRenderer(wornRenderer);
        return renderer;
    }


    public XYMultipleSeriesDataset getTruitonDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        ArrayList<String> legendTitles = new ArrayList<String>();
        legendTitles.add("Not Worn");
        legendTitles.add("Worn");

        for (int i = 0; i<2; i++){
            XYSeries series = new XYSeries(legendTitles.get(i));
            series.add(1,percentage[i]);
            dataset.addSeries(series);
        }

        return dataset;
    }

    public void myChartSettings(XYMultipleSeriesRenderer renderer) {
        if (ConfigureDrawerFragment.getAnalyzeMode() == 1) {
            renderer.setChartTitle("Pressure and Temperature Compliance");
        } else{
            renderer.setChartTitle("Force and Temperature Compliance");
        }
        renderer.setXAxisMin(0.9);
        renderer.setXAxisMax(1.1);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(110);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setBarSpacing(1);
        renderer.setYTitle("Compliance Percentage[%]");
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.GRAY);
        renderer.setXLabels(0); // sets the number of integer labels to appear
        renderer.setZoomEnabled(false, false);
        renderer.setPanEnabled(false, false);
        renderer.setDisplayValues(true);
    }

    private boolean calPercentageData(){
        Analyzer analyzer = ConfigureDrawerFragment.analyzer;
        List<Records> recordsList = analyzer.getMyRecordsList();
        if (recordsList.size() > 0){
            double forceReference = getIntent().getDoubleExtra("force",0);
            double temperatureReference = getIntent().getDoubleExtra("temperature",0);
            int[] startEndDateTime = getIntent().getIntArrayExtra("startEndIndex");
            Date startDate = new Date (startEndDateTime[0]-1900 ,startEndDateTime[1]-1,startEndDateTime[2],startEndDateTime[3],startEndDateTime[4]);
            Date endDate = new Date (startEndDateTime[5]-1900 ,startEndDateTime[6]-1,startEndDateTime[7],startEndDateTime[8],startEndDateTime[9]);
            int counter = 0;
            for (int i = 0;i<recordsList.size();i++){
                if (!recordsList.get(i).isHeader) {
                    NonHeaderRecords nonHeaderRecords = (NonHeaderRecords) recordsList.get(i);
                    if (nonHeaderRecords.compareTo(startDate) >= 0 && nonHeaderRecords.compareTo(endDate) <= 0){
                        float temperature =nonHeaderRecords.getAverageTempVal();
                        float force =nonHeaderRecords.getAverageForceVal();
                        if (temperature >= temperatureReference && force >= forceReference){
                            onCounter++;
                        }
                        else{
                            noCounter++;
                        }
                        counter++;
                    }
                }
            }
            if (counter > 0){
                percentage[0] = (double)noCounter/counter*100;
                percentage[1] = (double)onCounter/counter*100;
                for (int j =0;j<2;j++){
                    percentage[j] = Math.round(percentage[j] * 100.0) / 100.0;
                }
                return true;
            }
        }
        return false;
    }

}
