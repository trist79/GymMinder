package edu.temple.gymminder;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by rober_000 on 4/12/2017.
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        PeakDetectionTest.class,
        BasicDataUtilsMethodsTest.class,
        PreprocessingTest.class,
        ProcessTest.class,
        ProcessRealDataTest.class,
        DTWTest.class
})
public class DataUtilsTestSuite {
}
