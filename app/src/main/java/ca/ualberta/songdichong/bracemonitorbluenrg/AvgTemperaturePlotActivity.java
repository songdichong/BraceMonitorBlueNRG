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


public class AvgTemperaturePlotActivity extends Activity {
    Map<TimeWithinDay, List<Float>> DailyTemperatures = new HashMap<>();
    double max = 0.0;
    List<Date> xAxis = new ArrayList<>();
    List<Float> yAxis = new ArrayList<>();
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
        legendTitles.add("Daily Average Temperature Values");
        TimeSeries series = new TimeSeries(legendTitles.get(0));
        for (int i = 0; i < xAxis.size();i++){
            series.add(xAxis.get(i),yAxis.get(i));
        }
        dataset.addSeries(series);

        return dataset;
    }

    public void myChartSettings(XYMultipleSeriesRenderer renderer) {
        renderer.setChartTitle("Daily Average Temperature Values");
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
        renderer.setYTitle("Average Temperature(°C)");
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

        Date startDate = new Date (startEndIndex[0] -1900 ,startEndIndex[1] - 1,startEndIndex[2],startEndIndex[3],startEndIndex[4]);
        Date endDate = new Date (startEndIndex[5]-1900 ,startEndIndex[6]-1,startEndIndex[7],startEndIndex[8],startEndIndex[9]);
        List<Records> recordsList = analyzer.getMyRecordsList();
        startTime= new Date (startEndIndex[0]-1900  ,startEndIndex[1] - 1,startEndIndex[2],0,0);
        endTime = new Date (startEndIndex[0] -1900 ,startEndIndex[1] - 1,startEndIndex[2]+1,0,0);
        for (int i = 0;i<recordsList.size();i++){
            if (!recordsList.get(i).isHeader) {
                NonHeaderRecords nonHeaderRecords = (NonHeaderRecords) recordsList.get(i);
                if (nonHeaderRecords.compareTo(startDate) >= 0 && nonHeaderRecords.compareTo(endDate) <= 0) {
                    List<Float> vals = DailyTemperatures.getOrDefault(new TimeWithinDay(nonHeaderRecords.getTime()),new ArrayList<Float>());
                    vals.add(nonHeaderRecords.getTempVal());
                    DailyTemperatures.put(new TimeWithinDay(nonHeaderRecords.getTime()),vals);
                }
            }
        }
        for (Map.Entry<TimeWithinDay, List<Float>> entry : DailyTemperatures.entrySet()) {
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
            for (Float mark : marks) {
                sum += mark;
            }
            return sum / marks.size();
        }
        return sum;
    }
}
