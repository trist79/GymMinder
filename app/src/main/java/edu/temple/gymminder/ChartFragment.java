package edu.temple.gymminder;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;


/**
 * Draws a chart from a float array extra passed in using the static newInstance method.
 */
public class ChartFragment extends Fragment {

    private static final String EXTRA_DATA_STREAM = "Testing for compatibility... T";

    ArrayList<Entry> data = new ArrayList<>();
    LineChart chart;

    public ChartFragment() {
        // Required empty public constructor
    }

    public static ChartFragment newInstance(float[] accelerationStream){
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putFloatArray(EXTRA_DATA_STREAM, accelerationStream);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chart, container, false);
        float[] args;
        args = getArguments() == null ? null : getArguments().getFloatArray(EXTRA_DATA_STREAM);
        if (args == null) args = new float[]{1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 5, 6, 7, 8, 5, 4};
        for (int i = 0; i < args.length; i++) {
            data.add(new Entry(i, args[i]));
        }

        chart = (LineChart) v.findViewById(R.id.chart);
        LineDataSet dataSet = new LineDataSet(data, "Acceleration Stream");
        dataSet.setColor(Color.RED);
        dataSet.setValueTextSize(0f);
        dataSet.setCircleColor(Color.BLACK);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.invalidate();
        return v;
    }

}
