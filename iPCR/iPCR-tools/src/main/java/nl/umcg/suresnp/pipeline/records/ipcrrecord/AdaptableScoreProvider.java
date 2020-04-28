package nl.umcg.suresnp.pipeline.records.ipcrrecord;

public interface AdaptableScoreProvider {
    double getScore(IpcrRecord record);
    String[] getSamples();
    String getSamplesAsString();
}
