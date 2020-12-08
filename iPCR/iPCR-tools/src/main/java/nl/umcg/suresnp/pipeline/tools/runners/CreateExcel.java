package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.Locatable;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import nl.umcg.suresnp.pipeline.IpcrTools;
import nl.umcg.suresnp.pipeline.io.ExcelWriter;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.bedreader.BigBedGenomicAnnotationReader;
import nl.umcg.suresnp.pipeline.io.bedreader.BlockCompressedGenomicAnnotationReader;
import nl.umcg.suresnp.pipeline.io.bedreader.GenericGenomicAnnotationReader;
import nl.umcg.suresnp.pipeline.io.bedreader.GenomicAnnotationProvider;
import nl.umcg.suresnp.pipeline.io.summarystatisticreader.ReferenceDependentSummaryStatisticReader;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.records.summarystatistic.*;
import nl.umcg.suresnp.pipeline.tools.parameters.CreateExcelParameters;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

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

        // List of variants
        List<Locatable> variantsAsLocatable = new ArrayList<>(variantCache.values());

        // Genomic annotations, currently unaffected by the region filter option
        Map<String, List<GenericGenomicAnnotation>> regionAnnotations = new HashMap<>();
        if (params.getRegionAnnotationFiles() != null) {
            for (GenericGenomicAnnotation curAnnotation : params.getRegionAnnotationFiles()) {

                GenericFile file = curAnnotation.getPath();
                IntervalTreeMap<Set<GenericGenomicAnnotationRecord>> records = new IntervalTreeMap<>();
                GenomicAnnotationProvider reader;

                if (file.isTabixIndexed()) {
                    reader = new BlockCompressedGenomicAnnotationReader(file, variantsAsLocatable, true);
                } else if (file.isBigBed()) {
                    // TODO: hardcoded that bigBed file should have chr as a contig prefix
                    LOGGER.warn("Hardcoded that bigBed file should have chr as a contig prefix");
                    reader = new BigBedGenomicAnnotationReader(file, variantsAsLocatable, true);
                } else {
                    reader = new GenericGenomicAnnotationReader(file, true);
                }

                LOGGER.debug("Instantiated reader for file: " + file.getPath());
                int i = 0;
                for (GenericGenomicAnnotationRecord curRec : reader) {
                    if (curRec != null) {
                        if (records.containsKey(curRec)) {
                            records.get(curRec).add(curRec);
                        } else {
                            Set<GenericGenomicAnnotationRecord> bla = new HashSet<>(1);
                            bla.add(curRec);
                            records.put(curRec, bla);
                        }
                    }
                    IpcrTools.logProgress(i, 1000, reader.getClass().getSimpleName(), "thousand");
                    i++;
                }
                System.out.print("\n"); // Flush progress bar
                LOGGER.info("Read " + records.size() + " unique, non null locations");

                String[] header = Arrays.copyOfRange(reader.getHeader(), 3, reader.getHeader().length);
                reader.close();

                // Set header and record information
                curAnnotation.setHeader(header);
                curAnnotation.setRecords(records);

                List<GenericGenomicAnnotation> annotations;
                if (regionAnnotations.containsKey(curAnnotation.getGroup())) {
                    annotations = regionAnnotations.get(curAnnotation.getGroup());
                    annotations.add(curAnnotation);
                } else {
                    annotations = new ArrayList<>(1);
                    annotations.add(curAnnotation);
                    regionAnnotations.put(curAnnotation.getGroup(), annotations);
                }
            }
            LOGGER.info("Read genomic annotations");
        }

        // Variant annotations, currently unaffected by the region filter option
        Map<String, List<VariantBasedNumericGenomicAnnotation>> variantAnnotations = new HashMap<>();
        if (params.getVariantAnnotationFiles() != null) {
            for (VariantBasedNumericGenomicAnnotation curAnnotation: params.getVariantAnnotationFiles()) {
                GenericFile file = curAnnotation.getPath();

                ReferenceDependentSummaryStatisticReader reader = new ReferenceDependentSummaryStatisticReader(file,
                        new GenericFile(params.getOutputPrefix() + "missingVariants.txt"),
                        variantCache,
                        true);

                Map<String, VariantBasedNumericGenomicAnnotationRecord> records = new HashMap<>();
                String[] header = Arrays.copyOfRange(reader.getHeader(), 1, reader.getHeader().length);

                for (int i = 0; i < header.length; i++) {
                    header[i] = file.getBaseName() + "_" + header[i];
                }

                for (VariantBasedNumericGenomicAnnotationRecord tmp : reader) {
                    if (tmp != null) {
                        records.put(tmp.getPrimaryVariantId(), tmp);
                    }
                }

                curAnnotation.setHeader(header);
                curAnnotation.setRecords(records);

                if (variantAnnotations.containsKey(curAnnotation.getGroup())) {
                    variantAnnotations.get(curAnnotation.getGroup()).add(curAnnotation);
                } else {
                    List<VariantBasedNumericGenomicAnnotation> annotations = new ArrayList<>();
                    annotations.add(curAnnotation);
                    variantAnnotations.put(curAnnotation.getGroup(), annotations);
                }



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
