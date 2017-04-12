package edu.temple.gymminder;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.dtw.WarpPath;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
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
        //Run filter again to simulate re-processing data points at each run of DataUtils.process()
        for (int i = 0; i < 10; i++) {
            DataUtils.applySGFilterRealtime(i, data, processed.get(0));
        }
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
    public void testSuccsesfulRemovalOfPeaks() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        Class peakClass = getAccessibleDataUtilsClass("Peak");
        Constructor constr = peakClass.getDeclaredConstructors()[0];
        constr.setAccessible(true);
        Object originalPeak = constr.newInstance(0, 10f);
        ArrayList<Object> peaks = new ArrayList<>();
        Random rand = new Random();
        for(int i=0;i<10;i++){
            peaks.add(constr.newInstance(0, rand.nextFloat()*3));
        }
        assertEquals(10, peaks.size());
        peaks = (ArrayList<Object>) DataUtils.class
                .getDeclaredMethod("reducePeaks", ArrayList.class, originalPeak.getClass())
                .invoke(null, peaks, originalPeak);
        assertEquals(0, peaks.size());
    }

    @Test
    public void testNonRemovalOfValidPeaks() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, NoSuchFieldException {
        Class peakClass = getAccessibleDataUtilsClass("Peak");
        Constructor constr = peakClass.getDeclaredConstructors()[0];
        constr.setAccessible(true);
        Object originalPeak = constr.newInstance(0, 10f);
        ArrayList<Object> peaks = new ArrayList<>();
        Random rand = new Random();
        for(int i=0;i<10;i++){
            peaks.add(constr.newInstance(0, 3.34f+rand.nextFloat()*7f));
        }
        assertEquals(10, peaks.size());
        peaks = (ArrayList<Object>) DataUtils.class
                .getDeclaredMethod("reducePeaks", ArrayList.class, originalPeak.getClass())
                .invoke(null, peaks, originalPeak);
        assertEquals(10, peaks.size());
    }

    @Test
    public void testAccept() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, NoSuchFieldException {
        Class boundsClass = getAccessibleDataUtilsClass("DetectedBounds");
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
            DataUtils.process(values, timestamp+100000000L*i);
        }
    }

    @Test
    public void processCorrectlyAddsValuesWithTimestampsGreaterThanPeriod(){
        processDoesNotCrash();
        assertEquals(100, timestamps.size());
        assertEquals(100, data.get(0).size());
        assertEquals(100, processed.get(0).size());
    }

    @Test
    public void processCorrectlyAddsValuesWithTimestampsLessThanPeriod(){
        timestamps = new ArrayList<>();
        DataUtils.init(data, timestamps, processed);
        long timestamp = 1000000000L;
        Random random = new Random();
        for(int i=0; i<100; i++){
            float[] values = {random.nextFloat(), random.nextFloat(), random.nextFloat()};
            DataUtils.process(values, timestamp+50000000L*i);
        }
        //Should be 1+floor(99/2)
        assertEquals(50, timestamps.size());
        assertEquals(50, data.get(0).size());
        assertEquals(50, processed.get(0).size());
    }

    @Mock
    BufferedReader reader;

    @Test
    public void testLoadRepetitionPatternTimeSeries() throws IOException, NoSuchFieldException, IllegalAccessException {
        when(reader.readLine())
                .thenReturn("1.4,2,3.6,4,5,6.3,7,8,9")
                .thenReturn("5,6.3");
        DataUtils.loadRepetitionPatternTimeSeries(reader);
        //TODO: use reflection and set these back to private... do it with some other things too...
        assertNotNull(DataUtils.repPeak);
        assertNotNull(DataUtils.repTimeSeries);
        assertEquals(9, DataUtils.repTimeSeries.size());
        Object o = DataUtils.repPeak;
        assertEquals(5, getAccessibleDataUtilsClass("Peak").getDeclaredField("index").get(o));
        assertEquals(6.3f, (float) getAccessibleDataUtilsClass("Peak").getDeclaredField("amplitude").get(o), 0.01);
    }

    @Test
    public void testCalcFeatures() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, NoSuchFieldException {
        Class peakClass = getAccessibleDataUtilsClass("Peak");
        TimeSeriesBase.Builder builder = TimeSeriesBase.builder();
        for(int i=0; i<10; i++){
            builder = builder.add(i, i);
        }
        TimeSeries t1 = builder.build();
        TimeSeries t2 = builder.build();
        Constructor constr = peakClass.getDeclaredConstructors()[0];
        constr.setAccessible(true);
        Object originalPeak = constr.newInstance(0, 9f);
        double[] features = (double[]) DataUtils.class
                .getMethod("calcFeatures", TimeSeries.class, originalPeak.getClass(), TimeSeries.class)
                .invoke(null, t1, originalPeak, t2);
        double[] expected = { 0, 9, 0, 2.87228, 5.33853};
        assertArrayEquals(expected, features, .001);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsOnlyPeakValue() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        processed.get(0).add(7f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(amp, 7, 0.0001);
        assertEquals(index, 50);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsFinalPeakValue() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        processed.get(0).add(7f);
        processed.get(0).add(8f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(amp, 8, 0.0001);
        assertEquals(index, 51);
    }

    @Test
    public void testMovingZScorePeakDetectionDoesNotReturnFinalMaxValueOutsideOfPeak() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        processed.get(0).add(7f);
        processed.get(0).add(1f);
        processed.get(0).add(8f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(amp, 7, 0.0001);
        assertEquals(index, 50);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsNullWithNoPeakValues()  {
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        assertNull(o);
    }

    @Test
    public void testMovingZScorePeakDetectionDoesNotReturnPeakWithGradualIncrease(){
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        assertNull(o);
    }

    @Test
    public void testMovingZScorePeakDetectionDoesNotUpdateZAfterFoundPeak() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        for(int i=0; i<50; i++) processed.get(0).add(50f);
        processed.get(0).add(51f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);

        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(51, amp, 0.0001);
        assertEquals(100, index);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsNullWithNegativeValues(){
        int lag=5, window=20, start=50;
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        processed.get(0).add(-50f);
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(lag, window, 10, 0);
        assertNull(o);
    }

    @Test
    public void testMovingZScorePeakDetectionWorksWithNoiseValues() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) processed.get(0).add((float) Math.random());
        for(int i=0; i<50; i++) processed.get(0).add(50f);
        processed.get(0).add(51f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 50, 0);

        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(51, amp, 0.0001);
        assertEquals(100, index);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsNullWithPeakBeforeStart() {
        int lag=5, window=20, start=50;
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        processed.get(0).add(50f);
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(lag, window, 10, start+window-lag+1);
        assertNull(o);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsObjectWithPeakAtStartOfStart(){
        int lag=5, window=20, start=50;
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        processed.get(0).add(50f);
        for(int i=0; i<50; i++) processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(lag, window, 10, start+window-lag);
        assertNotNull(o);
    }

    public Class getAccessibleDataUtilsClass(String className) throws NoSuchFieldException {
        Class[] classes = DataUtils.class.getDeclaredClasses();
        Class peakClass = null;
        for (Class c : classes) {
            if (c.getName().equals("edu.temple.gymminder.DataUtils$" + className)) {
                peakClass = c;
                break;
            }
        }
        switch(className){
            case "Peak":
                peakClass.getDeclaredField("amplitude").setAccessible(true);
                peakClass.getDeclaredField("index").setAccessible(true);
                break;
            case "DetectedBounds":
                //Don't need any fields for now o3o
                break;
        }
        return peakClass;
    }

}