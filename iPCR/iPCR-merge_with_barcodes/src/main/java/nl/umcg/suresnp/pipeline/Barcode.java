package nl.umcg.suresnp.pipeline;

public class Barcode {

    private String readId;
    private String barcode;
    private int barcodeLength;

    public Barcode(String readId, String barcode, int barcodeLength) {
        this.readId = readId;
        this.barcode = barcode;
        this.barcodeLength = barcodeLength;
    }

    public String getReadId() {
        return readId;
    }

    public void setReadId(String readId) {
        this.readId = readId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getBarcodeLength() {
        return barcodeLength;
    }

    public void setBarcodeLength(int barcodeLength) {
        this.barcodeLength = barcodeLength;
    }
}
