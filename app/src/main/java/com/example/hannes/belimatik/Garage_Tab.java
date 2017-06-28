package com.example.hannes.belimatik;

/**
 * Created by Hannes on 13.06.2017.
 */
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tinkerforge.BrickletDistanceUS;
import com.tinkerforge.BrickletDualRelay;
import com.tinkerforge.BrickletTemperatureIR;
import com.tinkerforge.IPConnection;
import com.tinkerforge.NotConnectedException;

public class Garage_Tab extends Fragment {

    private String TAG;
    private int errorCode = 0;
    private boolean poolGarageAktuateOk = false;
    private boolean poolGarageDistanceOk = false;
    private IPConnection ipcon_garage;
    private IPConnection ipcon_distance;
    private BrickletDualRelay dr;
    private BrickletDistanceUS dus;
    private Functions functions = new Functions();
    private TextView tvDistance;
    private TextView tvTimer;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beli_matik_garage_tab, container, false);

        TAG = "PJ_onCreateView";
        ipcon_garage = new IPConnection();
        ipcon_distance = new IPConnection();
        dr = new BrickletDualRelay(getString(R.string.uid_garage_dual_relay), ipcon_garage);
        dus = new BrickletDistanceUS(getString(R.string.uid_garage_Distance_US), ipcon_distance);
        tvDistance = (TextView) rootView.findViewById(R.id.tvDistance);
        tvTimer = (TextView) rootView.findViewById(R.id.tvTimer);

        ImageButton btnGarageAktor = (ImageButton) rootView.findViewById(R.id.ibGarageAktor);
        btnGarageAktor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AktuateGarage().execute("");
                poolGarageAktuateOk = false;
                new DistanceMeasure().execute("");
                poolGarageDistanceOk = false;





                dus.addDistanceListener(new BrickletDistanceUS.DistanceListener() {
                    public void distance(int distance) {
                        //Log.i(TAG, "PJ_setDistance: " + distance);
                        final int localDistance = distance;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    tvDistance.setText("geht eh " + localDistance);
                                    new CountDownTimer(10000, 1000) {
                                        public void onTick(long millisUntilFinished) {
                                        }

                                        public void onFinish() {
                                            try {
                                                Log.i(TAG, "PJ_Disconnect: ");
                                                ipcon_distance.disconnect();

                                            } catch (NotConnectedException nc) {
                                                Log.i(TAG, "PJ_NotConnected: " + nc);
                                            }
                                        }
                                        //todo timer einbauen
                                    }.start();

                                } catch (Exception e) {
                                    Log.i(TAG, "PJ_setDistanceFehler: " + e);
                                }
                            }
                    });

                        //Log.i(TAG, "PJ_distgesetzt: " + distance);
                    }

                    });




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
            while (!poolGarageAktuateOk && count++ < maxtries) {
                try {
                    ipcon_garage.connect(getString(R.string.host_Garage), getContext().getResources().getInteger(R.integer.port_garage));
                    dr.setMonoflop((short) 1, true, (long) 500);

                    ipcon_garage.disconnect();
                    poolGarageAktuateOk = true;
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
            Log.i(TAG, "PJ_distanceMeasure");
            Log.i(TAG, "poolgarageaktuate " + poolGarageAktuateOk);
            while (!poolGarageDistanceOk && count++ < maxtries) {
                try {
                    Log.i(TAG, "PJ_while");
                    ipcon_distance.connect(getString(R.string.host_Garage), getContext().getResources().getInteger(R.integer.port_garage));


                    //ipcon_garage.connect(getString(R.string.host_Garage), getContext().getResources().getInteger(R.integer.port_garage));
                    dus.setDistanceCallbackPeriod(200);
                    Log.i(TAG, "PJ_setDistanceCallPer");


                    //ipcon_garage.disconnect();
                    poolGarageDistanceOk = true;
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



}
