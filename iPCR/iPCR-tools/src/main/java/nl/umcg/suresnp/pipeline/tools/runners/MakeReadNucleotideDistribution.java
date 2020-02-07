package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.fastq.FastqReader;
import htsjdk.samtools.fastq.FastqRecord;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrFileReader;
import nl.umcg.suresnp.pipeline.io.ipcrreader.IpcrRecordProvider;
import nl.umcg.suresnp.pipeline.records.ipcrrecords.IpcrRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecords.filters.HasBarcodeCountGreaterEqualsFilter;
import nl.umcg.suresnp.pipeline.records.ipcrrecords.filters.IpcrRecordFilter;
import nl.umcg.suresnp.pipeline.tools.parameters.MakeReadNucleotideDistributionParameters;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.umcg.suresnp.pipeline.IpcrTools.logProgress;

public class MakeReadNucleotideDistribution {

    private static final Logger LOGGER = Logger.getLogger(MakeReadNucleotideDistribution.class);
    private MakeReadNucleotideDistributionParameters params;

    private int numberOfNucsEitherEnd;
    private int[] nucN;
    private int[] nucA;
    private int[] nucT;
    private int[] nucC;
    private int[] nucG;

    public MakeReadNucleotideDistribution(MakeReadNucleotideDistributionParameters params) {
        this.params = params;

        this.numberOfNucsEitherEnd = 5;
        this.nucN = new int[numberOfNucsEitherEnd * 2];
        this.nucA = new int[numberOfNucsEitherEnd * 2];
        this.nucT = new int[numberOfNucsEitherEnd * 2];
        this.nucC = new int[numberOfNucsEitherEnd * 2];
        this.nucG = new int[numberOfNucsEitherEnd * 2];
    }

    public void run() throws IOException {
        //List<IpcrRecord> records = provider.getRecordsAsList(filterList);
        Set<String> readNames = getReadnameSet();
        FastqReader readOneReader = new FastqReader(new File(params.getInputBam() + "_R1.fq.gz"), true);
        FastqReader readTwoReader = new FastqReader(new File(params.getInputBam() + "_R2.fq.gz"), true);

        int i=0;
        for (FastqRecord curRecord : readOneReader) {
            logProgress(i, 1000000, "MakeReadNucleotideDistribution");
            i++;

            FastqRecord curRecordMate = readTwoReader.next();

            String rOneName = curRecord.getReadName().split(" ")[0];
            String rTwoName = curRecordMate.getReadName().split(" ")[0];

            if (rOneName.equals(rTwoName)) {
                if (readNames.contains(rOneName)) {
                    countNucs(curRecord.getReadString(), true);
                    countNucs(curRecordMate.getReadString(), false);
                }
            } else {
                LOGGER.error("Mismatched fastq's");
                System.exit(-1);
            }
        }
        System.out.print("\n"); // Flush progress bar

        LOGGER.info("Output:");
        System.out.println("N" + nucsToString(nucN));
        System.out.println("A" + nucsToString(nucA));
        System.out.println("T" + nucsToString(nucT));
        System.out.println("C" + nucsToString(nucC));
        System.out.println("G" + nucsToString(nucG));

    }

    private String nucsToString(int[] nucs) {
        StringBuilder b = new StringBuilder();
        String sep = "\t";
        for (int n : nucs) {
            b.append(sep);
            b.append(n);
        }

        return b.toString();
    }

    private void countNucs(String read, boolean isReadOne) {
        int i=0;
        while (i <= numberOfNucsEitherEnd-1) {
            if (isReadOne) {
                addNuc(read.charAt(i), i);
            } else {
                addNuc(read.charAt(i), i + numberOfNucsEitherEnd);
            }
            i++;
        }
    }

    private void addNuc(char nuc, int index) {
        switch (nuc) {
            case 'N':
                nucN[index] +=1;
                break;
            case 'A':
                nucA[index] +=1;
                break;
            case 'T':
                nucT[index] +=1;
                break;
            case 'C':
                nucC[index] +=1;
                break;
            case 'G':
                nucG[index] +=1;
                break;
        }

    }

    private Set<String> getReadnameSet() throws IOException {
        IpcrRecordProvider provider = new IpcrFileReader(params.getInputIpcr(), true);
        Set<String> readNamesToKeep = new HashSet<>();

        List<IpcrRecordFilter> filters = new ArrayList<>();
        filters.add(new HasBarcodeCountGreaterEqualsFilter(params.getBarcodeCountFilter()));
        IpcrRecord curRecord = provider.getNextRecord();

        int i = 0;
        while (curRecord != null) {
            logProgress(i, 1000000, "MakeReadNucleotideDistribution");
            i++;

            for (IpcrRecordFilter filter : filters) {
                if (filter.passesFilter(curRecord)) {
                    readNamesToKeep.add(curRecord.getPrimaryReadName());
                }
            }
            curRecord = provider.getNextRecord();
        }
        System.out.print("\n"); // Flush progressbar

        LOGGER.info("Read " + i + " records");
        LOGGER.info(readNamesToKeep.size() + " records passed filters");

        return readNamesToKeep;
    }
}
