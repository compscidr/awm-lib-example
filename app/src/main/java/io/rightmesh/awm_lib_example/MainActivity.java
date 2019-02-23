package io.rightmesh.awm_lib_example;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anadeainc.rxbus.Bus;
import com.anadeainc.rxbus.BusProvider;
import com.anadeainc.rxbus.Subscribe;
import io.rightmesh.awm.AndroidWirelessStatsCollector;
import io.rightmesh.awm.loggers.LogEvent;
import io.rightmesh.awm.stats.GPSStats;
import io.rightmesh.awm.stats.NetworkDevice;
import io.rightmesh.awm.stats.NetworkStat;

import static io.rightmesh.awm.stats.NetworkStat.DeviceType.BLUETOOTH;
import static io.rightmesh.awm.stats.NetworkStat.DeviceType.WIFI;

public class MainActivity extends AppCompatActivity {

    private AndroidWirelessStatsCollector awsc;
    protected Bus eventBus = BusProvider.getInstance();
    private TextView txtBtDevices;
    private TextView txtWifiDevices;
    private TextView txtGPS;
    private TextView txtSavedRecords;
    private TextView txtUploadedRecords;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Android Wireless Measurement");
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_main);
        started = false;

        awsc = new AndroidWirelessStatsCollector(this, false, false);
        awsc.start();
        started = true;

        txtBtDevices = findViewById(R.id.btDevices);
        txtWifiDevices = findViewById(R.id.wifiDevices);
        txtGPS = findViewById(R.id.gpsCoords);
        txtSavedRecords = findViewById(R.id.txtRecords);
        txtUploadedRecords = findViewById(R.id.txtUploadedRecords);
        eventBus.register(this);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int uploadedRecords = sharedPref.getInt("uploadedRecords", 0);
        txtUploadedRecords.setText("Uploaded Records: " + uploadedRecords);

        new Thread(() -> {
            int savedRecords = awsc.getSavedRecordCount();
            Log.d("MA", "SAVED COUNT: " + savedRecords);

            runOnUiThread(() -> {
                txtSavedRecords.setText("Saved Records: " + savedRecords);
            });
        }).start();
    }

    @Subscribe
    public void updateNetworkDevices(NetworkStat networkStat) {
        if (networkStat.getType() == BLUETOOTH) {
            Log.d("MA", "GOT BT NETWORK STAT TYPE");
            String status = "btDevices: ";
            status = status + networkStat.getDevices().size();
            for(NetworkDevice device : networkStat.getDevices()) {
                status = status + "\n" + device.getMac() + " " + device.getName() + " "
                        + device.getSignalStrength() + "dB";
            }
            txtBtDevices.setText(status);
        } else if (networkStat.getType() == WIFI) {
            Log.d("MA", "GOT WIFI NETWORK STAT TYPE");
            String status = "wifiDevices: ";
            status = status + networkStat.getDevices().size();
            for(NetworkDevice device : networkStat.getDevices()) {
                status = status + "\n" + device.getMac() + " " + device.getName() + " "
                        + device.getFrequency() + "Mhz " + device.getSignalStrength() + "dB";
            }
            txtWifiDevices.setText(status);
        } else {
            Log.d("MA", "GOT UNKNOWN NETWORK STAT TYPE");
        }
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
        if(started) {
            awsc.stop();
        }
        eventBus.unregister(this);
    }

    public void ToggleOnOff(View v) {
        Button btnOnOff = findViewById(R.id.btnOnOff);
        if (btnOnOff.getText().equals("TURN OFF")) {
            Log.d("MA", "TURN OFF");
            awsc.stop();
            started = false;
            txtBtDevices.setText("Turned off.");
            txtWifiDevices.setText("Turned off.");
            btnOnOff.setText("TURN ON");
        } else {
            Log.d("MA", "TURN ON");
            awsc.start();
            started = true;
            txtBtDevices.setText("btDevices: ");
            txtWifiDevices.setText("wifiDevices: ");
            btnOnOff.setText("TURN OFF");
        }
    }
}
