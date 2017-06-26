package com.example.hannes.belimatik;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


/**
 * Created by Hannes on 13.06.2017.
 */

public class Temperature_Tab extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beli_matik_temperature_tab, container, false);

         return rootView;
    }
}

