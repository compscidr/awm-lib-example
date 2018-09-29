package io.rightmesh.awm_lib_example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.anadeainc.rxbus.Bus;
import com.anadeainc.rxbus.BusProvider;
import com.anadeainc.rxbus.Subscribe;

import io.rightmesh.awm.AndroidWirelessStatsCollector;
import io.rightmesh.awm.BluetoothStats;
import io.rightmesh.awm.GPSStats;

public class MainActivity extends AppCompatActivity {

    private AndroidWirelessStatsCollector awsc;
    protected Bus eventBus = BusProvider.getInstance();
    private TextView txtBtDevices;
    private TextView txtGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        awsc = new AndroidWirelessStatsCollector(this);
        awsc.start();

        txtBtDevices = findViewById(R.id.btDevices);
        txtGPS = findViewById(R.id.gpsCoords);
        eventBus.register(this);
    }

    @Subscribe
    public void updateBTDevices(BluetoothStats btStats) {
        String status = "btDevices: ";
        status = status + btStats.macs.size();
        for(String mac : btStats.macs) {
            status = status + "\n" + mac;
        }
        txtBtDevices.setText(status);
    }

    @Subscribe
    public void updateGPS(GPSStats gpsStats) {
        txtGPS.setText("GPS Position: long:" + gpsStats.longitude + " lat: " + gpsStats.latitude);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        awsc.stop();
        eventBus.unregister(this);
    }
}
