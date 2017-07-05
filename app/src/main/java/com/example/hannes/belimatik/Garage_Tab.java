package com.example.hannes.belimatik;

/**
 * Created by Hannes on 13.06.2017.
 */
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tinkerforge.BrickletDistanceUS;
import com.tinkerforge.BrickletDualRelay;
import com.tinkerforge.IPConnection;

public class Garage_Tab extends Fragment {

    private String TAG;
    private int errorCode = 0;
    private boolean garageAktuateOk = false;
    private boolean garageDistanceOk = false;
    private IPConnection ipcon_garage;
    private IPConnection ipcon_distance;
    private BrickletDualRelay dr;
    private BrickletDistanceUS dus_first;
    private BrickletDistanceUS dus_second;
    private Functions functions = new Functions();
    private TextView tvDistance_first;
    private TextView tvDistance_second;
    private TextView tvStatus;
    private int distance_first;
    private int distance_second;
    private CountDownTimer cTimer;
    private boolean isTimerRunning = false;
    private ProgressBar garagePB;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beli_matik_garage_tab, container, false);

        TAG = "PJ_onCreateView";
        ipcon_garage = new IPConnection();
        ipcon_distance = new IPConnection();
        dr = new BrickletDualRelay(getString(R.string.uid_garage_dual_relay), ipcon_garage);
        dus_first = new BrickletDistanceUS(getString(R.string.uid_garage_Distance_first_US), ipcon_distance);
        dus_second = new BrickletDistanceUS(getString(R.string.uid_garage_Distance_second_US), ipcon_distance);
        tvDistance_first = (TextView) rootView.findViewById(R.id.tvDistance_first);
        tvDistance_second = (TextView) rootView.findViewById(R.id.tvDistance_second);
        tvStatus = (TextView) rootView.findViewById(R.id.tvStatus);
        garagePB = (ProgressBar) rootView.findViewById(R.id.garageProgressBar);

        new DistanceMeasure().execute("");
        garageDistanceOk = false;

        ImageButton btnCheckDistance = (ImageButton) rootView.findViewById(R.id.ibCheckDistance);
        btnCheckDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDistance_first.setText("Messung läuft");
                tvDistance_second.setText("Messung läuft");
                tvStatus.setText("Messung läuft");
                new DistanceMeasure().execute("");
                garageDistanceOk = false;
            }

            });


        ImageButton btnGarageAktor = (ImageButton) rootView.findViewById(R.id.ibGarageAktor);
        btnGarageAktor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                garagePB.setVisibility(View.VISIBLE);
                if (isTimerRunning){
                    cTimer.cancel();
                    isTimerRunning = false;
                    new AktuateGarage().execute("");
                    garageAktuateOk = false;
                    new DistanceMeasure().execute("");
                    garageDistanceOk = false;
                    tvDistance_first.setText("Distance " + distance_first);
                    tvDistance_second.setText("Distance " + distance_second);
                    tvStatus.setText("Unterbrochen");

                    garagePB.setVisibility(View.GONE);
                    return;
                }
                new DistanceMeasure().execute("");
                garageDistanceOk = false;
                new AktuateGarage().execute("");
                garageAktuateOk = false;

                //tvDistance.setText("Tor läuft");
                isTimerRunning = true;
                cTimer = new CountDownTimer(10000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        Log.i(TAG, "PJ_tinmer: " + millisUntilFinished / 1000);

                        new DistanceMeasure().execute("");
                        garageDistanceOk = false;
                    }
                    public void onFinish() {
                        isTimerRunning = false;
                        new DistanceMeasure().execute("");
                        garageDistanceOk = false;
                        Log.i(TAG, "PJ_distance_first: " + distance_first + "PJ_distance_second: " + distance_second);

                        isTimerRunning = false;
                        garagePB.setVisibility(View.GONE);
                    }
                };
                cTimer.start();
                isTimerRunning = true;

                Log.i(TAG, "PJ_clicked on garageopen");
            }
        });

        return rootView;
    }

    private class AktuateGarage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            int count = 0;
            int maxtries = 5;
            while (!garageAktuateOk && count++ < maxtries) {
                try {
                    ipcon_garage.connect(getString(R.string.host_Garage), getContext().getResources().getInteger(R.integer.port_garage));
                    dr.setMonoflop((short) 1, true, (long) 500);

                    ipcon_garage.disconnect();
                    garageAktuateOk = true;
                } catch (Exception e) {
                    Log.i(TAG, "pj_" + count + ":::" + e);

                    if (count >= 5) {
                        errorCode = 1;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {


            if (errorCode == 1){
                functions.showMsg("Fehler", "Verbindung konnte nicht hergestellt werden!", true, getActivity());
            }


        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }



    private class DistanceMeasure extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            int count = 0;
            int maxtries = 5;

            while (!garageDistanceOk && count++ < maxtries) {
                try {

                    ipcon_distance.connect(getString(R.string.host_Garage), getContext().getResources().getInteger(R.integer.port_garage));
                    distance_first = dus_first.getDistanceValue();
                    distance_second = dus_second.getDistanceValue();
                    ipcon_distance.disconnect();
                    garageDistanceOk = true;
                } catch (Exception e) {
                    Log.i(TAG, "pj_" + count + ":::" + e);

                    if (count >= 5) {
                        errorCode = 1;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            tvDistance_first.setText("Entfernung 1: " + distance_first);
            tvDistance_second.setText("Entfernung 2: " + tvDistance_second);

            if (isTimerRunning){
                setStatusRunning();
            } else {
                setStatus();
            }

            if (errorCode == 1){
                functions.showMsg("Fehler", "Verbindung konnte nicht hergestellt werden11!", true, getActivity());
            }


        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

private void setStatus(){
    if (distance_first < 1000 && distance_second < 1000){
        tvStatus.setText("Tor geöffnet");
    } else if (distance_first > 1000 && distance_second > 1000){
        tvStatus.setText("Tor geschlossen");
    } else if (distance_first < 1000 && distance_second > 1000){
        tvStatus.setText("Tor teilweise geöffnet");
    } else {
        tvStatus.setText("Tor status undefiniert");
    }
}
    private void setStatusRunning(){
        if (distance_first < 1000 && distance_second < 1000){
            tvStatus.setText("Tor geöffnet");
        } else if (distance_first > 1000 && distance_second > 1000){
            tvStatus.setText("Tor geschlossen");
        } else if (distance_first < 1000 && distance_second > 1000){
            tvStatus.setText("Tor in Bewegung");
        } else {
            tvStatus.setText("Tor status undefiniert");
        }
    }
}
