package com.brownjs.anonymousmessagingapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brownjs.anonymousmessagingapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChampionsFragment extends Fragment {

    public ChampionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_champions, container, false);
    }
}
