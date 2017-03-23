package edu.temple.gymminder;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DataUtilsTest {

    public static final long PERIOD = 10L;
    public static final int S2MS_CONVERSION = 1000000000;
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
            timestamps.add(PERIOD * i * S2MS_CONVERSION);
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

    @Test
    public void testSuccsesfulRemovalOfPeaks() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class[] classes = DataUtils.class.getDeclaredClasses();
        Class peakClass = null;
        for(Class c : classes){
            if(c.getName().equals("edu.temple.gymminder.DataUtils$Peak")){
                peakClass = c;
                break;
            }
        }
        Constructor constr = peakClass.getDeclaredConstructors()[0];
        constr.setAccessible(true);
        Object originalPeak = constr.newInstance(null, 0, 10f);
        ArrayList<Object> peaks = new ArrayList<>();
        Random rand = new Random();
        for(int i=0;i<10;i++){
            peaks.add(constr.newInstance(null, 0, rand.nextFloat()*3));
        }
        assertEquals(10, peaks.size());
        peaks = (ArrayList<Object>) DataUtils.class
                .getDeclaredMethod("reducePeaks", ArrayList.class, originalPeak.getClass())
                .invoke(null, peaks, originalPeak);
        assertEquals(0, peaks.size());
    }

    @Test
    public void testNonRemovalOfValidPeaks() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Class[] classes = DataUtils.class.getDeclaredClasses();
        Class peakClass = null;
        for(Class c : classes){
            if(c.getName().equals("edu.temple.gymminder.DataUtils$Peak")){
                peakClass = c;
                break;
            }
        }
        Constructor constr = peakClass.getDeclaredConstructors()[0];
        constr.setAccessible(true);
        Object originalPeak = constr.newInstance(null, 0, 10f);
        ArrayList<Object> peaks = new ArrayList<>();
        Random rand = new Random();
        for(int i=0;i<10;i++){
            peaks.add(constr.newInstance(null, 0, 3.34f+rand.nextFloat()*7f));
        }
        assertEquals(10, peaks.size());
        peaks = (ArrayList<Object>) DataUtils.class
                .getDeclaredMethod("reducePeaks", ArrayList.class, originalPeak.getClass())
                .invoke(null, peaks, originalPeak);
        assertEquals(10, peaks.size());
    }

    @Test
    public void testAccept() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Class[] classes = DataUtils.class.getDeclaredClasses();
        Class boundsClass = null;
        for(Class c : classes){
            if(c.getName().equals("edu.temple.gymminder.DataUtils$DetectedBounds")){
                boundsClass = c;
                break;
            }
        }
        Constructor constr = boundsClass.getDeclaredConstructors()[0];
        constr.setAccessible(true);
        //TODO: change constructor call when coefficients are found
        Object bounds = constr.newInstance(null, 0, 0, 0f, 0f, 0f, 0f, 0f);
        boolean x = (boolean) DataUtils.class
                .getDeclaredMethod("accept", bounds.getClass())
                .invoke(null, bounds);
        assertEquals(true, x);

        bounds = constr.newInstance(null, -1, 0, 0f, 0f, 0f, 0f, 0f);
        x = (boolean) DataUtils.class
                .getDeclaredMethod("accept", bounds.getClass())
                .invoke(null, bounds);
        assertEquals(x, true);
    }

}