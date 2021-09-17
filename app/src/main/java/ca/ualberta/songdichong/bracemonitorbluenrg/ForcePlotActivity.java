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


public class ForcePlotActivity extends Activity  {
    int noCounter = 0;
    int belowCounter = 0;
    int onCounter = 0;
    int overCounter = 0;
    Double[] percentage = new Double[4];
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

    public XYMultipleSeriesDataset getTruitonDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        ArrayList<String> legendTitles = new ArrayList<String>();
        legendTitles.add("Not Worn");
        legendTitles.add("Below Target");
        legendTitles.add("Within Target");
        legendTitles.add("Above Target");

        for (int i = 0; i<4; i++){
            XYSeries series = new XYSeries(legendTitles.get(i));
            series.add(1,percentage[i]);
            dataset.addSeries(series);
        }

        return dataset;
    }

    public void myChartSettings(XYMultipleSeriesRenderer renderer) {
        if (ConfigureDrawerFragment.getAnalyzeMode() == 1) {
            renderer.setChartTitle("Pressure Compliance Measurement");
        }else{
            renderer.setChartTitle("Force Compliance Measurement");
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

    public XYMultipleSeriesRenderer getTruitonRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(60);
        renderer.setChartTitleTextSize(80);
        renderer.setLabelsTextSize(50);
        renderer.setShowLegend(true);
        renderer.setLegendTextSize(50);
        renderer.setFitLegend(true);
        renderer.setBarWidth(200);
        //top,left,bot,right
        renderer.setMargins(new int[] { 120, 130, 30, 0 });
        XYSeriesRenderer notWornRenderer = new XYSeriesRenderer();
        //light blue = 0,255,255
        notWornRenderer.setColor(Color.rgb(0, 255, 255));
        notWornRenderer.setDisplayChartValues(true);
        notWornRenderer.setChartValuesTextSize(60);
        notWornRenderer.setChartValuesTextAlign(Paint.Align.RIGHT);
        XYSeriesRenderer belowRenderer = new XYSeriesRenderer();
        //dark yellow = 255,204,0
        belowRenderer.setColor(Color.rgb(255, 204, 0));
        belowRenderer.setDisplayChartValues(true);
        belowRenderer.setChartValuesTextSize(60);
        belowRenderer.setChartValuesTextAlign(Paint.Align.RIGHT);
        XYSeriesRenderer withinRenderer = new XYSeriesRenderer();
        withinRenderer.setColor(Color.GREEN);
        withinRenderer.setDisplayChartValues(true);
        withinRenderer.setChartValuesTextSize(60);
        withinRenderer.setChartValuesTextAlign(Paint.Align.RIGHT);
        XYSeriesRenderer overRenderer = new XYSeriesRenderer();
        overRenderer.setColor(Color.RED);
        overRenderer.setDisplayChartValues(true);
        overRenderer.setChartValuesTextSize(60);
        overRenderer.setChartValuesTextAlign(Paint.Align.RIGHT);

        renderer.addSeriesRenderer(notWornRenderer);
        renderer.addSeriesRenderer(belowRenderer);
        renderer.addSeriesRenderer(withinRenderer);
        renderer.addSeriesRenderer(overRenderer);
        return renderer;
    }

    private boolean calPercentageData() {
        Analyzer analyzer = ConfigureDrawerFragment.analyzer;
        List<Records> recordsList = analyzer.getMyRecordsList();
        if (recordsList.size() > 0) {
            double forceReference = getIntent().getDoubleExtra("force", 0);
            int[] startEndIndex = getIntent().getIntArrayExtra("startEndIndex");
            Date startDate = new Date(startEndIndex[0]-1900 , startEndIndex[1]-1, startEndIndex[2], startEndIndex[3], startEndIndex[4]);
            Date endDate = new Date(startEndIndex[5]-1900 , startEndIndex[6]-1, startEndIndex[7], startEndIndex[8], startEndIndex[9]);
            int counter = 0;
            for (int i = 0; i < recordsList.size(); i++) {
                if (!recordsList.get(i).isHeader) {
                    NonHeaderRecords nonHeaderRecords = (NonHeaderRecords) recordsList.get(i);
                    if (nonHeaderRecords.compareTo(startDate) >= 0 && nonHeaderRecords.compareTo(endDate) <= 0) {
                        float force = nonHeaderRecords.getAverageForceVal();
                        if (force <= 0.1 * forceReference) {
                            noCounter++;
                        } else if ((force <= 0.8 * forceReference) && (0.1 * forceReference < force)) {
                            belowCounter++;
                        } else if ((force <= 1.2 * forceReference) && (0.8 * forceReference < force)) {
                            onCounter++;
                        } else {
                            overCounter++;
                        }
                        counter++;
                    }
                }
            }
            if (counter > 0) {
                percentage[0] = (double) noCounter / counter * 100;
                percentage[1] = (double) belowCounter / counter * 100;
                percentage[2] = (double) onCounter / counter * 100;
                percentage[3] = (double) overCounter / counter * 100;
                for (int j = 0; j < 4; j++) {
                    percentage[j] = Math.round(percentage[j] * 100.0) / 100.0;
                }
                return true;
            }
        }

        return false;
    }

}
