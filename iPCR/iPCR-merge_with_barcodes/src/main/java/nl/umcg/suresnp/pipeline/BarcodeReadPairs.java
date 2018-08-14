package nl.umcg.suresnp.pipeline;

import java.util.ArrayList;
import java.util.List;

public class BarcodeReadPairs {


    private String barcode;
    private List<Barcode> barcodes;

    public BarcodeReadPairs() {
        this.barcode = barcode;
        this.barcodes = new ArrayList<>();
    }

    public BarcodeReadPairs(Barcode barcode) {
        this.barcode = barcode.getBarcode();
        this.barcodes = new ArrayList<>();
        barcodes.add(barcode);
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getDuplicateCount() {
        return barcodes.size();
    }

    public void addBarcode(Barcode record) {
        barcodes.add(record);
    }

    public List<Barcode> getDuplicates() {
        return barcodes;
    }
}
