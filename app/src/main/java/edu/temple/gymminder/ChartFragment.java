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
 * A simple {@link Fragment} subclass.
 */
public class ChartFragment extends Fragment {

    private static final String DATA_EXTRA = "Testing for compatibility... T";

    ArrayList<Entry> data = new ArrayList<>();
    LineChart chart;

    public ChartFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_chart, container, false);
        float[] args;
        args = getArguments()== null ? null : getArguments().getFloatArray(DATA_EXTRA);
        if(args == null) args = new float[] {1,2,3,4,5,6,7,6,5,4,3,5,6,7,8,5,4};
        for(int i=0;i<args.length;i++) {
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
