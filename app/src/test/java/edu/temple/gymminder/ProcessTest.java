package edu.temple.gymminder;

import com.fastdtw.timeseries.TimeSeriesBase;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by rober_000 on 4/12/2017.
 */

public class ProcessTest {

    @Rule
    public final DataUtilsResources res = new DataUtilsResources();

    @Test
    public void processDoesNotCrash(){
        res.timestamps = new ArrayList<>();
        DataUtils.init(res.data, res.timestamps, res.processed);
        long timestamp = 1000000000L;
        Random random = new Random();
        for(int i=0; i<100; i++){
            float[] values = {random.nextFloat(), random.nextFloat(), random.nextFloat()};
            DataUtils.process(values, timestamp+100000000L*i);
        }
    }

    @Test
    public void processCorrectlyAddsValuesWithTimestampsGreaterThanPeriod(){
        processDoesNotCrash();
        assertEquals(100, res.timestamps.size());
        assertEquals(100, res.data.get(0).size());
        assertEquals(100, res.processed.get(0).size());
    }

    @Test
    public void processCorrectlyAddsValuesWithTimestampsLessThanPeriod(){
        res.timestamps = new ArrayList<>();
        DataUtils.init(res.data, res.timestamps, res.processed);
        long timestamp = 1000000000L;
        Random random = new Random();
        for(int i=0; i<100; i++){
            float[] values = {random.nextFloat(), random.nextFloat(), random.nextFloat()};
            DataUtils.process(values, timestamp+50000000L*i);
        }
        //Should be 1+floor(99/2)
        //Or 1+floor(100/2), it honestly doesn't matter
        assertEquals(50, res.timestamps.size(), 1);
        assertEquals(50, res.data.get(0).size(), 1);
        assertEquals(50, res.processed.get(0).size(), 1);
    }

    @Test
    public void processCorrectlyAddsPeak(){
        res.timestamps = new ArrayList<>();
        //TODO: set to actual thing
        TimeSeriesBase.Builder builder = new TimeSeriesBase.Builder();
        for(int i=0;i<50;i++){
            builder = builder.add(0, i);
        }
        DataUtils.repTimeSeries = builder.build();
        DataUtils.repPeak = new DataUtils.Peak(25,0);
        DataUtils.majorAxisIndex = 0;
        DataUtils.init(res.data, res.timestamps, res.processed);
        long timestamp = DataUtils.SECOND / DataUtils.POLLING_FREQUENCY;
        for(int i=0; i<50; i++){
            float[] values = {0, 0, 0};
            DataUtils.process(values, timestamp*i);
        }
        DataUtils.process(new float[] {100,100,100}, timestamp*50);
        for(int i=0; i<5; i++){
            float[] values = {0, 0, 0};
            DataUtils.process(values, timestamp*(i+50));
        }
        assertEquals(1, DataUtils.peaks.size());
    }

    @Test
    public void processCorrectlyRemovesPeakAfterProcessed(){
        res.timestamps = new ArrayList<>();
        //TODO: set to actual thing
        TimeSeriesBase.Builder builder = new TimeSeriesBase.Builder();
        for(int i=0;i<50;i++){
            builder = builder.add(0, i);
        }
        DataUtils.repTimeSeries = builder.build();
        DataUtils.repPeak = new DataUtils.Peak(25,0);
        DataUtils.majorAxisIndex = 0;
        DataUtils.init(res.data, res.timestamps, res.processed);
        long timestamp = DataUtils.SECOND / DataUtils.POLLING_FREQUENCY;
        for(int i=0; i<50; i++){
            float[] values = {0, 0, 0};
            DataUtils.process(values, timestamp*i);
        }
        DataUtils.process(new float[] {100,100,100}, timestamp*50);
        for(int i=0; i<100; i++){
            float[] values = {0, 0, 0};
            DataUtils.process(values, timestamp*(i+50));
        }
        assertEquals(0, DataUtils.peaks.size());
    }

}
