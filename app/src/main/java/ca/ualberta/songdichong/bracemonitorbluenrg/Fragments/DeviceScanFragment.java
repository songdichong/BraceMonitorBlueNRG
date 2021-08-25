package ca.ualberta.songdichong.bracemonitorbluenrg.Fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;

import ca.ualberta.songdichong.bracemonitorbluenrg.BluetoothLeService;
import ca.ualberta.songdichong.bracemonitorbluenrg.Constants;
import ca.ualberta.songdichong.bracemonitorbluenrg.MainActivity;
import ca.ualberta.songdichong.bracemonitorbluenrg.R;


@SuppressWarnings("deprecation")
public class DeviceScanFragment extends Fragment {
    ProgressBar progressBar;
    ListView bleDeviceListView;
    TextView scanRemainder;
    Button scanButton;
    Button cancelButton;
    String deviceName;
    BluetoothLeService mBluetoothLeService;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scan, container, false);
        progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        bleDeviceListView = v.findViewById(R.id.bluetooth_list);
        scanRemainder = v.findViewById(R.id.textView);
        scanButton = v.findViewById(R.id.scan_button);
        cancelButton = v.findViewById(R.id.cancel_button);
        mBluetoothLeService = BluetoothLeService.getmBluetoothLeService();
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).requestPermission();
                scanButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                mBluetoothLeService.scanBluetooth(true);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.INVISIBLE);
                scanButton.setEnabled(true);
                ((MainActivity)getActivity()).cancelConnectDialogue();
            }
        });

        bleDeviceListView.setAdapter(mBluetoothLeService.getmBluetoothListAdapter());
        bleDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                deviceName = bleDeviceListView.getAdapter().getItem(position).toString();
                mBluetoothLeService.initConnection(position);
                scanRemainder.setVisibility(View.INVISIBLE);

            }
        });

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DialogFragment dialogFragment = ((DialogFragment) getParentFragment());
                if (dialogFragment != null) {
                    dialogFragment.dismiss();
                }
            }
        }, 5000);

        IntentFilter filter = buildIntentFilter();
        getActivity().getApplicationContext().registerReceiver(updateReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getApplicationContext().unregisterReceiver(updateReceiver);
    }


    public IntentFilter buildIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_GATT_SCAN_COMPLETE);
        filter.addAction(Constants.ACTION_GATT_CONNECTED);
        filter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        filter.addAction(Constants.ACTION_BATTERY_READ);
        return filter;
    }


    public final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_GATT_SCAN_COMPLETE.equals(action)) {
                progressBar.setVisibility(View.INVISIBLE);
                if (!(BluetoothLeService.connected)) {
                    scanButton.setEnabled(true);
                }
            }
            else if (Constants.ACTION_GATT_CONNECTED.equals(action)){
                progressBar.setVisibility(View.INVISIBLE);
                scanButton.setEnabled(false);
                BluetoothLeService.deviceName = intent.getStringExtra(Constants.ACTION_GATT_CONNECTED);
            }
            else if (Constants.ACTION_BATTERY_READ.equals(action)) {
                byte[] batteryTemp = intent.getByteArrayExtra("batteryLevel");
            }
        }
    };
}
