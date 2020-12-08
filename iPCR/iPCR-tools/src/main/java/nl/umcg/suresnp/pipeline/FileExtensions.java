package nl.umcg.suresnp.pipeline;

public enum FileExtensions {

    IPCR(".ipcr"),
    IPCR_INDEXED(IPCR + ".bgz"),
    IPCR_INDEX(IPCR + ".bgz.tbi"),
    IPCR_BARCODES(IPCR + ".barcodes"),
    IPCR_DISCARD(IPCR + ".discarded"),
    IPCR_ALLELE_SPECIFIC(IPCR + ".ase"),
    IPCR_ALLELE_SPECIFIC_MINIMAL(IPCR + ".ase.minimal"),
    NARROW_PEAK(".narrowPeak"),

    CDNA(".cdna"),
    CDNA_BARCODE_COUNT(CDNA + ".barcode.counts"),

    BED(".bed"),
    BEDPE(".bedpe"),
    BEDGRAPH(".bdg"),
    MACS_IPCR(IPCR.toString()),
    MACS_CDNA(CDNA.toString())
    ;
    private final String extension;



    FileExtensions(String extension) {
        this.extension = extension;
    }


    @Override
    public String toString() {
        return extension;
    }
}
