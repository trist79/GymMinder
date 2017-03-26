package edu.temple.gymminder;

import android.hardware.SensorEvent;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.dtw.TimeWarpInfo;
import com.fastdtw.dtw.WarpPath;
import com.fastdtw.matrix.ColMajorCell;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.util.Distances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rober_000 on 2/10/2017.
 */

public class DataUtils {

    //TODO: Refactor at least data processing part to instantiated class

    public static final float MS2S_CONVERSION = 1.0f / 1000000000.0f;
    public static final long SECOND = 1000000000;
    public static final float[] SG_FILTER = {-2, 3, 6, 7, 6, 3, -2};
    public static final float FILTER_SUM = sum(SG_FILTER);
    private static final float PERIOD = .1f;
    private static final double EXPANSION_VALUE = 1.5;
    private static final double PEAK_SIMILARITY_FACTOR = 3;
    private static final double ERROR = 0.00001;

    private static float[] avgNode = null;
    private static ArrayList<ArrayList<Float>> data;
    private static ArrayList<ArrayList<Float>> processedData;
    private static ArrayList<Long> timestamps;
    private static HashMap<Integer, Peak> peaks = new HashMap<>(); //TODO: more intelligent initial capacity using right window size

    //TODO initialize these values from stored value
    public static Peak repPeak;
    public static TimeSeries repTimeSeries;


    static void init(ArrayList<ArrayList<Float>> dataList, ArrayList<Long> time) {
        data = dataList;
        timestamps = time;
        processedData = null;
        avgNode = null;
    }

    static void init(ArrayList<ArrayList<Float>> dataList, ArrayList<Long> time, ArrayList<ArrayList<Float>> processedDataList) {
        data = dataList;
        timestamps = time;
        processedData = processedDataList;
        avgNode = null;
    }

    /**
     * @param x     x acceleration to be added into array
     * @param y     y acceleration to be added into array
     * @param z     z acceleration to be added into array
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
     *
     * @param list list of float values for which to calculate riemann rectangles
     * @return array of riemann rectangles
     */
    static float[] riemann(List<Float> list) {
        float[] velocity = new float[list.size() - 1];
        Iterator<Float> iterator = list.listIterator();
        int i = 0;
        while (iterator.hasNext() && timestamps.size() > i + 1) {
            velocity[i] = iterator.next() * ((timestamps.get(i + 1) - timestamps.get(i)) * MS2S_CONVERSION);
            i++;
        }
        return velocity;
    }

    /**
     * @param floats list of floats to be summed
     * @return the sum of values in floats
     */
    static float sum(float[] floats) {
        float sum = 0f;
        for (float f : floats) sum += f;
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
     * <p>
     * avgNode is used by this method when the real polling frequency of the device
     * is higher than the desired polling frequency, and represents all data points not yet
     * placed into the data array because the time difference is still too small where
     * avgNode[0] = value, and avgNode[1] = duration
     * <p>
     * If the polling frequency of the device is lower than the desired polling frequency it
     * interpolates a data point at the desired frequency using the newest data point and the
     * data point most recently added into the data array
     *
     * @param values    event values
     * @param timestamp timestamp of event
     */
    static void process(float[] values, long timestamp) {
        int i = 0; //TODO: determine which index is the major axis

        float x = Math.abs(values[i]) > 0.009 ? values[i] : 0;
        //First time adding a node, just add it lel
        if(timestamps.size()==0){
            timestamps.add(timestamp);
            data.get(i).add(x);
            processedData.get(i).add(x);
            return;
        }
        float duration = (timestamp - timestamps.get(timestamps.size() - 1)) * MS2S_CONVERSION;

        if ((duration + ERROR) < PERIOD || avgNode != null) {
            //average the points with sum node
            avgNode = average(avgNode, x, duration);
            duration = avgNode[1];
            x = avgNode[0];
        }
        if ((duration + ERROR) >= PERIOD) {
            //interpolate if needed
            if ((duration + ERROR) > PERIOD) x = interpolate(x, duration, i);
            //We can approximate timestamp value by adding .1s to previous value
            //Maybe not the best idea since it (maybe) causes drift when we interpolate, idk :d
            timestamps.add(timestamps.get(timestamps.size()-1)+ (long)(SECOND*PERIOD));
            data.get(i).add(x);
            //TODO: make this process most recent 3 nodes at once
            applySGFilterRealtime(processedData.get(i).size(), data.get(i), processedData.get(i));
            avgNode = null;
            Peak newPeak = detectPeak();
            //TODO: Probably want to put this in a thread that enqueues new peaks to check
            if (newPeak != null) {
                    /*
                        The key for our HashMap is the index of the new peak, which is the current
                        size of processedData at the time of insertion, plus the difference between
                        the size of repTimeSeries and the index of repPeak multiplied by
                        EXPANSION_VALUE.
                     */
                peaks.put((int) (newPeak.index + EXPANSION_VALUE *
                        (repTimeSeries.size() - repPeak.index)), newPeak);
            }
            if (peaks.containsKey(processedData.get(0).size())) {
                    /*
                        Because we used an index for our HashMap key value, we can use the
                        current index to detect if any peaks are ready to be examined
                     */
                TimeSeriesBase.Builder builder = TimeSeriesBase.builder();
                for(int j=0; j<processedData.get(i).size(); j++) builder = builder.add(j, processedData.get(i).get(j));
                TimeSeries t1 = builder.build();
                DetectedBounds bounds = detectBounds(t1, peaks.get(processedData.size()));

                peaks.remove(processedData.get(i).size());
                if (accept(bounds)) {
                        /*
                            This was a valid repetition, so we want to vibrate and remove any
                            potential peaks that we now know are contained within the repetition
                         */
                    for (int j = processedData.get(i).size() + 1; j < processedData.get(i).size() + 1 + bounds.e; j++) {
                        if (peaks.containsKey(j)) peaks.remove(j);
                    }
                }

            }

        }

    }


    private static Peak detectPeak() {
        //TODO: Implement selection of peak candidates: https://www.ncbi.nlm.nih.gov/pubmed/18269982
        return null;
    }

    /**
     *
     * @param x data value to be interpolated
     * @param duration duration > PERIOD since last added node
     * @param i index of data array being used for interpolation
     * @return interpolated data value
     */
    static float interpolate(float x, float duration, int i){
        float old = data.get(i).get(data.get(i).size() - 1);
        return old + (PERIOD) * (x - old) / (duration);
    }

    /**
     *
     * @param avgNode node to be modified
     * @param newValue new value to be added to average
     * @param newDuration new duration to be added to average
     * @return the modified avgNode
     */
    static float[] average(float[] avgNode, float newValue, float newDuration) {
        if (avgNode == null) {
            avgNode = new float[]{newValue, newDuration};
        } else {
            avgNode[0] = avgNode[0] * (avgNode[1] / (avgNode[1] + newDuration))
                    + newValue * (newDuration / (avgNode[1] + newDuration));
            avgNode[1] = avgNode[1] + newDuration;
        }
        return avgNode;
    }


    /**
     * @param data array of data points to be smoothed
     * @return smoothed data points
     */
    static ArrayList<Float> applySavitzkyGolayFilter(ArrayList<Float> data) {
        ArrayList<Float> filtered = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            float sum = 0;
            for (int j = 0; j < SG_FILTER.length; j++) {
                if (i + j - SG_FILTER.length / 2 >= 0 && i + j - SG_FILTER.length / 2 < data.size()) {
                    sum += SG_FILTER[j] * data.get(i + j - SG_FILTER.length / 2);
                }
            }
            filtered.add(i, sum / FILTER_SUM);
        }
        return filtered;
    }

    /**
     * @param index         the index of the data point to apply the filter to
     * @param data          data array to be filtered
     * @param processedData data array to be inserted into
     */
    static void applySGFilterRealtime(int index, ArrayList<Float> data, ArrayList<Float> processedData) {
        float sum = 0;
        for (int i = 0; i < SG_FILTER.length; i++) {
            if (i + index - SG_FILTER.length / 2 >= 0 && i + index - SG_FILTER.length / 2 < data.size()) {
                sum += SG_FILTER[i] * data.get(i + index - SG_FILTER.length / 2);
            }
        }
        processedData.add(index, sum / FILTER_SUM);
    }

    public static ArrayList<Peak> reducePeaks(ArrayList<Peak> peaks, Peak originalPeak){
        ArrayList<Peak> result = new ArrayList<>();
        for(Peak p : peaks){
            if(p.amplitude >= ( Math.pow(PEAK_SIMILARITY_FACTOR, -1) * originalPeak.amplitude )){
                result.add(p);
            }
        }
        return result;
    }

    public static TimeSeries subSeries(TimeSeries series, int start, int end){
        TimeSeriesBase.Builder builder = TimeSeriesBase.builder();
        for(int i = start; i<end; i++){
            builder.add(series.getTimeAtNthPoint(i), series.getMeasurement(i, 0));
        }
        return builder.build();
    }

    public static int getLastMatchingIndexOfFirst(WarpPath path){
        ColMajorCell cell = path.get(0);
        int startIndexI = cell.getRow(); //This might need to be cell.getCol()
        int i;
        for(i=1; startIndexI == cell.getRow() && i<path.size(); i++){
            cell = path.get(i);
        }
        //Subtract 2 because the index is not valid, and then i++ was called again
        return path.get(i-2).getCol(); //If cell.getRow() changes, so does this
    }

    public static int getFirstMatchingIndexOfLast(WarpPath path){
        ColMajorCell cell = path.get(path.size()-1);
        int endIndexI = cell.getRow(); //ditto comments above
        int i;
        for(i=path.size()-2; endIndexI == cell.getRow() && i>=0; i--){
            cell = path.get(i);
        }
        //Work backwards this time so add the two back
        return path.get(i+2).getCol();
    }

    /**
     *
     * @param t1        acceleration stream
     * @param t1Peak    peak candidate for acceleration stream
     * @return          DetectedBounds representing bounds of candidate for repetition
     */
    public static DetectedBounds detectBounds(TimeSeries t1, Peak t1Peak){
        int s = (int) (t1Peak.index - EXPANSION_VALUE * repPeak.index);
        int e = (int) (t1Peak.index - EXPANSION_VALUE * (repTimeSeries.size() - repPeak.index));
        t1 = subSeries(t1, s, e);
        TimeWarpInfo info = FastDTW.compare(t1, repTimeSeries, Distances.EUCLIDEAN_DISTANCE);
        //Last element s in R -> C[0]
        s = getLastMatchingIndexOfFirst(info.getPath());
        //First element e in R -> C[n]
        e = getFirstMatchingIndexOfLast(info.getPath());
        //Find min, mean, std, rms, dur in R[s':e']
        t1 = subSeries(t1, s, e);
        //TODO: confirm this is what's meant by normalized distance
        double dst = FastDTW.compare(t1, repTimeSeries, Distances.EUCLIDEAN_DISTANCE).getDistance();  //TODO maybe find a way to do this faster
        double max = t1Peak.amplitude;
        double min = Double.MAX_VALUE;
        double mean = 0;
        double std = 0;
        double rms = 0;
        for(int i=0; i<t1.size(); i++){
            double value = t1.getMeasurement(i, 0);
            min = min < value ? min : value;
            mean += value;
            rms += value*value;
        }
        mean /= t1.size();
        for(int i=0; i<t1.size(); i++){
            double value = t1.getMeasurement(i, 0);
            std += Math.pow((value - mean), 2);
        }
        rms = Math.sqrt((rms / t1.size()));
        std = Math.sqrt((std/(t1.size()-1)));
        return new DetectedBounds(s, e, dst, max, min, std, rms);
    }

    public static boolean accept(DetectedBounds bounds){
        //TODO logistic regression to find coefficients
        final double b0 = 0, b1 = 1, b2 = 1, b3 = 1, b4 = 1, b5 = 1, b6 = 1;
        double res = b0 + (b1 * bounds.dst) + (b2 * bounds.max) + (b3 * bounds.min) + (b4 * bounds.sd)
                + (b5 * bounds.rms) + (b6 * bounds.dur);
        return 1/(1+Math.exp(-1.0*res)) >= .5;
    }

    public static void loadRepetitionPatternTimeSeries(BufferedReader reader){
        TimeSeriesBase.Builder builder = TimeSeriesBase.builder();
        try {
            String line = reader.readLine();
            String[] numbers = line.split(",");
            int i = 0;
            for(String s : numbers){
                builder = builder.add(i++, Float.parseFloat(s));
            }
            line = reader.readLine();
            numbers = line.split(",");
            repTimeSeries = builder.build();
            repPeak = new Peak(Integer.parseInt(numbers[0]), Float.parseFloat(numbers[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class DetectedBounds {
        double dst, max, min, sd, rms, dur;
        int s, e;

        public DetectedBounds(int s, int e, double dst, double max, double min, double sd, double rms){
            this.s = s; this.e = e; this.dst = dst; this.max = max; this.min = min; this.sd = sd;
            this.rms = rms; this.dur = e - s;
        }
    }

    private static class Peak {
        float amplitude;
        int index;

        public Peak(int index, float amplitude){
            this.index = index;
            this.amplitude = amplitude;
        }
    }

}
