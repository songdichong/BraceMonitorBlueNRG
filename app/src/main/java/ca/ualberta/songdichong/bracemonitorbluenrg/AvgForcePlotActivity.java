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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Analyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Days;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.ConfigureDrawerFragment;

public class AvgForcePlotActivity extends Activity  {
    Map<int[], List<Double>> DailyForces = new HashMap<>();
    double max = 0.0;
    List<Date> xAxis = new ArrayList<>();
    List<Double> yAxis = new ArrayList<>();
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
        Date startTime = new Date(2020,1,1);
        Date endTime = new Date(2020,1,2);
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
        AtomicInteger counter = new AtomicInteger();
        for (Days days:analyzer.getMyDaysList()){
            for (Records rec:days.getRecordsList())
            {
                if (counter.get() >=start && counter.get() <end)
                {
                    if (DailyForces.containsKey(rec.time))
                    {
                        List<Double> vals =  DailyForces.get(rec.time);
                        vals.add(rec.getForceVal());
                        DailyForces.put(rec.time,vals);
                    }
                    else{
                        Double force = rec.getForceVal();
                        List<Double> vals = new ArrayList<Double>();
                        vals.add(force);
                        DailyForces.put(rec.time,vals);
                    }
                }
                counter.getAndIncrement();
            }
        }
        Map<Date,  Double> sortedEntry = new TreeMap<>();
        for (Map.Entry<int[], List<Double>> entry : DailyForces.entrySet()) {
            Date dt = new Date(2020,1,1,entry.getKey()[0],entry.getKey()[1]);
            Double averageY = calculateAverage(entry.getValue());
            sortedEntry.put(dt,averageY);
        }
        for (Map.Entry<Date, Double> entry:sortedEntry.entrySet()){
            xAxis.add(entry.getKey());
            yAxis.add(entry.getValue());
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
