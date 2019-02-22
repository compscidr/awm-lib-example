package io.rightmesh.awm_lib_example;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.anadeainc.rxbus.Bus;
import com.anadeainc.rxbus.BusProvider;
import com.anadeainc.rxbus.Subscribe;

import io.rightmesh.awm.AndroidWirelessStatsCollector;
import io.rightmesh.awm.loggers.LogEvent;
import io.rightmesh.awm.stats.BluetoothStats;
import io.rightmesh.awm.stats.GPSStats;
import io.rightmesh.awm.stats.WiFiStats;

public class MainActivity extends AppCompatActivity {

    private AndroidWirelessStatsCollector awsc;
    protected Bus eventBus = BusProvider.getInstance();
    private TextView txtBtDevices;
    private TextView txtWifiDevices;
    private TextView txtGPS;
    private TextView txtSavedRecords;
    private TextView txtUploadedRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        awsc = new AndroidWirelessStatsCollector(this, false, false);
        awsc.start();

        txtBtDevices = findViewById(R.id.btDevices);
        txtWifiDevices = findViewById(R.id.wifiDevices);
        txtGPS = findViewById(R.id.gpsCoords);
        txtSavedRecords = findViewById(R.id.txtRecords);
        txtUploadedRecords = findViewById(R.id.txtUploadedRecords);
        eventBus.register(this);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int uploadedRecords = sharedPref.getInt("uploadedRecords", 0);
        txtUploadedRecords.setText("Uploaded Records: " + uploadedRecords);

        int savedRecords = awsc.getSavedRecordCount();
        Log.d("MA", "SAVED COUNT: " + savedRecords);
        txtSavedRecords.setText("Saved Records: " + savedRecords);
    }

    @Subscribe
    public void updateBTDevices(BluetoothStats btStats) {
        String status = "btDevices: ";
        status = status + btStats.getMacs().size();
        for(String mac : btStats.getMacs()) {
            status = status + "\n" + mac;
        }
        txtBtDevices.setText(status);
    }

    @Subscribe
    public void updateWiFiDevices(WiFiStats wifiStats) {
        String status = "wifiDevices: ";
        status = status + wifiStats.getMacs().size();
        for(String mac : wifiStats.getMacs()) {
            status = status + "\n" + mac;
        }
        txtWifiDevices.setText(status);
    }

    @Subscribe
    public void updateGPS(GPSStats gpsStats) {
        txtGPS.setText("GPS Position: long:" + gpsStats.longitude + " lat: " + gpsStats.latitude);
    }

    @Subscribe
    public void logEvent(LogEvent logEvent) {
        if (logEvent.getLogType() == LogEvent.LogType.NETWORK) {
            if (logEvent.getEventType() == LogEvent.EventType.SUCCESS) {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                int uploadedRecords = sharedPref.getInt("uploadedRecords", 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("uploadedRecords", uploadedRecords + logEvent.getNumRecords());
                editor.apply();
                txtUploadedRecords.setText("Uploaded Records: "
                        + (uploadedRecords + logEvent.getNumRecords()));

                txtSavedRecords.setText("Saved Records: 0");
            }
        } else if (logEvent.getLogType() == LogEvent.LogType.DISK) {
            if (logEvent.getEventType() == LogEvent.EventType.SUCCESS) {
                String savedRecords = txtSavedRecords.getText().toString();
                int numSavedRecords = Integer.parseInt(savedRecords.subSequence(15, savedRecords.length()).toString());
                txtSavedRecords.setText("Saved Records: " + ( numSavedRecords + logEvent.getNumRecords()));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        awsc.stop();
        eventBus.unregister(this);
    }
}
