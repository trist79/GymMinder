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
        for(int i=0; i<3;i++){
            data.add(new ArrayList<Float>());
            processed.add(new ArrayList<Float>());
        }
        ArrayList<Long> timestamps = new ArrayList<>();
        for(int i=0;i<10;i++){
            timestamps.add( PERIOD * i * 1000000000);
        }
        DataUtils.init(data, timestamps, processed);
    }

    @Test
    public void testDataUtilsConstantRiemann(){
        ArrayList<Float> data = new ArrayList<>();
        for(int i=0;i<10;i++){
            data.add(1f);
        }
        float f[] = DataUtils.riemann(data);
        for(int i=0;i<9;i++){
            assertEquals(PERIOD, f[i], .1);
        }
    }

    @Test
    public void testDataUtilsConstantIntegration(){
        ArrayList<Float> data = new ArrayList<>();
        for(int i=0;i<10;i++){
            data.add(1f);
        }
        float f = DataUtils.sum(DataUtils.riemann(data));
        assertEquals(PERIOD * 9, f, 1);
    }

    @Test
    public void testDataUtilsLinearRiemann(){
        ArrayList<Float> data = new ArrayList<>();
        for(int i=0;i<10;i++){
            data.add(1f * i);
        }
        float f[] = DataUtils.riemann(data);
        for(int i=0;i<9;i++){
            assertEquals(i * PERIOD, f[i], .1);
        }
    }

    @Test
    public void testDataUtilsLinearIntegration(){
        ArrayList<Float> data = new ArrayList<>();
        for(int i=0;i<10;i++){
            data.add(1f * i);
        }
        float f = DataUtils.sum(DataUtils.riemann(data));
        assertEquals(360, f, 1);
    }

    @Test
    public void applySavitzkyGolayFilterDoesNotCrash(){
        ArrayList<Float> data = new ArrayList<Float>();
        for(int i=0;i<10;i++){
            data.add(1f);
        }
        DataUtils.applySavitzkyGolayFilter(data);
    }

    @Test
    public void realTimeSavitzkyGolayFilterDoesNotCrash(){
        for(int i=0; i<10; i++){
            data.get(0).add((float) Math.random());
        }
        for(int i=0; i<10;i++){
            DataUtils.applySGFilterRealtime(i, data.get(0), processed.get(0));
        }
    }


}