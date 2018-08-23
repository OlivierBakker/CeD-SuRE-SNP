package nl.umcg.suresnp.pipeline;

import htsjdk.samtools.SAMRecord;

public class IpcrRecord implements Comparable {

    private String barcode;
    private String referenceSequence;
    private int startOne;
    private int endOne;
    private int startTwo;
    private int endTwo;
    private char orientation;
    private int mapqOne;
    private int mapqTwo;
    private String cigarOne;
    private String cigarTwo;
    private String sequenceOne;
    private String sequenceTwo;
    private int duplicateCount;


    public IpcrRecord(String barcode, SAMRecord cachedSamRecord, SAMRecord mate) {
        SAMRecord first;
        SAMRecord second;
        char strand;

        if (cachedSamRecord.getReadNegativeStrandFlag()) {
            first = mate;
            second = cachedSamRecord;
            strand = '-';
        } else {
            first = cachedSamRecord;
            second = mate;
            strand = '+';
        }

        this.barcode = barcode;
        this.referenceSequence = first.getReferenceName();
        this.startOne = first.getAlignmentStart();
        this.endOne = first.getAlignmentEnd();
        this.startTwo = second.getAlignmentStart();
        this.endTwo = second.getAlignmentEnd();
        this.orientation = strand;
        this.mapqOne = first.getMappingQuality();
        this.mapqTwo = second.getMappingQuality();
        this.cigarOne = first.getCigarString();
        this.cigarTwo = second.getCigarString();
        this.sequenceOne = first.getReadString();
        this.sequenceTwo = second.getReadString();
    }

    public IpcrRecord(String barcode, String referenceSequence, int startOne, int endOne, int startTwo, int endTwo, char orientation, int mapqOne, int mapqTwo, String cigarOne, String cigarTwo, String sequenceOne, String sequenceTwo, int duplicateCount) {
        this.barcode = barcode;
        this.referenceSequence = referenceSequence;
        this.startOne = startOne;
        this.endOne = endOne;
        this.startTwo = startTwo;
        this.endTwo = endTwo;
        this.orientation = orientation;
        this.mapqOne = mapqOne;
        this.mapqTwo = mapqTwo;
        this.cigarOne = cigarOne;
        this.cigarTwo = cigarTwo;
        this.sequenceOne = sequenceOne;
        this.sequenceTwo = sequenceTwo;
        this.duplicateCount = duplicateCount;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getReferenceSequence() {
        return referenceSequence;
    }

    public void setReferenceSequence(String referenceSequence) {
        this.referenceSequence = referenceSequence;
    }

    public int getStartOne() {
        return startOne;
    }

    public void setStartOne(int startOne) {
        this.startOne = startOne;
    }

    public int getEndOne() {
        return endOne;
    }

    public void setEndOne(int endOne) {
        this.endOne = endOne;
    }

    public int getStartTwo() {
        return startTwo;
    }

    public void setStartTwo(int startTwo) {
        this.startTwo = startTwo;
    }

    public int getEndTwo() {
        return endTwo;
    }

    public void setEndTwo(int endTwo) {
        this.endTwo = endTwo;
    }

    public char getOrientation() {
        return orientation;
    }

    public void setOrientation(char orientation) {
        this.orientation = orientation;
    }

    public int getMapqOne() {
        return mapqOne;
    }

    public void setMapqOne(int mapqOne) {
        this.mapqOne = mapqOne;
    }

    public int getMapqTwo() {
        return mapqTwo;
    }

    public void setMapqTwo(int mapqTwo) {
        this.mapqTwo = mapqTwo;
    }

    public String getCigarOne() {
        return cigarOne;
    }

    public void setCigarOne(String cigarOne) {
        this.cigarOne = cigarOne;
    }

    public String getCigarTwo() {
        return cigarTwo;
    }

    public void setCigarTwo(String cigarTwo) {
        this.cigarTwo = cigarTwo;
    }

    public String getSequenceOne() {
        return sequenceOne;
    }

    public void setSequenceOne(String sequenceOne) {
        this.sequenceOne = sequenceOne;
    }

    public String getSequenceTwo() {
        return sequenceTwo;
    }

    public void setSequenceTwo(String sequenceTwo) {
        this.sequenceTwo = sequenceTwo;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public String getOutputString(String sep){

        StringBuilder sb = new StringBuilder();
        sb.append(barcode)
                .append(sep)
                .append(referenceSequence)
                .append(sep)
                .append(startOne)
                .append(sep)
                .append(endOne)
                .append(sep)
                .append(startTwo)
                .append(sep)
                .append(endTwo)
                .append(sep)
                .append(orientation)
                .append(sep)
                .append(mapqOne)
                .append(sep)
                .append(mapqTwo)
                .append(sep)
                .append(cigarOne)
                .append(sep)
                .append(cigarTwo)
                .append(sep)
                .append(sequenceOne)
                .append(sep)
                .append(sequenceTwo);

        if (duplicateCount != 0) {
            sb.append(sep).append(duplicateCount);
        }

        return sb.toString();

    }


    public String getSimpleOutputString(String sep) {
        StringBuilder sb = new StringBuilder();
        sb.append(barcode)
                .append(sep)
                .append(referenceSequence)
                .append(sep)
                .append(startOne)
                .append(sep)
                .append(endOne)
                .append(sep)
                .append(startTwo)
                .append(sep)
                .append(endTwo)
                .append(sep)
                .append(orientation);

        if (duplicateCount != 0) {
            sb.append(sep).append(duplicateCount);
        }

        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        IpcrRecord other = (IpcrRecord) o;
        return (barcode.compareTo(other.getBarcode()));
    }
}
