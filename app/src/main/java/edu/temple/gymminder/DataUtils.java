package edu.temple.gymminder;

import android.hardware.SensorEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rober_000 on 2/10/2017.
 */

public class DataUtils {

    public static final float CONVERSION = 1.0f / 1000000000.0f;
    public static final float[] SG_FILTER = {-2, 3, 6, 7, 6, 3, -2};
    public static final float FILTER_SUM = sum(SG_FILTER);

    private static float[] avgNode = null;
    private static ArrayList<ArrayList<Float>> data;
    private static ArrayList<ArrayList<Float>> processedData;
    private static ArrayList<Long> timestamps;


    static void init(ArrayList<ArrayList<Float>> dataList, ArrayList<Long> time) {
        data = dataList;
        timestamps = time;
        avgNode = null;
    }

    static void init(ArrayList<ArrayList<Float>> dataList, ArrayList<Long> time, ArrayList<ArrayList<Float>> processedDataList) {
        data = dataList;
        timestamps = time;
        processedData = processedDataList;
        avgNode = null;
    }

    /**
     * @param x x acceleration to be added into array
     * @param y y acceleration to be added into array
     * @param z z acceleration to be added into array
     * @param alpha low pass filter parameter
     */
    static void addWithLowPassFilter(float x, float y, float z, float alpha) {
        if (data.get(0).size() > 0) {
            float ox = data.get(0).get(data.get(0).size() - 1);
            float oy = data.get(1).get(data.get(0).size() - 1);
            float oz = data.get(2).get(data.get(0).size() - 1);
            data.get(0).add(ox + alpha * (x - ox));
            data.get(1).add(oy + alpha * (y - oy));
            data.get(2).add(oz + alpha * (z - oz));
        } else {
            addUnfiltered(x, y, z);
        }
    }

    static void addUnfiltered(float x, float y, float z) {
        data.get(0).add(x);
        data.get(1).add(y);
        data.get(2).add(z);
    }

    /**
     * This is an intermediate step in approximating the definite integral of data points, for
     * use with sum
     * @param list list of float values for which to calculate riemann rectangles
     * @return array of riemann rectangles
     */
    static float[] riemann(List<Float> list) {
        float[] velocity = new float[list.size()-1];
        Iterator<Float> iterator = list.listIterator();
        int i = 0;
        while (iterator.hasNext() && timestamps.size() > i + 1) {
            velocity[i] = iterator.next() * ((timestamps.get(i + 1) - timestamps.get(i)) * CONVERSION);
            i++;
        }
        return velocity;
    }

    /**
     * @param floats list of floats to be summed
     * @return the sum of values in floats
     */
    static float sum(float[] floats){
        float sum = 0f;
        for(float f : floats) sum+=f;
        return sum;
    }

    /**
     * @param floats array of floats for which to find the maximum and average value
     * @return a float array whose first value is the max and whose second value is the average
     */
    static float[] maxAndAvg(float[] floats) {
        float f[] = new float[2];
        float max = floats[0];
        float sum = 0;
        for (int i = 0; i < floats.length; i++) {
            max = max > floats[i] ? max : floats[i];
            sum += floats[i];
        }
        f[0] = max;
        f[1] = sum / floats.length;
        return f;
    }

    /**
     * Adds data point into the uniformly-spaced and smoothed data array
     *
     * avgNode is used by this method when the real polling frequency of the device
     * is higher than the desired polling frequency, and represents all data points not yet
     * placed into the data array because the time difference is still too small
     *
     * If the polling frequency of the device is lower than the desired polling frequency it
     * interpolates a data point at the desired frequency using the newest data point and the
     * data point most recently added into the data array
     *
     * @param event SensorEvent containing data point to be processed
     */
    static void process(SensorEvent event) {
        for (int i = 0; i < 3; i++) {
            float x = Math.abs(event.values[i] > 0.09 ? event.values[i] : 0);
            float duration = event.timestamp - timestamps.get(timestamps.size()-1) * CONVERSION;
                //TODO: Separate averaging and interpolation into their own methods
                if (duration < .1) {
                //average the points with sum node
                if (avgNode == null) {
                    avgNode = new float[]{x, duration};
                } else {
                    avgNode[0] = avgNode[0] * (avgNode[1] / (avgNode[1] + duration))
                            + x * (duration / (avgNode[1] + duration));
                    avgNode[1] = avgNode[1] + duration;
                    duration = avgNode[1];
                }
                //After averaging is done it's possible we now need to interpolate
            }
            if (duration > .1) {
                //interpolate
                float old = data.get(i).get(data.get(i).size() - 1);
                x = old + (.1f) * (x - old) / (duration);
                data.get(i).add(x);
                applySGFilterRealtime(processedData.get(i).size(), data.get(i), processedData.get(i));
                avgNode = null;
            } else if (duration == .1) {
                data.get(i).add(x);
                applySGFilterRealtime(processedData.get(i).size(), data.get(i), processedData.get(i));
                avgNode = null;
            }
        }
    }

    /**
     *
     * @param data array of data points to be smoothed
     * @return smoothed data points
     */
    static ArrayList<Float> applySavitzkyGolayFilter(ArrayList<Float> data){
        ArrayList<Float> filtered = new ArrayList<>(data.size());
        for(int i=0; i < data.size(); i++){
            float sum = 0;
            for(int j=0; j < SG_FILTER.length; j++){
                if(i + j - SG_FILTER.length/2 >= 0 && i + j - SG_FILTER.length/2 < data.size()){
                    sum+= SG_FILTER[j] * data.get(i+j-SG_FILTER.length/2);
                }
            }
            filtered.add(i, sum/FILTER_SUM);
        }
        return filtered;
    }

    /**
     *  @param index the index of the data point to apply the filter to
     *  @param data data array to be filtered
     *  @param processedData data array to be inserted into
     */
    static void applySGFilterRealtime(int index, ArrayList<Float> data, ArrayList<Float> processedData){
        float sum = 0;
        for(int i=0; i<SG_FILTER.length; i++){
            if(i + index - SG_FILTER.length/2 >= 0 && i + index - SG_FILTER.length/2 < data.size()){
                sum += SG_FILTER[i] * data.get(i + index - SG_FILTER.length/2);
            }
        }
        processedData.add(index, sum/FILTER_SUM);
    }


}
