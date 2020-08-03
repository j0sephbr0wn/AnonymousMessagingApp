package com.brownjs.anonymousmessagingapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.brownjs.anonymousmessagingapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Class is not currently used
 */
public class ChampionsFragment extends Fragment {

    /**
     * Constructor
     */
    public ChampionsFragment() {
        // Required empty public constructor
    }


    /**
     * {@inheritDoc}
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_champions, container, false);

        ImageView imgPreview = view.findViewById(R.id.image);


        return view;
    }
}
