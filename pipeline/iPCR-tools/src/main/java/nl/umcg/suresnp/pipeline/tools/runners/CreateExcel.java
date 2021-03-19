package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.samtools.util.Locatable;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import nl.umcg.suresnp.pipeline.IpcrTools;
import nl.umcg.suresnp.pipeline.io.AnnotatedGeneReader;
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
import nl.umcg.suresnp.pipeline.records.ensemblrecord.AnnotatedGene;
import nl.umcg.suresnp.pipeline.records.ensemblrecord.GeneBasedGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.records.summarystatistic.*;
import nl.umcg.suresnp.pipeline.tools.parameters.CreateExcelParameters;
import org.apache.log4j.Logger;
import org.molgenis.genotype.RandomAccessGenotypeData;
import org.molgenis.genotype.RandomAccessGenotypeDataReaderFormats;
import org.molgenis.genotype.annotation.Annotation;
import org.molgenis.genotype.util.LdCalculatorException;
import org.molgenis.genotype.variant.GeneticVariant;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class CreateExcel {

    private static final Logger LOGGER = Logger.getLogger(CreateExcel.class);
    private final CreateExcelParameters params;


    public CreateExcel(CreateExcelParameters params) {
        this.params = params;
    }

    public void run() throws IOException, ParseException, LdCalculatorException {
        createSnpExcel();
    }


    private void createSnpExcel() throws IOException, ParseException, LdCalculatorException {

        VCFFileReader vcfReader = new VCFFileReader(params.getInputVcf(), true);
        Map<String, GeneticVariantInterval> variantCache = new HashMap<>();

        // Either only include reference SNPs from
        if (params.getRegionFilterFile() != null) {
            InRegionFilter regionFilter = new InRegionFilter(params.getRegionFilterFile(), true);

            for (BedRecord curRec : regionFilter) {
                for (CloseableIterator<VariantContext> it = vcfReader.query(curRec); it.hasNext(); ) {
                    VariantContext curContext = it.next();
                    GeneticVariantInterval curGeneticVariantInterval = createSnpFromVariantContext(curContext);
                    variantCache.put(curGeneticVariantInterval.getPrimaryVariantId(), curGeneticVariantInterval);
                }
            }
        } else {
            for (VariantContext curContext : vcfReader) {
                GeneticVariantInterval curGeneticVariantInterval = createSnpFromVariantContext(curContext);
                variantCache.put(curGeneticVariantInterval.getPrimaryVariantId(), curGeneticVariantInterval);
            }
        }

        // LD proxy lookup
        if (params.getLdReference() != null) {
            annotateGeneticVariantsWithLdProxies(variantCache);
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
                    // TODO: hardcoded that bigBed file should have chr as a contig prefix, needs to be made an option
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
                System.out.print(""); // Flush progress bar
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
            for (VariantBasedNumericGenomicAnnotation curAnnotation : params.getVariantAnnotationFiles()) {
                GenericFile file = curAnnotation.getPath();

                ReferenceDependentSummaryStatisticReader reader = new ReferenceDependentSummaryStatisticReader(file,
                        new GenericFile(params.getOutputPrefix() + ".missingVariants.txt"),
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
        

        // Gene annotations
        GeneBasedGenomicAnnotation geneAnnotations = null;
        if (params.getEnsemblGenesFile() != null) {
            AnnotatedGeneReader reader = new AnnotatedGeneReader(params.getEnsemblGenesFile(), true);
            geneAnnotations = reader.readGenesAsGenomicAnnotation();
        }

        ExcelWriter excelWriter = new ExcelWriter(new GenericFile(params.getOutputPrefix() + ".xlsx"));
        excelWriter.saveSnpAnnotationExcel(variantCache, regionAnnotations, variantAnnotations, geneAnnotations);


        LOGGER.info("debug");
    }

    private static GeneticVariantInterval createSnpFromVariantContext(VariantContext curContext) {

        GeneticVariantInterval outputGeneticVariantInterval = new GeneticVariantInterval(curContext.getID(),
                curContext.getReference().getBaseString(),
                curContext.getAlternateAllele(0).getBaseString(),
                curContext.getStart(),
                curContext.getContig());

        if (curContext.getID().equals(".")) {
            outputGeneticVariantInterval.setPrimaryVariantId(outputGeneticVariantInterval.getContig() + ":" + outputGeneticVariantInterval.getStart() + ";" + outputGeneticVariantInterval.getAllele1() + ";" + outputGeneticVariantInterval.getAllele2());
        }

        return outputGeneticVariantInterval;
    }

    private void annotateGeneticVariantsWithLdProxies(Map<String, GeneticVariantInterval> variants) throws IOException, LdCalculatorException {

        double ldThresh = params.getLdThreshold();
        int window = params.getLdWindow();
        File ldReferenceFile = params.getLdReference();

        RandomAccessGenotypeDataReaderFormats format = RandomAccessGenotypeDataReaderFormats.matchFormatToPath(ldReferenceFile.getAbsolutePath());
        RandomAccessGenotypeData ldReference = format.createGenotypeData(ldReferenceFile.getAbsolutePath());
        Map<String, GeneticVariant> ldReferenceMap = ldReference.getVariantIdMap();

        LOGGER.info(variants.size() + " query variants given as input");

        Set<String> availableQueryVariants = variants.keySet();
        availableQueryVariants.retainAll(ldReferenceMap.keySet());
        int proxyCount = 0;

        LOGGER.info("Overlapped " + availableQueryVariants.size() + " query variants available in reference genotypes");

        int i = 0;
        for (String queryVariantId: availableQueryVariants) {
            GeneticVariant queryVariant = ldReferenceMap.get(queryVariantId);
            GeneticVariantInterval queryVariantInterval = variants.get(queryVariantId);

            Iterable<GeneticVariant> windowVariants = ldReference.getVariantsByRange(queryVariant.getSequenceName(),
                    queryVariant.getStartPos() - window,
                    queryVariant.getStartPos() + window);

            for (GeneticVariant overlappingVariant : windowVariants) {
                double r2 = overlappingVariant.calculateLd(queryVariant).getR2();
                if (r2 >= ldThresh) {
                    queryVariantInterval.addLdProxy(overlappingVariant.getPrimaryVariantId(), r2);
                    proxyCount++;
                }
            }

            i++;
            IpcrTools.logProgress(i, 1000, "CreateExcel", "thousand query");
        }

        LOGGER.info("Done, found " + proxyCount + " LD proxies for " + availableQueryVariants.size() + " query variants");
    }
}
