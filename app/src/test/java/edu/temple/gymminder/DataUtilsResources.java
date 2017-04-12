package edu.temple.gymminder;

import org.junit.rules.ExternalResource;

import java.util.ArrayList;

/**
 * Created by rober_000 on 4/12/2017.
 */

public class DataUtilsResources extends ExternalResource {

    public final long PERIOD = 10L;
    public final int S2MS_CONVERSION = 1000000000;
    ArrayList<ArrayList<Float>> data = new ArrayList<>();
    ArrayList<ArrayList<Float>> processed = new ArrayList<>();
    ArrayList<Long> timestamps;

    protected void before(){
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
