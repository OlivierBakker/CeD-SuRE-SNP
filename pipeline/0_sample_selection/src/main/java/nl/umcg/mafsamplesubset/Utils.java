package nl.umcg.mafsamplesubset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static List<Integer> convertFloatsToInts(float[] input) {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        //double[] output = new double[input.length];
        List<Integer> output = new ArrayList<>();
        for (int i = 0; i < input.length; i++)
        {
            if (input[i] < 0) {
                output.add(-1);
            } else if (input[i] < 0.5) {
                output.add(0);
            } else {
                output.add(Math.round(input[i]));
            }
        }
        return output;
    }

    public static int[] convertFloatsToIntArray(float[] input) {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        int[] output = new int[input.length];
        for (int i = 0; i < input.length; i++)
        {
            if (input[i] < 0) {
                output[i] = -1;
            } else if (input[i] < 0.5) {
                output[i] = 0;
            } else {
                output[i] = Math.round(input[i]);
            }
        }
        return output;
    }

    public static List<Double> convertFloatsToDoubles(float[] input) {
        if (input == null)
        {
            return null; // Or throw an exception - your choice
        }
        //double[] output = new double[input.length];
        List<Double> output = new ArrayList<>();
        for (int i = 0; i < input.length; i++)
        {
            output.add((double) input[i]);
        }
        return output;
    }


    // Get a subset of an array by index
    public static int[] getSubset(int[] input, int[] subset) {
        int[] result = new int[subset.length];
        for (int i = 0; i < subset.length; i++)
            result[i] = input[subset[i]];
        return result;
    }


    public static String floatsToString(float[] a, String sep) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(sep);
        }

    }


    // Returns all indices of value
    public static Set<Integer> findIndicesEqual(int[] data, int value) {

        Set<Integer> indices = new HashSet<>();

        for (int i=0; i < data.length; i++) {

            if (data[i] == value) {
                indices.add(i);
            }

        }
        return (indices);
    }

    // Returns all indices of value
    public static Set<Integer> findIndicesLess(double[] data, double value) {

        Set<Integer> indices = new HashSet<>();

        for (int i=0; i < data.length; i++) {

            if (data[i] < value) {
                indices.add(i);
            }

        }
        return (indices);
    }

    // Returns all indices of value
    public static Set<Integer> findIndicesGreater(double[] data, double value) {

        Set<Integer> indices = new HashSet<>();

        for (int i=0; i < data.length; i++) {

            if (data[i] > value) {
                indices.add(i);
            }

        }
        return (indices);
    }


}
