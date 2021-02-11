package nl.umcg.suresnp.pipeline.io;

import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;

public class ReferenceBedFileType {
    private int columnToSplit;
    private final BedFileType bedFileType;

    public ReferenceBedFileType(int columnToSplit, BedFileType fileType) {
        this.columnToSplit = columnToSplit;
        this.bedFileType=fileType;
    }

    public ReferenceBedFileType(BedFileType bedFileType) {
        this.bedFileType = bedFileType;
    }

    public int getColumnToSplit() {
        if (bedFileType == BedFileType.THREE_COL) {
            return -1;
        }
        return columnToSplit;
    }

    public BedFileType getBedFileType() {
        return bedFileType;
    }

    public enum BedFileType {
        THREE_COL,
        FOUR_COL;
    }
}
