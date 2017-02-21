package edu.temple.gymminder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class IntegrationEstimationTest {

    public static final long PERIOD = 10L;
    ArrayList<ArrayList<Float>> data = new ArrayList<>();
    ArrayList<ArrayList<Float>> processed = new ArrayList<>();

    @Before
    public void initDataUtils() {
        for (int i = 0; i < 3; i++) {
            data.add(new ArrayList<Float>());
            processed.add(new ArrayList<Float>());
        }
        ArrayList<Long> timestamps = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            timestamps.add(PERIOD * i * 1000000000);
        }
        DataUtils.init(data, timestamps, processed);
    }

    @Test
    public void testDataUtilsConstantRiemann() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(1f);
        }
        float f[] = DataUtils.riemann(data);
        for (int i = 0; i < 9; i++) {
            assertEquals(PERIOD, f[i], .1);
        }
    }

    @Test
    public void testDataUtilsConstantIntegration() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(1f);
        }
        float f = DataUtils.sum(DataUtils.riemann(data));
        assertEquals(PERIOD * 9, f, 1);
    }

    @Test
    public void testDataUtilsLinearRiemann() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(1f * i);
        }
        float f[] = DataUtils.riemann(data);
        for (int i = 0; i < 9; i++) {
            assertEquals(i * PERIOD, f[i], .1);
        }
    }

    @Test
    public void testDataUtilsLinearIntegration() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(1f * i);
        }
        float f = DataUtils.sum(DataUtils.riemann(data));
        assertEquals(360, f, 1);
    }

    @Test
    public void applySavitzkyGolayFilterDoesNotCrash() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(1f);
        }
        DataUtils.applySavitzkyGolayFilter(data);
    }

    @Test
    public void realTimeSavitzkyGolayFilterDoesNotCrash() {
        for (int i = 0; i < 10; i++) {
            data.get(0).add((float) Math.random());
        }
        for (int i = 0; i < 10; i++) {
            DataUtils.applySGFilterRealtime(i, data.get(0), processed.get(0));
        }
    }

    @Test
    public void testSavitzkyGolayFilter() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add((float) i);
        }
        data = DataUtils.applySavitzkyGolayFilter(data);
        float[] expected = {.28f, .95f, 1.9f, 3f, 4f, 5f, 6f, 7.95f, 7.61f, 5.71f};
        for (int i = 0; i < 10; i++) {
            assertEquals(expected[i], data.get(i), .01);
        }
    }

    @Test
    public void testSavitzkyGolayFilterRealtime() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add((float) i);
        }
        for (int i = 0; i < 10; i++) {
            DataUtils.applySGFilterRealtime(i, data, processed.get(0));
        }
        float[] expected = {.28f, .95f, 1.9f, 3f, 4f, 5f, 6f, 7.95f, 7.61f, 5.71f};
        for (int i = 0; i < 10; i++) {
            assertEquals(expected[i], processed.get(0).get(i), .01);
        }
    }

    @Test
    public void testInterpolation(){
        ArrayList<ArrayList<Float>> data = new ArrayList<>();
        data.add(new ArrayList<Float>());
        data.get(0).add(10f);
        //reinitialize DataUtils to use our new data sets
        DataUtils.init(data, null, null);
        float x = DataUtils.interpolate(20, .2f, 0);
        assertEquals(15, x, .01);
        x = DataUtils.interpolate(30, .2f, 0);
        assertEquals(20, x, .01);
        x = DataUtils.interpolate(30, .3f, 0);
        assertEquals(16.66, x, .01);
    }

    @Test
    public void testAverage(){
        float[] avgNode = {10f, 10f};
        float[] newNode = {20f, 10f};
        float[] result = DataUtils.average(avgNode, newNode[0], newNode[1]);
        assertEquals(15, result[0], .1);
        assertEquals(20, result[1], .1);
        newNode = new float[]{20f, 20f};
        avgNode = new float[]{10f, 10f};
        result = DataUtils.average(avgNode, newNode[0], newNode[1]);
        assertEquals(16.66f, result[0], .1);
    }

}