package edu.temple.gymminder;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by rober_000 on 4/23/2017.
 */

public class ProcessRealDataTest extends ProcessTest {

    ArrayList<ArrayList<Float>> data = new ArrayList<>(3);

    @Override
    void setupPeakTimeSeriesAndAxis(){
        data = readData("repetition_time_series_5_reps.csv");
        int index = 0;
        ArrayList<Float> base = readData("repetition_time_series_base.csv").get(index);
        DataUtils.Peak peak = new DataUtils.Peak(31, 2.05744835048f);
        TimeSeries series = DataUtils.seriesFromList(base);

        DataUtils.repTimeSeries = series;
        DataUtils.repPeak = peak;
        DataUtils.majorAxisIndex = index;
        DataUtils.peaks = new HashMap<>();
        DataUtils.init(res.data, res.timestamps, res.processed);
    }

    ArrayList<ArrayList<Float>> readData(String filename){
        ArrayList<ArrayList<Float>> ret = new ArrayList<>();
        URL url = getClass().getClassLoader().getResource(filename);
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            for(int j=0;j<3;j++) {
                String line = reader.readLine();
                String[] strings = line.split(",");
                ArrayList<Float> values = new ArrayList<>(strings.length);
                for (String s : strings) {
                    values.add(Float.parseFloat(s));
                }
                values = DataUtils.applySavitzkyGolayFilter(values);
                ret.add(values);
            }
        } catch (IOException e) {
            fail();
        }
        return ret;
    }

    @Override
    public void testProcessRemovesOverlappingPeaks() {
        setupPeakTimeSeriesAndAxis();
        for(int i=0; i<50; i++){
            float[] values = {0, 0, 0};
            DataUtils.process(values, DataUtils.POLLING_RATE*i);
        }
        DataUtils.process(new float[] {100,100,100}, DataUtils.POLLING_RATE*50);
        DataUtils.process(new float[] {0,0,0}, DataUtils.POLLING_RATE*51);
        DataUtils.process(new float[] {100,100,100}, DataUtils.POLLING_RATE*52);
        DataUtils.process(new float[] {0,0,0}, DataUtils.POLLING_RATE*53);
        assertEquals(2, DataUtils.peaks.size());
        for(int i=0; i<59; i++){
            float[] values = {0, 0, 0};
            DataUtils.process(values, DataUtils.POLLING_RATE*(i+54));
        }
        assertEquals(2, DataUtils.peaks.size());
        float[] values = {0, 0, 0};
        DataUtils.process(values, DataUtils.POLLING_RATE * 113);
        assertEquals(0, DataUtils.peaks.size());
    }

    @Override
    public void testProcessDoesNotRemoveNonOverlappingPeaks() {
        setupPeakTimeSeriesAndAxis();
        for(int i=0; i<50; i++){
            float[] values = {0, 0, 0};
            DataUtils.process(values, DataUtils.POLLING_RATE*i);
        }
        DataUtils.process(new float[] {100,100,100}, DataUtils.POLLING_RATE*50);
        for(int i=0; i<60; i++){
            float[] values = {0, 0, 0};
            DataUtils.process(values, DataUtils.POLLING_RATE*(i+51));
        }
        DataUtils.process(new float[] {100,100,100}, DataUtils.POLLING_RATE*151);
        assertEquals(2, DataUtils.peaks.size());
        float[] values = {0, 0, 0};
        DataUtils.process(values, DataUtils.POLLING_RATE*(152));
        assertEquals(2, DataUtils.peaks.size());
        DataUtils.process(values, DataUtils.POLLING_RATE*(153));
        assertEquals(1, DataUtils.peaks.size());
    }

    @Test
    public void testCalculateReps(){
        setupPeakTimeSeriesAndAxis();
        assertEquals(5, DataUtils.calculateReps(DataUtils.seriesFromList(data.get(0))).size());
    }


    }
