package nl.umcg.suresnp.pipeline.records.summarystatistic;

public class LdProxy implements Comparable<LdProxy> {

    private String variantId;
    private double r2;

    public LdProxy(String variantId, double r2) {
        this.variantId = variantId;
        this.r2 = r2;
    }

    @Override
    public int compareTo(LdProxy o) {
        return Double.compare(this.r2, o.getR2());
    }

    public String getVariantId() {
        return variantId;
    }

    public double getR2() {
        return r2;
    }

    @Override
    public String toString(){
        return variantId + ":" + String.format("%.2f",r2);
    }
}
