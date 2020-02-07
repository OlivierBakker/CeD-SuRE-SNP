package nl.umcg.suresnp.pipeline.records.inforecord.consumers;

import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

public class BarcodeConsumer implements IntConsumer {

    private Set<String> uniqueBarcodes;
    private List<String> barcodePool;

    public BarcodeConsumer(Set<String> uniqueBarcodes, List<String> barcodePool) {
        this.uniqueBarcodes = uniqueBarcodes;
        this.barcodePool = barcodePool;
    }

    @Override
    public void accept(int value) {
        uniqueBarcodes.add(barcodePool.get(value));
    }


    public int getSize() {
        return uniqueBarcodes.size();
    }
}
