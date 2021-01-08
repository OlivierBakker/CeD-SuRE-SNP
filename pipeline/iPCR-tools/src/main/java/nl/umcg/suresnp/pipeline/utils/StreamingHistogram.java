package nl.umcg.suresnp.pipeline.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StreamingHistogram {

    long totalValue;
    long totalValuesAdded;
    long binSize;
    List<Long> bins;
    int scalingFactor;

    public StreamingHistogram(long binSize, int startingBins) {
        this.binSize = binSize;
        this.bins = new ArrayList<>(Collections.nCopies(startingBins, (long)0));
        this.scalingFactor = 30;
        this.totalValue = 0;
        this.totalValuesAdded = 0;
    }

    public StreamingHistogram(long binSize) {
        this.binSize = binSize;
        this.bins = new ArrayList<>();
        this.scalingFactor = 30;
        this.totalValue = 0;
        this.totalValuesAdded = 0;
    }

    public void addPostiveValue(long value) {
        this.totalValue = totalValue + value;
        this.totalValuesAdded ++;

        int idx = (int) Math.round((double)value / (double) binSize) - 1;

        if (idx < 0) {
            idx = 0;
        }

        if (idx > bins.size() - 1) {
            int diff = idx - (bins.size() - 1);
            int i = 0;
            while (i <= diff) {
                bins.add((long)0);
                i++;
            }
        }

        long newVal = bins.get(idx);
        newVal ++;
        bins.set(idx, newVal);
    }


    /**
     * Returns the mean value in the data
     * @return the mean value
     */
    public double getMean() {
        return (double) totalValue / totalValuesAdded;
    }


    /**
     * Retrieves the value of the bin that the median value is contained in.
     * If binsize is equal to one, and only positive integers are included,
     * is the same as median value.
     *
     * @return the value of the bin in which the median is contained
     */
    public double getValueOfMedianBin() {
        long medianCount = totalValuesAdded / 2;

        int medianBinIndex = 0;
        long totalBinCount = 0;
        for (long curBinCount : bins) {
            totalBinCount += curBinCount;
            if (totalBinCount < medianCount) {
                medianBinIndex ++;
                continue;
            } else {
                break;
            }
        }

        return medianBinIndex * binSize;
    }

    /**
     * Returns a string representation of the histogram in ASCII art
     * @return a string representation of the histogram
     */
    public String getHistAsString(){

        StringBuilder b = new StringBuilder();

        // Determine the max value of the hist
        long maxValue = 0;
        for (long bin : bins) {
            if (bin > maxValue) {
                maxValue = bin;
            }
        }

        double scalingValue =  Math.floor((double) maxValue / scalingFactor);

        int i = 0;
        for (long bin : bins) {
            int histMultiplier = (int) Math.floor((double) bin / scalingValue);
            b.append(i * binSize);
            b.append("\t");
            b.append(bin);
            b.append("\t\t");
            b.append("*".repeat(histMultiplier));
            b.append("\n");
            i++;
        }

        return b.toString();
    }

    /**
     * Gets the histogram as a TSV
     * @return a TSV representation of the histogram
     */
    public String getHistAsTsv() {
        StringBuilder b = new StringBuilder();

        int i=0;
        for (long bin : bins) {
            b.append(i * binSize);
            b.append("\t");
            b.append(bin);
            b.append("\n");
            i++;
        }

        return b.toString();
    }





}
