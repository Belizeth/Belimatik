package com.example.hannes.belimatik;

/**
 * Created by Hannes on 13.06.2017.
 */
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Garage_Tab extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beli_matik_garage_tab, container, false);
        return rootView;
    }
}
