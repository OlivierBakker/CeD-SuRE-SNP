package nl.umcg.suresnp.pipeline.tools.runners;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import nl.umcg.suresnp.pipeline.io.GenericFile;
import nl.umcg.suresnp.pipeline.io.bedreader.GenericGenomicAnnotationReader;
import nl.umcg.suresnp.pipeline.io.bedreader.GenomicAnnotationProvider;
import nl.umcg.suresnp.pipeline.records.bedrecord.BedRecord;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotation;
import nl.umcg.suresnp.pipeline.records.bedrecord.GenericGenomicAnnotationRecord;
import nl.umcg.suresnp.pipeline.records.ipcrrecord.filters.InRegionFilter;
import nl.umcg.suresnp.pipeline.records.summarystatistic.GeneticVariant;
import nl.umcg.suresnp.pipeline.tools.parameters.CreateExcelParameters;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            InRegionFilter regionFilter = new InRegionFilter(params.getRegionFilterFile());

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
                GenomicAnnotationProvider reader = new GenericGenomicAnnotationReader(file, false);
                IntervalTreeMap<GenericGenomicAnnotationRecord> records = new IntervalTreeMap<>();

                for (GenericGenomicAnnotationRecord tmp : reader) {
                    records.put(tmp, tmp);
                }

                regionAnnotations.add(new GenericGenomicAnnotation(file, reader.getHeader(), records));
            }
        }





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
