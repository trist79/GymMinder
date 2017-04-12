package edu.temple.gymminder;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.dtw.WarpPath;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicDataUtilsMethodsTest {

    @Rule
    public final DataUtilsResources res = new DataUtilsResources();

    @Test
    public void testDataUtilsConstantRiemann() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(1f);
        }
        float f[] = DataUtils.riemann(data);
        for (int i = 0; i < 9; i++) {
            assertEquals(res.PERIOD, f[i], .1);
        }
    }

    @Test
    public void testDataUtilsConstantIntegration() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(1f);
        }
        float f = DataUtils.sum(DataUtils.riemann(data));
        assertEquals(res.PERIOD * 9, f, 1);
    }

    @Test
    public void testDataUtilsLinearRiemann() {
        ArrayList<Float> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(1f * i);
        }
        float f[] = DataUtils.riemann(data);
        for (int i = 0; i < 9; i++) {
            assertEquals(i * res.PERIOD, f[i], .1);
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
        assertEquals(5, res.getAccessibleDataUtilsClass("Peak").getDeclaredField("index").get(o));
        assertEquals(6.3f, (float) res.getAccessibleDataUtilsClass("Peak").getDeclaredField("amplitude").get(o), 0.01);
    }

}
    

