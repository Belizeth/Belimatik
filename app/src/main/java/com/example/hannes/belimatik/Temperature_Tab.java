package com.example.hannes.belimatik;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;

import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import api.NetatmoUtils;
import api.model.Measures;
import api.model.Module;
import api.model.Params;
import api.model.Station;

import static android.content.ContentValues.TAG;


/**
 * Created by Hannes on 13.06.2017.
 */

public class Temperature_Tab extends Fragment {

    HttpClient httpClient;
    Handler handler = new Handler();
    ListView listView;
    CustomAdapter mAdapter;
    List<Module> mListItems = new ArrayList<>();
    List<Station> mDevices;
    int mCompletedRequest;
    Station myStation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beli_matik_temperature_tab, container, false);

        listView = (ListView) rootView.findViewById(R.id.NetatmoList);
        // Login to Netatmo
        httpClient = new HttpClient(getActivity());
        if(httpClient.getAccessToken() != null){
            //if the user is already logged

        }else{
            //else, stats the LoginActivity
            doNetatmoLogin();
        }
        initNetatmoData();
        CountDownTimer doorTimer = new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
                //tvSecondsToTerminate.setText("" + millisUntilFinished / 1000);
            }
            public void onFinish() {
                //finishing = true;
                //PlaySound(R.raw.error_max, MainActivity.this);

                //finishAndRemoveTask();
                ReadAndShowData();
            }
        }.start();

         return rootView;
    }
    private void doNetatmoLogin(){

        final HttpClient httpClient = new HttpClient(getActivity());
        httpClient.login(getString(R.string.netatmo_user), getString(R.string.netatmo_pwd),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject;
                        try {
                            jsonObject= new JSONObject(response);
                            httpClient.processOAuthResponse(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,error.toString());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG,"PJ_error::: da hats was");
                            }
                        });
                    }
                }


        );
    }
    private void initNetatmoData(){
        mAdapter = new CustomAdapter(getActivity(), mListItems);

        listView.setAdapter(mAdapter);


        //setSupportProgressBarIndeterminateVisibility(true);

        //final ctivity activity = this;


        httpClient.getDevicesList(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject res = null;
                        try {
                            res = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (res != null) {
                            mDevices = NetatmoUtils.parseDevicesList(res);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    List<String> stationName = new ArrayList<>();
                                    for (Station station : mDevices) {
                                        myStation = station;
                                        stationName.add(station.getName());
                                        Log.d(TAG, "PJ_Station::: " + myStation.getName());
                                    }

                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                            getActivity(),
                                            android.R.layout.simple_spinner_dropdown_item,
                                            stationName);

                                   /* ActionBar actionBar = getSupportActionBar();
                                    actionBar.setDisplayShowTitleEnabled(false);
                                    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                                   // actionBar.setListNavigationCallbacks(adapter, getActivity());
                                   */

                                }
                            });

                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                             //   setSupportProgressBarIndeterminateVisibility(false);
                            }
                        });



                    }



                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                              //  setSupportProgressBarIndeterminateVisibility(false);
                            }
                        });
                    }
                });
    }

    private void ReadAndShowData(){
        int itemPosition = 1;
        //Station station = mDevices.get(itemPosition);
        final List<Module> modules = myStation.getModules();
        mCompletedRequest = modules.size();

        if(!mListItems.isEmpty()){
            mListItems.clear();
            mAdapter.notifyDataSetChanged();
        }

        final String[] types = new String[]{
                Params.TYPE_NOISE,
                Params.TYPE_CO2,
                Params.TYPE_PRESSURE,
                Params.TYPE_HUMIDITY,
                Params.TYPE_TEMPERATURE,
                Params.TYPE_RAIN,
                Params.TYPE_RAIN_SUM_1,
                Params.TYPE_RAIN_SUM_24,
                Params.TYPE_WIND_ANGLE,
                Params.TYPE_WIND_STRENGTH,
                Params.TYPE_GUST_ANGLE,
                Params.TYPE_GUST_STRENGTH
        };

        httpClient.getDevicesList(
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject res = null;
                        try {
                            res = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(res!=null){
                            HashMap<String, Measures> measuresHashMap = NetatmoUtils.parseMeasures(res,types);
                            for(Module module : modules){
                                if(measuresHashMap.containsKey(module.getId())){
                                    module.setMeasures(measuresHashMap.get(module.getId()));
                                    mListItems.add(module);
                                }
                            }
                            mAdapter.notifyDataSetChanged();

                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //setSupportProgressBarIndeterminateVisibility(false);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,error.toString());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //setSupportProgressBarIndeterminateVisibility(false);
                            }
                        });
                    }
                });


    }
}

