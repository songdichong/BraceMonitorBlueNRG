package ca.ualberta.songdichong.bracemonitorbluenrg.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import ca.ualberta.songdichong.bracemonitorbluenrg.BluetoothLeService;
import ca.ualberta.songdichong.bracemonitorbluenrg.Constants;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.LinearRegression;
import ca.ualberta.songdichong.bracemonitorbluenrg.MainActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.R;

public class CalibrationFragment  extends Fragment {
    private BluetoothLeService mBluetoothLeService;
    Button addCalibrationButton;
    Button deleteCalibrationButton;
    Button calculateCalibrationButton;
    ListView calibrationStatusListView;
    TextView calibrationFormulaTextView;
    ArrayAdapter calibrationListAdapter;
    List<double[]> calibrationValList = new ArrayList<>();
    List<String> calibrationStringList = new ArrayList<>();
    static double slope = Double.NaN;
    static double intercept = Double.NaN;
    static double r2 = Double.NaN;
    double currentForceMeasurement = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothLeService = BluetoothLeService.getmBluetoothLeService();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calibration, container, false);
        addCalibrationButton = rootView.findViewById(R.id.add_cali_button);
        deleteCalibrationButton = rootView.findViewById(R.id.delete_cali_button);
        calculateCalibrationButton = rootView.findViewById(R.id.calculate_cali_button);
        calibrationStatusListView = rootView.findViewById(R.id.calibration_list);
        calibrationFormulaTextView = rootView.findViewById(R.id.calibration_formula_textview);
        calibrationListAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,calibrationStringList);
        calibrationStatusListView.setAdapter(calibrationListAdapter);

        addCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumInput();
            }
        });
        deleteCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calibrationValList.size() > 0 && calibrationStringList.size() > 0){
                    calibrationValList.remove(calibrationValList.size()-1);
                    calibrationStringList.remove(calibrationStringList.size()-1);
                    calibrationListAdapter.notifyDataSetChanged();
                }
            }
        });

        calculateCalibrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double[] voltageCoordinates = new double[calibrationValList.size()];
                double[] pressureCoordinates = new double[calibrationValList.size()];
                for (int i = 0; i < calibrationValList.size(); i++) {
                    voltageCoordinates[i] = calibrationValList.get(i)[0];
                    pressureCoordinates[i] = calibrationValList.get(i)[1];
                }
                LinearRegression linearRegression = new LinearRegression(voltageCoordinates,pressureCoordinates);
                slope = linearRegression.slope();
                intercept = linearRegression.intercept();
                r2 = linearRegression.R2();
                showFormulaText();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                if (!Double.isNaN(slope) && !Double.isNaN(intercept)){
                    builder.setMessage("Valid slope and intercept have been computed. Do you want to save them to the monitor?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                   mBluetoothLeService.setActiveCalibrationValue(slope, intercept);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }})
                            .setTitle("Save?")
                            .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothLeService.updateForceSampling(true);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        filter.addAction(Constants.ACTION_FORCE_UPDATE);
        slope = mBluetoothLeService.getForceCalibration()[0];
        intercept = mBluetoothLeService.getForceCalibration()[1];
        showFormulaText();
        getActivity().getApplicationContext().registerReceiver(updateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBluetoothLeService.updateForceSampling(false);

        getActivity().getApplicationContext().unregisterReceiver(updateReceiver);
    }

    public final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_FORCE_UPDATE.equals(action)) {
                if (mBluetoothLeService.getDeviceInfoVal() == Constants.activeBraceMonitor) {
                    currentForceMeasurement = intent.getDoubleExtra(Constants.ACTION_FORCE_UPDATE,0);
                }
            }

            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).restart();
                }
            }
        }
    };
    private void showFormulaText(){
        StringBuilder text = new StringBuilder("P = ");
        text.append(String.format("%.2f",slope)  + " * V ");
        if (intercept >= 0){
            text.append("+ ");
        } else {
            text.append("- ");
        }
        text.append(String.format("%.2f",Math.abs(intercept)) + "\n");
        text.append("R²: "+String.format("%.4f",r2));
        calibrationFormulaTextView.setText(text);
    }

    private void showNumInput(){
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.number_input_dialog, null, false);
        Button button = layout.findViewById(R.id.confirm);
        final PopupWindow popupWindow = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.setContentView(layout);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.Animation);
        final EditText numberInput =  layout.findViewById(R.id.numberInput);
        numberInput.setHint("Real-world pressure (mmHg)");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    double pass = Double.parseDouble(numberInput.getText().toString()) ;
                    double[] cali = new double[2];
                    cali[0] = currentForceMeasurement;
                    cali[1] = pass;
                    String calibrationString = "ADC Voltage: " + cali[0] + " V. Calibration Val: " + cali[1] + " mmHg.";

                    calibrationValList.add(cali);
                    calibrationStringList.add(calibrationString);
                    calibrationListAdapter.notifyDataSetChanged();
                }
                catch (Exception e){
                    mBluetoothLeService.makeToast("Unknown error happens!");
                }
                popupWindow.dismiss();
            }
        });
        Button cancelButton = layout.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        popupWindow.showAtLocation(layout, Gravity.TOP, 0, 0);
    }
}
