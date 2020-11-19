package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import nl.umcg.suresnp.pipeline.io.ExcelWriter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.bedreader.GenericGenomicAnnotationReader;
import nl.umcg.suresnp.pipeline.io.bedreader.GenomicAnnotationProvider;
import nl.umcg.suresnp.pipeline.io.summarystatisticreader.ReferenceDependentSummaryStatisticReader;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.records.summarystatistic.GeneticVariant;
import nl.umcg.suresnp.pipeline.records.summarystatistic.SummaryStatistic;
import nl.umcg.suresnp.pipeline.records.summarystatistic.SummaryStatisticRecord;
import nl.umcg.suresnp.pipeline.tools.parameters.CreateExcelParameters;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class CreateExcel {

    private static final Logger LOGGER = Logger.getLogger(CreateExcel.class);
    private final CreateExcelParameters params;


    public CreateExcel(CreateExcelParameters params) {
        this.params = params;
    }

    public void run() throws IOException, ParseException {
        createSnpExcel();
    }


    private void createSnpExcel() throws IOException, ParseException {

        VCFFileReader vcfReader = new VCFFileReader(params.getInputVcf(), true);
        Map<String, GeneticVariant> variantCache = new HashMap<>();

        // Either only include reference SNPs from
        if (params.getRegionFilterFile() != null) {
            InRegionFilter regionFilter = new InRegionFilter(params.getRegionFilterFile(), true);

            for (BedRecord curRec : regionFilter) {
                for (CloseableIterator<VariantContext> it = vcfReader.query(curRec); it.hasNext(); ) {
                    VariantContext curContext = it.next();
                    GeneticVariant curGeneticVariant = createSnpFromVariantContext(curContext);
                    variantCache.put(curGeneticVariant.getPrimaryVariantId(), curGeneticVariant);
                }
            }
        } else {
            for (VariantContext curContext : vcfReader) {
                GeneticVariant curGeneticVariant = createSnpFromVariantContext(curContext);
                variantCache.put(curGeneticVariant.getPrimaryVariantId(), curGeneticVariant);
            }
        }

        // Genomic annotations, currently unaffected by the region filter option
        List<GenericGenomicAnnotation> regionAnnotations = new ArrayList<>();
        if (params.getRegionAnnotationFiles() != null) {
            for(GenericFile file : params.getRegionAnnotationFiles()) {
                GenomicAnnotationProvider reader = new GenericGenomicAnnotationReader(file, true);
                IntervalTreeMap<List<GenericGenomicAnnotationRecord>> records = new IntervalTreeMap<>();

                for (GenericGenomicAnnotationRecord tmp : reader) {
                    if (records.containsKey(tmp)) {
                        records.get(tmp).add(tmp);
                    } else {
                        List<GenericGenomicAnnotationRecord> bla = new ArrayList(1);
                        bla.add(tmp);
                        records.put(tmp, bla);
                    }

                }

                reader.close();
                regionAnnotations.add(new GenericGenomicAnnotation(file,
                        Arrays.copyOfRange(reader.getHeader(), 3, reader.getHeader().length),
                        records));
            }
            LOGGER.info("Read genomic annotations");
        }

        // Variant annotations, currently unaffected by the region filter option
        List<SummaryStatistic> variantAnnotations = new ArrayList<>();
        if (params.getVariantAnnotationFiles() != null) {
            for(GenericFile file : params.getVariantAnnotationFiles()) {
                ReferenceDependentSummaryStatisticReader reader = new ReferenceDependentSummaryStatisticReader(file,
                        new GenericFile(params.getOutputPrefix() + "missingVariants.txt"),
                        variantCache,
                        false);
                IntervalTreeMap<SummaryStatisticRecord> records = new IntervalTreeMap<>();

                for (SummaryStatisticRecord tmp : reader) {
                    if (tmp != null) {
                        records.put(tmp, tmp);
                    }
                }

                variantAnnotations.add(new SummaryStatistic(file, records));
            }
            LOGGER.info("Read variant annotations");
        }

        ExcelWriter excelWriter = new ExcelWriter(new GenericFile(params.getOutputPrefix() + ".xlsx"));
        excelWriter.saveSnpAnnotationExcel(variantCache, regionAnnotations, variantAnnotations);


        LOGGER.info("debug");
    }


    private static GeneticVariant createSnpFromVariantContext(VariantContext curContext) {

        GeneticVariant outputGeneticVariant = new GeneticVariant(curContext.getID(),
                curContext.getReference().getBaseString(),
                curContext.getAlternateAllele(0).getBaseString(),
                curContext.getStart(),
                curContext.getContig());

        if (curContext.getID().equals(".")) {
            outputGeneticVariant.setPrimaryVariantId(outputGeneticVariant.getContig() + ":" + outputGeneticVariant.getStart() + ";" + outputGeneticVariant.getAllele1() + ";" + outputGeneticVariant.getAllele2());
        }

        return outputGeneticVariant;
    }
}
