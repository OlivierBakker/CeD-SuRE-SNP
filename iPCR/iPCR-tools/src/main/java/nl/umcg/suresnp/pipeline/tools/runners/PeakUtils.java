package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.util.IntervalTreeMap;
import nl.umcg.suresnp.pipeline.FileExtensions;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.bedreader.BedRecordProvider;
import nl.umcg.suresnp.pipeline.io.bedreader.FourColBedFileReader;
import nl.umcg.suresnp.pipeline.io.bedreader.NarrowPeakReader;
import nl.umcg.suresnp.pipeline.io.bedwriter.NarrowPeakWriter;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.NarrowPeakRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.PeakUtilsParameters;
import nl.umcg.suresnp.pipeline.utils.BedUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;
import umcg.genetica.graphics.Grid;
import umcg.genetica.graphics.panels.ScatterplotPanel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class PeakUtils {

    private static final Logger LOGGER = Logger.getLogger(PeakUtils.class);
    private  PeakUtilsParameters params;

    public PeakUtils(PeakUtilsParameters params) {
        this.params = params;
    }


    public void createConsensusPeaks() throws IOException {
        NarrowPeakWriter writer;
        List<IntervalTreeMap<NarrowPeakRecord>> peaks = new ArrayList<>(params.getInputFiles().length);

        for (GenericFile curFile : params.getInputFiles()) {
            NarrowPeakReader reader = new NarrowPeakReader(curFile, params.getPattern());
            peaks.add(reader.getNarrowPeakRecordsAsTreeMap(params.getFilters()));
        }

        if (peaks.size() > 2) {
            LOGGER.error("Currently merging more than 2 peak files is not supported");
            System.exit(-1);
        } else if (peaks.size() == 2) {

            LOGGER.info("Merging peaks into consensus peaks, omitting non overlapping peaks");
            List<NarrowPeakRecord> output = new ArrayList<>();

            IntervalTreeMap<NarrowPeakRecord> indexPeaks = peaks.get(0);
            IntervalTreeMap<NarrowPeakRecord> otherPeaks = peaks.get(1);

            for (NarrowPeakRecord curRecord: indexPeaks.values()) {
                Collection<NarrowPeakRecord> overlappingRecords = otherPeaks.getOverlapping(curRecord);

                if (overlappingRecords.size() >=1) {
                    NarrowPeakRecord curConsensus = makeConsensusRecord(curRecord, overlappingRecords);
                    output.add(curConsensus);

                    // Algo is greedy, so only the first overlap is reported
                    for (NarrowPeakRecord curOverlap: overlappingRecords) {
                        otherPeaks.remove(curOverlap);
                    }
                }

            }

            LOGGER.info("Done merging, sorting consensus peaks");
            output.sort(Comparator.comparing(NarrowPeakRecord::getContig).thenComparing(NarrowPeakRecord::getStart));

            LOGGER.info("Done sorting, writing output");
            writer = new NarrowPeakWriter(new GenericFile(params.getOutputPrefix() + FileExtensions.NARROW_PEAK));
            writer.writeRecords(output);
            writer.flushAndClose();

            LOGGER.info("Done, wrote " + output.size() + " records");
        } else {
            writer = new NarrowPeakWriter(new GenericFile(params.getOutputPrefix()));
            writer.writeRecords(peaks.get(0).values());
            writer.flushAndClose();
            LOGGER.info("Done, wrote " + peaks.get(0).values().size() + " records");

        }


    }


    /**
     * Creates a consensus record out of a set of records
     * @param index
     * @param others
     * @return
     */
    private static NarrowPeakRecord makeConsensusRecord(NarrowPeakRecord index, Collection<NarrowPeakRecord> others) {

        int start = index.getStart();
        int end = index.getEnd();
        StringBuilder name = new StringBuilder(index.getName());

        double signalValue = index.getSignalValue();

        for (NarrowPeakRecord curRec : others) {
            if (curRec.getStart() < start) {
                start = curRec.getStart();
            }

            if (curRec.getEnd() > end) {
                end = curRec.getEnd();
            }

            name.append("|");
            name.append(curRec.getName());

            signalValue += curRec.getSignalValue();
        }

        signalValue = signalValue / (others.size() +1);

        return new NarrowPeakRecord(index.getContig(), start, end, name.toString(),0, '.', signalValue, -1, -1, -1);
    }



    /**
     * Correlate two score profiles (from .narrowPeak or 4 col bed files)
     * TODO: Cleanup and better plotting
     *
     * @throws Exception the exception
     */
    public void getPeakCorrelations() throws Exception {
        LOGGER.info("Correlating peaks");

        // Reading the input files, can be .bed or .narrowPeak
        List<BedRecord>[] inputFiles = new List[params.getInputFiles().length];
        int i = 0;
        for (GenericFile curFile : params.getInputFiles()) {
            BedRecordProvider reader;

            LOGGER.info("Reading file: " + curFile.getFileName());
            if (curFile.getFileName().contains(".bed")) {
                reader = new FourColBedFileReader(curFile);
            } else {
                reader = new NarrowPeakReader(curFile);
            }

            inputFiles[i] = reader.getBedRecordAsList();
            reader.close();
            i++;
        }
        LOGGER.info("Done reading");

        // Intersect overlapping bed records. Remove others
        List<BedRecord>[] output = BedUtils.intersectSortedBedRecords(inputFiles[0], inputFiles[1]);
        LOGGER.info("Output " + output.length);

        // Calculate correlations and make plot
        i = 0;
        double[] x = new double[output[0].size()];
        double[] y = new double[output[1].size()];

        BufferedWriter writer = new GenericFile(params.getOutputPrefix() + ".xy.tsv").getAsBufferedWriter();
        writer.write("x\ty");
        while (i < output[0].size()) {
            // X records
            BedRecord cur = output[0].get(i);
            writer.write(Double.toString(cur.getScore()) + "\t");
            x[i] = Math.log(cur.getScore()) / Math.log(10);

            // Y records
            cur = output[1].get(i);
            writer.write(Double.toString(cur.getScore()));
            y[i] = Math.log(cur.getScore()) / Math.log(10);

            writer.newLine();
            i++;
        }
        writer.flush();
        writer.close();

        LOGGER.info("Calculating pearson R");
        PearsonsCorrelation test = new PearsonsCorrelation();
        LOGGER.info("Pearson R: " + test.correlation(x, y));

        // Make the plot
        Grid grid = new Grid(500, 500, 1, 1, 100, 100);
        ScatterplotPanel p = new ScatterplotPanel(1, 1);
        p.setData(x, y);
        //p.setDataRange(new Range(0, 0, 500, 500));
        p.setAlpha((float) 0.5);
        p.setLabels("X", "Y");
        p.setPlotElems(true, false);
        grid.addPanel(p);
        grid.draw(params.getOutputPrefix() + ".png");
    }
}
