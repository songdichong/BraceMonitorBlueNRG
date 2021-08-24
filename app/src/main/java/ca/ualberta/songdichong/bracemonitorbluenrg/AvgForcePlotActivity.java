package ca.ualberta.songdichong.bracemonitorbluenrg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Analyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Days;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.TimeWithinDay;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.ConfigureDrawerFragment;

public class AvgForcePlotActivity extends Activity  {
    double max = 0.0;
    List<Date> xAxis = new ArrayList<>();
    List<Double> yAxis = new ArrayList<>();
    Map<TimeWithinDay, List<Double>> DailyForces = new HashMap<>();
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
        legendTitles.add("Daily Average Force Values");
        TimeSeries series = new TimeSeries(legendTitles.get(0));
        for (int i = 0; i < xAxis.size();i++){
            series.add(xAxis.get(i),yAxis.get(i));
        }

        dataset.addSeries(series);
        return dataset;
    }

    public void myChartSettings(XYMultipleSeriesRenderer renderer) {
        renderer.setChartTitle("Daily Average Force Values");

        renderer.setXAxisMin(startTime.getTime());
        Log.v("start",String.valueOf(startTime.getTime()));
        renderer.setXAxisMax(endTime.getTime());
        Log.v("end",String.valueOf(endTime.getTime()));
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
        renderer.setYTitle("Average Force(N)");
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
        int start = startEndIndex[0];
        int end  = startEndIndex[1];
        List<Records> recordsList = analyzer.getMyRecordsList();
        for (int counter = start; counter < end; counter++){
            Records rec = recordsList.get(counter);
            if (!rec.isHeader){
                List<Double> vals =DailyForces.getOrDefault(new TimeWithinDay(rec.time),new ArrayList<Double>());
                vals.add(rec.getForceVal());
                DailyForces.put(new TimeWithinDay(rec.time),vals);
            }
        }
        startTime = new Date(recordsList.get(start).getYear()-1900,recordsList.get(start).getMonth()-1,recordsList.get(start).getDate());
        endTime = new Date(recordsList.get(start).getYear()-1900,recordsList.get(start).getMonth()-1,recordsList.get(start).getDate()+1);
        for (Map.Entry<TimeWithinDay, List<Double>> entry : DailyForces.entrySet()) {
            Date dt = new Date(recordsList.get(start).getYear()-1900,recordsList.get(start).getMonth()-1,
                    recordsList.get(start).getDate(),entry.getKey().getHour(),entry.getKey().getMin());
            Double averageY = calculateAverage(entry.getValue());
            xAxis.add(dt);
            yAxis.add(averageY);
        }
        for (Double y:yAxis){
            if (max<y) max = y;
        }
    }

    private double calculateAverage(List <Double> marks) {
        Double sum = 0.;
        if(!marks.isEmpty()) {
            for (Double mark : marks) {
                sum += mark;
            }
            return sum / marks.size();
        }
        return sum;
    }
}
