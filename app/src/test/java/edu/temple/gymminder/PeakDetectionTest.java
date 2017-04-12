package edu.temple.gymminder;

import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by rober_000 on 4/12/2017.
 */

public class PeakDetectionTest {

    @Rule
    public final DataUtilsResources res = new DataUtilsResources();

    @Test
    public void testMovingZScorePeakDetectionReturnsOnlyPeakValue() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = res.getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        res.processed.get(0).add(7f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(amp, 7, 0.0001);
        assertEquals(index, 50);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsFinalPeakValue() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = res.getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        res.processed.get(0).add(7f);
        res.processed.get(0).add(8f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(amp, 8, 0.0001);
        assertEquals(index, 51);
    }

    @Test
    public void testMovingZScorePeakDetectionDoesNotReturnFinalMaxValueOutsideOfPeak() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = res.getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        res.processed.get(0).add(7f);
        res.processed.get(0).add(1f);
        res.processed.get(0).add(8f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(amp, 7, 0.0001);
        assertEquals(index, 50);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsNullWithNoPeakValues()  {
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        assertNull(o);
    }

    @Test
    public void testMovingZScorePeakDetectionDoesNotReturnPeakWithGradualIncrease(){
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);
        assertNull(o);
    }

    @Test
    public void testMovingZScorePeakDetectionDoesNotUpdateZAfterFoundPeak() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = res.getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        for(int i=0; i<50; i++) res.processed.get(0).add(50f);
        res.processed.get(0).add(51f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 5, 0);

        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(51, amp, 0.0001);
        assertEquals(100, index);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsNullWithNegativeValues(){
        int lag=5, window=20, start=50;
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        res.processed.get(0).add(-50f);
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(lag, window, 10, 0);
        assertNull(o);
    }

    @Test
    public void testMovingZScorePeakDetectionWorksWithNoiseValues() throws NoSuchFieldException, IllegalAccessException {
        Class peakClass = res.getAccessibleDataUtilsClass("Peak");
        for(int i=0; i<50; i++) res.processed.get(0).add((float) Math.random());
        for(int i=0; i<50; i++) res.processed.get(0).add(50f);
        res.processed.get(0).add(51f);
        Object o = DataUtils.movingZScorePeakDetection(5, 20, 50, 0);

        float amp = (float) peakClass.getDeclaredField("amplitude").get(o);
        int index = (int) peakClass.getDeclaredField("index").get(o);
        assertEquals(51, amp, 0.0001);
        assertEquals(100, index);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsNullWithPeakBeforeStart() {
        int lag=5, window=20, start=50;
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        res.processed.get(0).add(50f);
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(lag, window, 10, start+window-lag+1);
        assertNull(o);
    }

    @Test
    public void testMovingZScorePeakDetectionReturnsObjectWithPeakAtStartOfStart(){
        int lag=5, window=20, start=50;
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        res.processed.get(0).add(50f);
        for(int i=0; i<50; i++) res.processed.get(0).add(1f);
        Object o = DataUtils.movingZScorePeakDetection(lag, window, 10, start+window-lag);
        assertNotNull(o);
    }

    @Test
    public void testSuccsesfulRemovalOfPeaks() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        Class peakClass = res.getAccessibleDataUtilsClass("Peak");
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
        Class peakClass = res.getAccessibleDataUtilsClass("Peak");
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
}
