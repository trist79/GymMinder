package edu.temple.gymminder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by nickdellosa on 4/26/17.
 */

public class ClassifierCoefficientsTest {

    public ArrayList<DataUtils.DetectedBounds> goodBounds;
    public ArrayList<DataUtils.DetectedBounds> badBounds;

    @Before
    public void setup() {
        goodBounds = new ArrayList<>(Arrays.asList(
                new DataUtils.DetectedBounds(0, 5, 20.0, 10.0, -10.0, 2, 4),
                new DataUtils.DetectedBounds(10, 15, 19.5, 9.5, -10.0, 3, 2)
                ));

        badBounds = new ArrayList<>(Arrays.asList(
                new DataUtils.DetectedBounds(5, 10, 8.0, 5.0, -3.0, 4, 16),
                new DataUtils.DetectedBounds(15, 20, 4.0, 2.0, -2.0, .5, .25)
        ));
    }

    @Test
    public void testGenerateCoefficients() {
        ClassifierCoefficients cc = ClassifierCoefficients.generateCoefficients(goodBounds, badBounds);
        assertNotNull(cc);
        assertEquals(1.0E-8, cc.coefpure, 0);
        assertEquals(-24.429883835863492, cc.coefdst, 0);
        assertEquals(-1.5326341916401822, cc.coefmax, 0);
        assertEquals(0.7805559275507329, cc.coefmin, 0);
        assertEquals(1.5322204671634234, cc.coefsd, 0);
        assertEquals(-0.8807346503255455, cc.coefrms, 0);
        assertEquals(-0.665740233051311, cc.coefdur, 0);
    }
}
