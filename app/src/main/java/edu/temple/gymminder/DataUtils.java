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

    static float sum(float[] floats){
        float sum = 0f;
        for(float f : floats) sum+=f;
        return sum;
    }

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

    static void process(SensorEvent event) {
        for (int i = 0; i < 3; i++) {
            float x = Math.abs(event.values[i] > 0.09 ? event.values[i] : 0);
            float duration = event.timestamp - timestamps.get(timestamps.size()-1) * CONVERSION;
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
                applySGFilterRealtime(data.size(), data.get(i), processedData.get(i));
                avgNode = null;
            } else if (duration == .1) {
                data.get(i).add(x);
                applySGFilterRealtime(data.size(), data.get(i), processedData.get(i));
                avgNode = null;
            }
        }
    }


    static ArrayList<Float> applySavitzkyGolayFilter(ArrayList<Float> data){
        ArrayList<Float> filtered = new ArrayList<>(data.size());
        for(int i=0; i < filtered.size(); i++){
            for(int j=0; j < SG_FILTER.length; i++){
                float sum = 0;
                if(i + j - SG_FILTER.length/2 > 0 && i + j - SG_FILTER.length/2 < data.size()){
                    sum+= SG_FILTER[j] * data.get(i+j-SG_FILTER.length/2);
                }
                filtered.add(i, sum/sum(SG_FILTER));
            }
        }
        return data;
    }

    static void applySGFilterRealtime(int index, ArrayList<Float> data, ArrayList<Float> processedData){
        for(int i=0; i<SG_FILTER.length; i++){
            float sum = 0;
            if(i + index - SG_FILTER.length/2 > 0 && i + index - SG_FILTER.length/2 < data.size()){
                sum += SG_FILTER[i] * data.get(i + index - SG_FILTER.length/2);
            }
            processedData.add(index, sum/sum(SG_FILTER));
        }
    }

}
