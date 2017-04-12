package edu.temple.gymminder;

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
        assertEquals(50, res.timestamps.size());
        assertEquals(50, res.data.get(0).size());
        assertEquals(50, res.processed.get(0).size());
    }
}
