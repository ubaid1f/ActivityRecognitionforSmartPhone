package com.example.euguzns.arforphone;

import dataobject.SensorObject;

/**
 * Created by euguzns on 2018-01-12.
 */

public class FeatureExtraction {

    public double[] getFeaturesSingle (Object obj) {
        SensorObject objFeature = (SensorObject)obj;
        double[] feature = new double[24];

        feature[0] = getAverage(objFeature.getAccX());
        feature[1] = getAverage(objFeature.getAccY());
        feature[2] = getAverage(objFeature.getAccZ());
        feature[3] = getMax(objFeature.getAccX());
        feature[4] = getMax(objFeature.getAccY());
        feature[5] = getMax(objFeature.getAccZ());
        feature[6] = getMin(objFeature.getAccX());
        feature[7] = getMin(objFeature.getAccY());
        feature[8] = getMin(objFeature.getAccZ());
        feature[9] = getStandardDeviation(objFeature.getAccX());
        feature[10] = getStandardDeviation(objFeature.getAccY());
        feature[11] = getStandardDeviation(objFeature.getAccZ());
        feature[12] = getCrossing(objFeature.getAccX());
        feature[13] = getCrossing(objFeature.getAccY());
        feature[14] = getCrossing(objFeature.getAccZ());
        feature[15] = getQuartile(objFeature.getAccX());
        feature[16] = getQuartile(objFeature.getAccY());
        feature[17] = getQuartile(objFeature.getAccZ());
        feature[18] = getMeanAbs(objFeature.getAccX());
        feature[19] = getMeanAbs(objFeature.getAccY());
        feature[20] = getMeanAbs(objFeature.getAccZ());
        feature[21] = getVariance(objFeature.getAccX());
        feature[22] = getVariance(objFeature.getAccY());
        feature[23] = getVariance(objFeature.getAccZ());

        return feature;
    }

    /**
     * get Zero Crossing
     * @return number of zero crossing
     */
    static double getCrossing(double[] signal) {
        double crossing = 0;
        double mean = 0;
        mean = getAverage(signal);
        for (int i = 0; i < signal.length - 1; i++) {
            if ((signal[i] - mean) * (signal[i + 1] - mean) < 0)
                crossing += 1;
        }
        crossing /= signal.length;

        return crossing;
    }

    /**
     * get Standard deviation
     * @return Standard deviation
     */
    static double getStandardDeviation(double[] signal) {
        double variance = getVariance(signal);
        return Math.sqrt(variance);
    }

    /**
     * get Max value
     * @return max value
     */
    static double getMax(double[] signal) {
        double max = -9999;
        for (int j = 0; j < signal.length; j++) {
            if (signal[j] > max) {
                max = signal[j];
            }
        }
        return max;
    }

    /**
     * get minimum value
     * @return minimum value
     */
    static double getMin(double[] signal) {
        double min = 9999;
        for (int j = 0; j < signal.length; j++) {
            if (signal[j] < min) {
                min = signal[j];
            }
        }
        return min;
    }

    /**
     * get sum of array data
     * @return sum of array data
     */
    public static double getSum(double[] values) {
        double sumData = 0;

        for (int i = 0; i < values.length; i++) {
            sumData += values[i];
        }
        return sumData;
    }

    /**
     * get average of array
     * @return average
     */
    public static double getAverage(double[] values) {
        double avgData = 0;

        avgData = getSum(values) / values.length;

        return avgData;
    }

    /**
     * get variance of array
     * @return variance
     */
    public static double getVariance(double[] values) {
        double varData = 0;
        double sum_varData = 0;
        double avg = getAverage(values);

        for (int i = 0; i < values.length; i++) {
            sum_varData += Math.pow((values[i] - avg), 2);
        }
        varData = sum_varData / values.length;

        return varData;
    }

    /**
     * get covariance of two arrays
     * @return covariance of two arrays
     */
    public static double getCovariance(double[] values_A, double[] values_B) {
        double a_avg = getAverage(values_A);
        double b_avg = getAverage(values_B);
        double covarData = 0;

        double[] ab_avg = new double[values_A.length];

        for (int i = 0; i < values_A.length; i++) {
            ab_avg[i] = (values_A[i] - a_avg) * (values_B[i] - b_avg);
        }

        covarData = getAverage(ab_avg);

        return covarData;
    }

    /**
     * get quartile value
     * @return quartile
     */
    public static double getQuartile(double[] values) {
        double[] result = sort(values);
        int quart = values.length / 4;
        return Math.abs((result[quart] + result[quart + 1]) / 2
                - (result[values.length - quart] + result[values.length - quart + 1]) / 2);
    }

    /**
     * get mean value of abs
     * @return meanabs
     */
    public static double getMeanAbs(double[] values) {
        double average = getAverage(values);
        double[] list = new double[values.length];
        for (int j = 0; j < values.length; j++) {
            list[j] = Math.abs(values[j] - average);
        }

        return getAverage(list);
    }

    /**
     * get median value of abs
     * @return median value of abs
     */
    public static double getMedianAbs(double[] values) {
        double median = getMedian(values);
        double list[] = new double[values.length];
        for (int j = 0; j < values.length; j++) {
            list[j] = Math.abs(values[j] - median);
        }
        return getMedian(list);
    }

    /**
     * get median value
     * @return median value
     */
    public static double getMedian(double[] values) {
        double[] result = sort(values);
        if (result.length % 2 == 1) {
            return result[(result.length / 2) + 1];
        } else {
            double a, b;
            a = result[(result.length / 2) + 1];
            b = result[(result.length / 2)];
            return (a + b) / 2.;
        }
    }

    /**
     * get range of array
     * @return maximum data - minimum data
     */
    public static double getRange(double[] value) {
        return getMax(value) - getMin(value);
    }

    /**
     * get geomean of array
     * @return geomean of array
     */
    public static double getGeomean(double[] value) {
        double result = 1;
        for (int j = 0; j < value.length; j++) {
            result *= value[j];
        }
        return Math.pow(result, (1 / value.length));
    }


    /**
     * sort array
     * @return sorted array
     */
    public static double[] sort(double[] value) {
        double[] result = new double[value.length];
        for (int j = 0; j < value.length; j++) {
            result[j] = value[j];
        }

        for (int j = 0; j < result.length; j++) {
            for (int k = 0; k < result.length - 1; k++) {
                if (result[k] > result[k + 1]) {
                    double temp = result[k];
                    result[k] = result[k + 1];
                    result[k + 1] = result[k];
                }
            }
        }

        return result;
    }

    /**
     * get harmonic value of array
     * @return harmonic value of array
     */
    public static double getHarmonic(double[] values) {
        double sum = 0.0;

        for (int j = 0; j < values.length; j++) {
            sum += 1.0 / values[j];
        }

        return sum;
    }

    /**
     * get kurtosis data of array
     * @return kurtosis of array
     */
//    public static double getKurtosis(double[] values) {
//        Kurtosis ks = new Kurtosis();
//        return ks.evaluate(values);
//    }

    public static double getTrimMean(double[] values) {
        double sum = 0.0;
        for (int j = 1; j < values.length - 1; j++) {
            sum += values[j];
        }
        return sum / (values.length - 2);
    }
}
