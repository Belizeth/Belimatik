package com.example.hannes.belimatik;

/**
 * Created by Hannes on 13.06.2017.
 */
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.tinkerforge.BrickletDistanceUS;
import com.tinkerforge.BrickletDualRelay;
import com.tinkerforge.BrickletTemperatureIR;
import com.tinkerforge.IPConnection;

public class Garage_Tab extends Fragment {

    private String TAG;
    private int errorCode = 0;
    private boolean poolGarageAktuateOk = false;
    private IPConnection ipcon;
    private BrickletDualRelay dr;
    private BrickletDistanceUS dus;
    private Functions functions = new Functions();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beli_matik_garage_tab, container, false);

        TAG = "PJ_onCreateView";
        ipcon = new IPConnection();
        dr = new BrickletDualRelay(getString(R.string.uid_garage_dual_relay), ipcon);

        ImageButton btnGarageAktor = (ImageButton) rootView.findViewById(R.id.ibGarageAktor);
        btnGarageAktor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AktuateGarage().execute("");

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
                    ipcon.connect(getString(R.string.host_pool), getContext().getResources().getInteger(R.integer.port_pool));
                    dr.setMonoflop((short) 1, true, (long) 500);
                    ipcon.disconnect();
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
}
