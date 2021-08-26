package ca.ualberta.songdichong.bracemonitorbluenrg.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.songdichong.bracemonitorbluenrg.BluetoothLeService;
import ca.ualberta.songdichong.bracemonitorbluenrg.Constants;
import ca.ualberta.songdichong.bracemonitorbluenrg.Drawers.Records;
import ca.ualberta.songdichong.bracemonitorbluenrg.MainActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.R;

public class OutPutDataFragment extends PreferenceFragment {
    BluetoothLeService mBluetoothLeService;
    TextView batteryText;
    TextView temperatureText;
    TextView deviceName;
    TextView versionText;
    TextView memoryText;

    private TextView mNumSamplesText;
    private List<Records> downloadedData = new ArrayList<>();

    private PopupWindow popupWindowDownloading;
    Thread currentThread = null;
    private Handler handler;

    double downloadedSize;
    double totalSize;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.output_preferences);
        mBluetoothLeService = BluetoothLeService.getmBluetoothLeService();

        Preference startFullDownload = findPreference("start_data_download");
        startFullDownload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                mBluetoothLeService.downloadData();
                return true;
            }
        });

        Preference startUploadData = findPreference("upload_data_email");
        startUploadData.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (isAdded()){
                    ((MainActivity)getActivity()).showUploadDialog();
                }
                return true;
            }
        });

        Preference erasePref = findPreference("erase_flash");
        if (mBluetoothLeService.getDeviceInfoVal() == Constants.internalFlashBraceMonitor){
            erasePref.setEnabled(false);
        }
        erasePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you sure you want to delete the log?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mBluetoothLeService.eraseAllFlash();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setTitle("Warning!")
                        .setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }

        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_preferences_layout, null);
        batteryText = (TextView) v.findViewById(R.id.battery_value);
        temperatureText = (TextView) v.findViewById(R.id.temperature_value);
        deviceName = (TextView) v.findViewById(R.id.device_name);
        versionText = (TextView) v.findViewById(R.id.version_value);
        memoryText = (TextView) v.findViewById(R.id.memory_value);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        batteryText.setText(String.format("%.2f",mBluetoothLeService.batteryVal) + "V");
        deviceName.setText(mBluetoothLeService.deviceName);
        versionText.setText("v"+mBluetoothLeService.getDeviceVersionVal());
        if (mBluetoothLeService.getTotalAddress() != Integer.MIN_VALUE) {
            double percentage = ((double)mBluetoothLeService.getTotalAddress() / 0x3FFFFF * 100);
            memoryText.setText(String.format("%.1f",percentage)+"%");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_TEMP_UPDATE);
        filter.addAction(Constants.ACTION_DATA_DOWNLOAD);
        filter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        filter.addAction(Constants.ACTION_BATTERY_READ);
        filter.addAction(Constants.ACTION_VERSION_UPDATE);
        filter.addAction(Constants.ACTION_DATA_ERASE);
        getActivity().getApplicationContext().registerReceiver(updateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getApplicationContext().unregisterReceiver(updateReceiver);
    }


    private class UpdateTextThread extends Thread {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (mNumSamplesText != null) {
                        totalSize = mBluetoothLeService.getTotalAddress();
                        if (totalSize == Integer.MIN_VALUE) {
                            mNumSamplesText.setText("Downloading..." + downloadedData.size());
                        }
                        else{
                            Log.v("downloadedSize",downloadedSize+" "+totalSize);
                            mNumSamplesText.setText("Downloading..." + (int)((downloadedSize/totalSize)*100) +" %");
                        }
                        if (handler != null) {
                            handler.removeCallbacksAndMessages(null);
                        }
                        handler = new Handler();
                        final Runnable r = new Runnable() {
                            public void run() {
                                if(popupWindowDownloading != null){
                                    popupWindowDownloading.dismiss();
                                }
                            }
                        };
                        handler.postDelayed(r, 3000);
                    }
                }
            });
        }
    }


    public void showDownloadingDialogue(String text) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.dialog_download, null, false);
        popupWindowDownloading = new PopupWindow(layout,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindowDownloading.setContentView(layout);
        popupWindowDownloading.setAnimationStyle(R.style.Animation);
        mNumSamplesText =  layout.findViewById(R.id.downloadText);
        mNumSamplesText.setText(text);
        popupWindowDownloading.showAtLocation(layout, Gravity.TOP, 0, 0);
    }


    public final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_TEMP_UPDATE.equals(action)) {
                double temperature = intent.getDoubleExtra(Constants.ACTION_TEMP_UPDATE,0);
                temperatureText.setText(String.format("%.1f",temperature) + "°C");
            }

            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (getActivity() != null) {
                    ((MainActivity)getActivity()).restart();
                }
            }

            else if (Constants.ACTION_BATTERY_READ.equals(action)) {
                double batt = intent.getDoubleExtra(Constants.ACTION_BATTERY_READ,0);
                batteryText.setText(String.format("%.2f",batt) + "V");
            }

            else if (Constants.ACTION_VERSION_UPDATE.equals(action)) {
                int[] array = intent.getIntArrayExtra(Constants.ACTION_VERSION_UPDATE);
                if (array!= null) {
                    int version = array[0];
                    int address = array[1];
                    versionText.setText("v"+version);
                    if (mBluetoothLeService.getTotalAddress() != Integer.MIN_VALUE) {
                        double percentage = ((double)address / 0x3FFFFF * 100);
                        memoryText.setText(String.format("%.1f",percentage)+"%");
                    }
                }
            }

            else if (Constants.ACTION_DATA_DOWNLOAD.equals(action)) {
                int length = intent.getIntExtra(Constants.ACTION_DATA_DOWNLOAD,0);
                downloadedSize += length;
                Log.v("downloadedSize",downloadedSize+"");
                if (getActivity() != null) {
                    downloadedData = mBluetoothLeService.downloadedData;
                    if(popupWindowDownloading == null){
                        showDownloadingDialogue("Downloading");
                    }
                    if(currentThread != null && currentThread.isAlive()) {
                        currentThread.interrupt();
                    }
                    currentThread = new UpdateTextThread();
                    currentThread.start();
                }
            }

            //When ACTION_DATA_ERASE of current braceMonitorDevice is received, determine how long should the
            //waiting dialogue should exists, then show this dialogue for x seconds.
            else if (Constants.ACTION_DATA_ERASE.equals(action)){
                if (getActivity() != null) {
                    final double timer = intent.getDoubleExtra(Constants.ACTION_DATA_ERASE,1);
                    CountDownTimer cdt = new CountDownTimer( (int)timer*1000,1000){
                        public void onTick(long millisUntilFinished){
                            if(popupWindowDownloading != null){
                                popupWindowDownloading.dismiss();
                            }
                            showDownloadingDialogue("Erasing... " + ((int)timer*1000 - millisUntilFinished) / 1000 + "/" + (int)timer);
                        }
                        public void onFinish() {
                            // DO something when time minute is up
                            if(popupWindowDownloading != null){
                                popupWindowDownloading.dismiss();
                            }
                        }
                    }.start();
                }
            }
        }
    };

}
