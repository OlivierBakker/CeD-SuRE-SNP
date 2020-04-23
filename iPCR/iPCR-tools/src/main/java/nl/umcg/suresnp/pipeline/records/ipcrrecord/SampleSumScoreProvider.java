package nl.umcg.suresnp.pipeline.records.ipcrrecord;

public class SampleSumScoreProvider implements AdaptableScoreProvider {

    private String[] samplesToSum;

    public SampleSumScoreProvider(String[] samplesToSum) {
        this.samplesToSum = samplesToSum;
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
}
