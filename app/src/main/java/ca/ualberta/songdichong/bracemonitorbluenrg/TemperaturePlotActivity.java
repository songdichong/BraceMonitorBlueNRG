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
import java.util.List;
import java.util.Map;

import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Analyzer;
import ca.ualberta.songdichong.bracemonitorbluenrg.Fragments.ConfigureDrawerFragment;

public class TemperaturePlotActivity extends Activity {
    int noCounter = 0;
    int onCounter = 0;
    Double[] percentage = new Double[2];
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
        legendTitles.add("Worn");

        for (int i = 0; i<2; i++){
            XYSeries series = new XYSeries(legendTitles.get(i));
            series.add(1,percentage[i]);
            dataset.addSeries(series);
        }

        return dataset;
    }

    public void myChartSettings(XYMultipleSeriesRenderer renderer) {
        renderer.setChartTitle("Temperature Compliance Measurement");
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

    private boolean calPercentageData(){
        Analyzer analyzer = ConfigureDrawerFragment.analyzer;
        Map<int[],Double> averageTemperature = analyzer.getAverageTemperature();
        List<Double> temperartures = new ArrayList<Double>(averageTemperature.values());
        if (temperartures.size() > 0){
            double temperatureReference = getIntent().getDoubleExtra("temperature",0);
            int[] startEndIndex = getIntent().getIntArrayExtra("startEndIndex");
            int start = 0;
            int end  = 0;
            if (startEndIndex != null){
                start = startEndIndex[0];
                end  = startEndIndex[1];
            }

            for (int i = start;i<end;i++){
                Double temperature = temperartures.get(i);
                if (temperature < temperatureReference){
                    noCounter++;
                }
                else{
                    onCounter++;
                }
            }
            percentage[0] = (double)noCounter/(end-start)*100;
            percentage[1] = (double)onCounter/(end-start)*100;
            for (int j =0;j<2;j++){
                percentage[j] = Math.round(percentage[j] * 100.0) / 100.0;
            }
            return true;
        }
        return false;
    }
}