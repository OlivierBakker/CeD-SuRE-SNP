package nl.umcg.suresnp.pipeline.inforecords;

public class InfoRecord {

    private String readId;
    private int barcodeErrorCount;
    private int barcodeStartInRead;
    private int barcodeEndInRead;
    private String barcode;
    private String barcodeQ;
    private String fragment;
    private String fragmentQ;
    private String adapter;
    private String adapterQ;

    public InfoRecord(String readId, int barcodeErrorCount, int barcodeStartInRead, int barcodeEndInRead, String barcode, String barcodeQ, String fragment, String fragmentQ, String adapter, String adapterQ) {
        this.readId = readId;
        this.barcodeErrorCount = barcodeErrorCount;
        this.barcodeStartInRead = barcodeStartInRead;
        this.barcodeEndInRead = barcodeEndInRead;
        this.barcode = barcode;
        this.barcodeQ = barcodeQ;
        this.fragment = fragment;
        this.fragmentQ = fragmentQ;
        this.adapter = adapter;
        this.adapterQ = adapterQ;
    }

    public String getReadId() {
        return readId;
    }

    public void setReadId(String readId) {
        this.readId = readId;
    }

    public int getBarcodeErrorCount() {
        return barcodeErrorCount;
    }

    public void setBarcodeErrorCount(int barcodeErrorCount) {
        this.barcodeErrorCount = barcodeErrorCount;
    }

    public int getBarcodeStartInRead() {
        return barcodeStartInRead;
    }

    public void setBarcodeStartInRead(int barcodeStartInRead) {
        this.barcodeStartInRead = barcodeStartInRead;
    }

    public int getBarcodeEndInRead() {
        return barcodeEndInRead;
    }

    public void setBarcodeEndInRead(int barcodeEndInRead) {
        this.barcodeEndInRead = barcodeEndInRead;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcodeQ() {
        return barcodeQ;
    }

    public void setBarcodeQ(String barcodeQ) {
        this.barcodeQ = barcodeQ;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getFragmentQ() {
        return fragmentQ;
    }

    public void setFragmentQ(String fragmentQ) {
        this.fragmentQ = fragmentQ;
    }

    public String getAdapter() {
        return adapter;
    }

    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }

    public String getAdapterQ() {
        return adapterQ;
    }

    public void setAdapterQ(String adapterQ) {
        this.adapterQ = adapterQ;
    }
}
