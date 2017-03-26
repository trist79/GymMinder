package edu.temple.gymminder;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.dtw.WarpPath;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;

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
    ArrayList<Long> timestamps;

    @Before
    public void initDataUtils() {
        for (int i = 0; i < 3; i++) {
            data.add(new ArrayList<Float>());
            processed.add(new ArrayList<Float>());
        }
        timestamps = new ArrayList<>();
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
        assertEquals(30f, result[1], .1);
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
        Object bounds = constr.newInstance(0, 0, 0f, 0f, 0f, 0f, 0f);
        boolean x = (boolean) DataUtils.class
                .getDeclaredMethod("accept", bounds.getClass())
                .invoke(null, bounds);
        assertEquals(true, x);

        bounds = constr.newInstance(-1, 0, 0f, 0f, 0f, 0f, 0f);
        x = (boolean) DataUtils.class
                .getDeclaredMethod("accept", bounds.getClass())
                .invoke(null, bounds);
        assertEquals(x, true);
    }

    @Test
    public void testGetLastMatchingIndexOfFirst(){
        TimeSeriesBase.Builder builder1 = TimeSeriesBase.builder();
        TimeSeriesBase.Builder builder2 = TimeSeriesBase.builder();
        for(int i=0; i<2; i++){
            builder1 = builder1.add(i, 0);
        }
        for(int i=0; i<10; i++){
            builder1 = builder1.add(i+2, i);
            builder2 = builder2.add(i, i);
        }
        TimeSeries t1 = builder1.build();
        TimeSeries t2 = builder2.build();
        WarpPath path = FastDTW.compare(t1, t2, Distances.EUCLIDEAN_DISTANCE).getPath();
        int index = DataUtils.getLastMatchingIndexOfFirst(path);
        //Last matching index should be third, since we added 2 extra values
        assertEquals(2, index);
    }

    @Test
    public void testGetFirstMatchingIndexOfLast(){
        TimeSeriesBase.Builder builder1 = TimeSeriesBase.builder();
        TimeSeriesBase.Builder builder2 = TimeSeriesBase.builder();
        for(int i=0; i<10; i++){
            builder1 = builder1.add(i, i);
            builder2 = builder2.add(i, i);
        }
        for(int i=0; i<2; i++){
            builder1 = builder1.add(i+10, 10);
        }
        TimeSeries t1 = builder1.build();
        TimeSeries t2 = builder2.build();
        WarpPath path = FastDTW.compare(t1, t2, Distances.EUCLIDEAN_DISTANCE).getPath();
        int index = DataUtils.getFirstMatchingIndexOfLast(path);
        assertEquals(9, index);
    }

    @Test
    public void testSubSeries(){
        TimeSeriesBase.Builder builder = TimeSeriesBase.builder();
        for(int i=0; i<10; i++){
            builder = builder.add(i, i);
        }
        TimeSeries ts = builder.build();
        assertEquals(10, ts.size());
        ts = DataUtils.subSeries(ts, 2, 8);
        assertEquals(2, ts.getMeasurement(0, 0), 0);
        assertEquals(7, ts.getMeasurement(5, 0), 0);
        assertEquals(6, ts.size());
    }

    @Test
    public void processDoesNotCrash(){ 
        timestamps = new ArrayList<>();
        DataUtils.init(data, timestamps, processed);
        long timestamp = 1000000000L;
        Random random = new Random();
        for(int i=0; i<100; i++){
            float[] values = {random.nextFloat(), random.nextFloat(), random.nextFloat()};
            DataUtils.process(values, timestamp+100000000*i);
        }
    }

}