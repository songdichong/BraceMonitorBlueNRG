package ca.ualberta.songdichong.bracemonitorbluenrg.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import ca.ualberta.songdichong.bracemonitorbluenrg.BluetoothLeService;
import ca.ualberta.songdichong.bracemonitorbluenrg.Constants;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import ca.ualberta.songdichong.bracemonitorbluenrg.MainActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.R;

import static ca.ualberta.songdichong.bracemonitorbluenrg.Constants.FILENAME;


@SuppressWarnings("deprecation")
public class RealTimePlotFragment extends Fragment {
    View rootView;
    BluetoothLeService mBluetoothLeService;
    // redraws a plot whenever an update is received:
    private class MyPlotUpdater implements Observer {
        Plot plot;
        public MyPlotUpdater(Plot plot) {
            this.plot = plot;
        }
        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }
    private XYPlot dynamicPlot;
    private MyPlotUpdater plotUpdater;
    private SampleDynamicXYDatasource data;
    private List<RealTimeData> realTimeDataList = new ArrayList<>();
    Button setTargetForce;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBluetoothLeService = BluetoothLeService.getmBluetoothLeService();
        rootView = inflater.inflate(R.layout.plot_fragment_layout, container, false);
        final Button startSampling = rootView.findViewById(R.id.start_sampling);
        final Button stopSampling = rootView.findViewById(R.id.stop_sampling);
        setTargetForce  = rootView.findViewById(R.id.set_target);
        final Button saveRealTimeDataButton = rootView.findViewById(R.id.save_real_time_data);
        if (BluetoothLeService.calibrated && BluetoothLeService.connected) {
            startSampling.setEnabled(true);
            stopSampling.setEnabled(true);
        } else {
            startSampling.setEnabled(false);
            stopSampling.setEnabled(false);
        }
        setTargetForce.setEnabled(BluetoothLeService.realTimeSampling);
        startSampling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.updateForceSampling(true);
                BluetoothLeService.realTimeSampling = true;
                setTargetForce.setEnabled(BluetoothLeService.realTimeSampling);
            }
        });
        stopSampling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.updateForceSampling(false);
                BluetoothLeService.realTimeSampling = false;
                setTargetForce.setEnabled(BluetoothLeService.realTimeSampling);
            }
        });

        saveRealTimeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUploadDialog();
            }
        });
        dynamicPlot = (XYPlot) rootView.findViewById(R.id.dynamicXYPlot);
        plotUpdater = new MyPlotUpdater(dynamicPlot);
        int yMin = 0;
        int yMax = 5;
        if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor){
            yMax = 100;
        }
        int xMax = 600;
        // only display whole numbers in domain labels
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));

        // getInstance and position datasets:
        data = new SampleDynamicXYDatasource(xMax);
        SampleDynamicSeries forceSeries = new SampleDynamicSeries(data, "Force (N)");
        if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor){
            forceSeries = new SampleDynamicSeries(data, "Pressure (mmHg)");
        }
        // hook up the plotUpdater to the data model:
        data.addObserver(plotUpdater);

        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                getResources().getColor(R.color.orange_color), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(4);
        dynamicPlot.addSeries(forceSeries, formatter1);

        dynamicPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        //Domain step = 7 - 1. 100 each step,
        dynamicPlot.setDomainStepValue(7);
        dynamicPlot.setRangeStepMode(XYStepMode.SUBDIVIDE);
        //Range step = 6 - 1. 1 each step. If max changed to 10 then 2 each step.
        dynamicPlot.setRangeStepValue(6);
        dynamicPlot.setRangeValueFormat(new DecimalFormat("#.#"));
        // uncomment this line to freeze the range boundaries:
        dynamicPlot.setRangeBoundaries(yMin, yMax, BoundaryMode.FIXED);

        dynamicPlot.setDomainBoundaries(0, xMax, BoundaryMode.FIXED);
        dynamicPlot.setDomainLabel("Time (0.1s)");
        dynamicPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getBorderPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.setRangeLabel("Force (N)");
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getLegendWidget());
        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[]{PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout infoLayout = (LinearLayout) rootView.findViewById(R.id.plot_info_container);
                int currentVisibility = infoLayout.getVisibility();
                if (currentVisibility == View.INVISIBLE) {
                    infoLayout.setVisibility(View.VISIBLE);
                } else {
                    infoLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_FORCE_UPDATE);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        getActivity().getApplicationContext().registerReceiver(updateReceiver, intentFilter);
        if (mBluetoothLeService.adjustmentEnabled){
//            setTargetForce.setBackgroundResource(R.drawable.green_rectangle_button);
            setTargetForce.getBackground().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.green_color), PorterDuff.Mode.MULTIPLY));
        }else {
//            setTargetForce.setBackgroundResource(R.color.white_color);
            setTargetForce.getBackground().clearColorFilter();
        }
        setTargetForce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothLeService.getDeviceInfoVal()== Constants.activeBraceMonitor){
                    if (mBluetoothLeService.adjustmentEnabled){
                        mBluetoothLeService.adjustmentEnabled = false;
                        mBluetoothLeService.setTargetPressure(0xff,0xff);
//                        setTargetForce.setBackgroundResource(android.R.drawable.btn_default);
                        setTargetForce.getBackground().clearColorFilter();

                    }else {
                        showInputDialog();
                    }

                } else {
                    if (data.forceArray.length > 0){
                        mBluetoothLeService.setTargetForce(data.currentForce);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Do you want to save subject ID, target force and sample rate?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mBluetoothLeService.directControlUnit(3,true);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                                .setTitle("Save?")
                                .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_info));
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getApplicationContext().unregisterReceiver(updateReceiver);
    }

    public final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_FORCE_UPDATE.equals(action)) {
                double forceVal = intent.getDoubleExtra("forceVal",0);

                if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor){
                    data.updateDataSetActive(forceVal);
                }else {
                    data.updateDataSetPassive(forceVal);
                }
            }

            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).restart();
                }
            }
        }
    };

    class SampleDynamicXYDatasource {
        private final int SAMPLE_SIZE;
        private double[] forceArray;
        private int phase = 0;
        public double currentForce;
        private MyObservable notifier;

        {
            notifier = new MyObservable();
        }

        public SampleDynamicXYDatasource(int sampleSize) {
            SAMPLE_SIZE = sampleSize;
            forceArray = new double[SAMPLE_SIZE];
        }


        // encapsulates management of the observers watching this datasource for update events:
        class MyObservable extends Observable {
            @Override
            public void notifyObservers() {
                setChanged();
                super.notifyObservers();
            }
        }

        public void updateDataSetPassive(double forceValue) {
            if (getActivity() == null) {
                return;
            }
            double[] forceCalibration = mBluetoothLeService.getForceCalibration();
            double forceMeasurement;
            if (forceValue <= forceCalibration[1]) {
                forceMeasurement = (forceValue-forceCalibration[0]) / (forceCalibration[1] - forceCalibration[0]);
            }
            else if (forceValue <= forceCalibration[2]) {
                forceMeasurement = (forceValue-forceCalibration[1]) / (forceCalibration[2] - forceCalibration[1]) + 1;
            }
            else if (forceValue < forceCalibration[3]) {
                forceMeasurement = (forceValue-forceCalibration[2]) / (forceCalibration[3] - forceCalibration[2]) * 4 + 2;
            }
            else{
                forceMeasurement = (forceValue-forceCalibration[3]) / (forceCalibration[3] - forceCalibration[2]) * 4 + 6;
            }

            if (forceMeasurement<0) {forceMeasurement = 0;}

            if (phase == SAMPLE_SIZE) {
                for (int i = 0; i < SAMPLE_SIZE - 1; i++) {
                    forceArray[i] = forceArray[i + 1];
                }
                forceArray[SAMPLE_SIZE - 1] = forceMeasurement;
            } else {
                forceArray[phase] = forceMeasurement;
                phase++;
            }
            RealTimeData realTimeData = new RealTimeData(Calendar.getInstance().getTime(), forceMeasurement);
            realTimeDataList.add(realTimeData);
            currentForce = forceMeasurement;
            String pressureString = "Force [N]: " + new DecimalFormat("0.0").format(forceMeasurement);
            ((TextView) rootView.findViewById(R.id.force_value_popup)).setText(pressureString);
            notifier.notifyObservers();


            if (forceMeasurement > 5){
                dynamicPlot.setRangeBoundaries(0, 10, BoundaryMode.FIXED);
            }
        }


        public void updateDataSetActive(double adcVoltage){
            if (getActivity() == null) {
                return;
            }
            double[] forceCalibration = mBluetoothLeService.getForceCalibration();
            double slope = forceCalibration[0];
            double intercept = forceCalibration[1];
            double forceMeasurement = slope * adcVoltage + intercept;
            if (phase == SAMPLE_SIZE) {
                for (int i = 0; i < SAMPLE_SIZE - 1; i++) {
                    forceArray[i] = forceArray[i + 1];
                }
                forceArray[SAMPLE_SIZE - 1] = forceMeasurement;
            } else {
                forceArray[phase] = forceMeasurement;
                phase++;
            }
            RealTimeData realTimeData = new RealTimeData(Calendar.getInstance().getTime(), forceMeasurement);
            realTimeDataList.add(realTimeData);
            currentForce = forceMeasurement;
            String pressureString = "Pressure [mmHg]: " + new DecimalFormat("0.").format(forceMeasurement);
            ((TextView) rootView.findViewById(R.id.force_value_popup)).setText(pressureString);
            notifier.notifyObservers();

            if (forceMeasurement > 100){
                dynamicPlot.setRangeBoundaries(0, 200, BoundaryMode.FIXED);
            }
        }

        public int getItemCount() {
            return SAMPLE_SIZE;
        }

        public Number getX(int index) {
            if (index >= SAMPLE_SIZE) {
                throw new IllegalArgumentException();
            }
            return index;
        }

        public Number getY(int index) {
            return forceArray[index];
        }

        public void addObserver(Observer observer) {
            notifier.addObserver(observer);
        }

        public void removeObserver(Observer observer) {
            notifier.deleteObserver(observer);
        }

    }

    class SampleDynamicSeries implements XYSeries {
        private SampleDynamicXYDatasource datasource;
        private String title;

        public SampleDynamicSeries(SampleDynamicXYDatasource datasource, String title) {
            this.datasource = datasource;
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return datasource.getItemCount();
        }

        @Override
        public Number getX(int index) {
            return datasource.getX(index);
        }

        @Override
        public Number getY(int index) {
            return datasource.getY(index);
        }
    }

    class RealTimeData{
        Date currentTime;
        double force;
        RealTimeData(Date time, double f){
            currentTime = time;
            force = f;
        }
        @Override
        public String toString(){
            return currentTime.toString() + " " + String.format("%.2f",force);
        }
    }

    void showInputDialog(){
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.dialog_input_threshold, null, false);
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.Animation);
        final EditText firstInput = layout.findViewById(R.id.first_input);
        final EditText secondInput = layout.findViewById(R.id.second_input);
        Button button = layout.findViewById(R.id.confirm);
        firstInput.setHint("target pressure");
        secondInput.setHint("allowance");
        firstInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        secondInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    int targetPressure = Integer.parseInt(firstInput.getText().toString());
                    int allowance = Integer.parseInt(secondInput.getText().toString());
                    if (targetPressure >= 1 && allowance >= 1 && targetPressure-allowance>=0){
                        mBluetoothLeService.setTargetPressure(targetPressure,allowance);
                        mBluetoothLeService.adjustmentEnabled = true;
//                        setTargetForce.setBackgroundResource(R.drawable.green_rectangle_button);
                        setTargetForce.getBackground().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.green_color), PorterDuff.Mode.MULTIPLY));

                        popupWindow.dismiss();
                    }
                    else {
                        mBluetoothLeService.makeToast("pressure and allowance should >= 1, and pressure should be greater than allowance");
                    }
                }
                catch (Exception exception){
                    mBluetoothLeService.makeToast("input is not valid");
                }
            }
        });
        Button cancelButton = (Button) layout.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }

    public void showUploadDialog() {
        saveRealTimeData();
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.dialog_upload, null, false);
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setAnimationStyle(R.style.Animation);
        Button button = layout.findViewById(R.id.confirm);
        Button cancelButton =  layout.findViewById(R.id.cancel);
        final String currentTime = (String) DateFormat.format("yyyy-MM-dd hh:mm", new Date());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "RealTimeData " + currentTime);
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                    File attachment = new File(Environment.getExternalStorageDirectory(), Constants.REALTIMEDATAFILENAME );
                    if (!attachment.exists() || !attachment.canRead()) {
                        Toast.makeText(getActivity().getApplication(), "Attachment Error", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.v("uri: ",getActivity().getApplicationContext().getPackageName());
                        Uri uri = FileProvider.getUriForFile(getActivity().getApplicationContext(),"ca.ualberta.songdichong.bracemonitorbluenrg.fileprovider", attachment);
                        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity().getApplication().getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    attachment.deleteOnExit();
                } catch (Throwable t) {
                    Toast.makeText(getActivity().getApplicationContext(), "Request failed try again: " + t.toString(), Toast.LENGTH_LONG).show();
                }
                popupWindow.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }

    private void saveRealTimeData() {
        File file = new File(Environment.getExternalStorageDirectory(), Constants.REALTIMEDATAFILENAME);
        if (file.exists()) {
            deleteFile();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(("Brace Monitor Real Time Data: "+Calendar.getInstance().getTime().toString()+"\n").getBytes());
            out.write(("Time, Force(N)\n").getBytes());
            out.write('\n');
            for (int i = 0; i < realTimeDataList.size(); i++) {
                RealTimeData realTimeData = realTimeDataList.get(i);
                String output = realTimeData.toString();
                out.write(output.substring(1, output.length() - 1).getBytes());
                out.write('\n');
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), Constants.REALTIMEDATAFILENAME);
        if (file != null) {
            file.delete();
        }
    }
}
