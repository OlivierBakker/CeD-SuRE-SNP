package nl.umcg.suresnp.pipeline.records.ipcrrecord;

import org.apache.log4j.Logger;

public class SampleSumScoreProvider implements AdaptableScoreProvider {
    private static final Logger LOGGER = Logger.getLogger(SampleSumScoreProvider.class);
    private final String[] samplesToSum;
    private final String samplesAsString;

    public SampleSumScoreProvider(String[] samplesToSum) {
        if (samplesToSum == null) {
            throw new IllegalArgumentException("Did not provide sample names");
        }
        LOGGER.info("Summing scores over the following samples:");
        for(String sample: samplesToSum) {
            LOGGER.info(sample);
        }
        this.samplesToSum = samplesToSum;

        StringBuilder b = new StringBuilder();
        int i=0;
        for (String s: samplesToSum) {
            if (i>0) {
                b.append("+");
            }
            b.append(s);
            i++;
        }
        this.samplesAsString = b.toString();
    }

    @Override
    public double getScore(IpcrRecord record) {

        if (samplesToSum[0].equals("IPCR")) {
            return record.getIpcrDuplicateCount();
        } else {
            double outputSum = 0;
            for (String sample : samplesToSum) {
                outputSum += record.getBarcodeCountPerSample().get(sample);
            }
            return outputSum;
        }

    }

    @Override
    public String[] getSamples() {
        return samplesToSum;
    }

    @Override
    public String getSamplesAsString() {
        return samplesAsString;
    }
}
