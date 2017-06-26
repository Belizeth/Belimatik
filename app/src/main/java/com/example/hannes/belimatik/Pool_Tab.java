package com.example.hannes.belimatik;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tinkerforge.BrickletPTC;
import com.tinkerforge.BrickletRemoteSwitch;
import com.tinkerforge.BrickletTemperatureIR;
import com.tinkerforge.IPConnection;


/**
 * Created by Hannes on 13.06.2017.
 */

public class Pool_Tab extends Fragment {
    private IPConnection ipcon;
    private BrickletTemperatureIR tir;
    private BrickletPTC ptc;
    private BrickletRemoteSwitch rs;
    private int waterTempAblauf;
    private short ambjTemp;
    private static final String TAG = "TAG" ;
    private TextView tvAmbTemp;
    private TextView tvWaterTempAblauf;
    private boolean poolTempgetOk = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beli_matik_pool_tab, container, false);

        tvWaterTempAblauf = (TextView) rootView.findViewById(R.id.WaterTempAblaufView);
        tvAmbTemp = (TextView) rootView.findViewById(R.id.PoolAirTempView);
        ipcon = new IPConnection();
        tir = new BrickletTemperatureIR(getString(R.string.uid_pool_amb), ipcon);
        ptc = new BrickletPTC("yX8", ipcon);
        rs = new BrickletRemoteSwitch("nWL", ipcon);


        new SetTemperatures().execute("");

        ImageButton btnLightOn = (ImageButton) rootView.findViewById(R.id.btnLightOn);
        btnLightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendLightOrder LightOrder = new SendLightOrder("On");

                LightOrder.execute("");
                Log.i(TAG, "PJ_you clicked On");
            }
        });

        ImageButton btnLightOff = (ImageButton) rootView.findViewById(R.id.btnLightOff);
        btnLightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendLightOrder LightOrder = new SendLightOrder("Off");

                LightOrder.execute("");
                Log.i(TAG, "PJ_you clicked Off");
            }
        });

        ImageButton btnLightNextColor = (ImageButton) rootView.findViewById(R.id.btnLightNextColor);
        btnLightNextColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendLightOrder LightOrder = new SendLightOrder("NextColor");

                LightOrder.execute("");
                Log.i(TAG, "PJ_you clicked next Color");
            }
        });

        return rootView;
    }

    private class SetTemperatures extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            int count = 0;
            int maxtries = 5;
            while (!poolTempgetOk && count++ < maxtries) {
                try {
                    ipcon.connect(getString(R.string.host_pool), getContext().getResources().getInteger(R.integer.port_pool));
                    waterTempAblauf = ptc.getTemperature();
                    ambjTemp = tir.getObjectTemperature();
                    ipcon.disconnect();
                    poolTempgetOk = true;
                } catch (Exception e) {

                    Log.i(TAG, "pj_" + count + ":::" + e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            tvWaterTempAblauf.setText("" + waterTempAblauf/100.0 + " °C");
            tvAmbTemp.setText("" + ambjTemp/10.0 + " °C");
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private class SendLightOrder extends AsyncTask<String, Void, String> {
        private  String switchOrder;
        public SendLightOrder(String order){
            super();
           switchOrder = order;
        }
        @Override
        protected String doInBackground(String... params) {

            try {
                ipcon.connect(getString(R.string.host_pool), getContext().getResources().getInteger(R.integer.port_pool));
                if (switchOrder == "On") {
                    rs.switchSocketB((short) 1, (short) 2, BrickletRemoteSwitch.SWITCH_TO_ON);
                } else if (switchOrder == "Off"){
                    rs.switchSocketB((short) 1, (short) 2, BrickletRemoteSwitch.SWITCH_TO_OFF);
                } else if (switchOrder == "NextColor"){
                    rs.switchSocketB((short) 1, (short) 2, BrickletRemoteSwitch.SWITCH_TO_OFF);
                    Thread.sleep(600);
                    rs.switchSocketB((short) 1, (short) 2, BrickletRemoteSwitch.SWITCH_TO_ON);
                }
                ipcon.disconnect();
            } catch (Exception e) {

                Log.i(TAG, "pj_" + ":::" + e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            tvWaterTempAblauf.setText("" + waterTempAblauf/100.0 + " °C");
            tvAmbTemp.setText("" + ambjTemp/10.0 + " °C");
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
