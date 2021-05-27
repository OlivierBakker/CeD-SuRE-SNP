package nl.umcg.mafsamplesubset;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public final class MafSampleSubsetParameters {

    private static final Options OPTIONS;

    static {

        OPTIONS = new Options();
        Option option;

        option = Option.builder("g")
                .longOpt("genotype")
                .hasArg(true)
                .required()
                .type(String.class)
                .desc("Path to genotype data.")
                .argName("PATH")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("r")
                .longOpt("snplist")
                .hasArg(true)
                .required()
                .type(String.class)
                .desc("Path to snplist.")
                .argName("PATH")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("o")
                .longOpt("output-prefix")
                .hasArg(true)
                .required()
                .type(String.class)
                .desc("Prefix for output.")
                .argName("OUTFILE")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("p")
                .longOpt("permutations")
                .hasArg(true)
                .required()
                .type(String.class)
                .desc("The amount of permutation to run")
                .argName("N_PERMUTATIONS")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("s")
                .longOpt("subset-size")
                .hasArg(true)
                .required()
                .type(String.class)
                .desc("The size of the subset")
                .argName("SUBSET_SIZE")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("l")
                .longOpt("snp-priority-maf")
                .hasArg(true)
                .required()
                .type(String.class)
                .desc("The MAF to consider as a priority variant")
                .argName("MAF")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("h")
                .longOpt("sample-priority")
                .hasArg(true)
                .required()
                .type(String.class)
                .desc("The amount of minor alleles a sample must have from the prioritized snp" +
                        "list in order to be considered. Homozygous individuals count for 2 alleles")
                .argName("MAF")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("u")
                .longOpt("target-maf")
                .hasArg(true)
                .required()
                .type(String.class)
                .desc("The MAF to target. (atm just used for reporting statistics)")
                .argName("MAF")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("v")
                .longOpt("heterozygous-only")
                .hasArg(false)
                .required()
                .desc("Only use heterozygous individuals in sample prioritization")
                .build();
        OPTIONS.addOption(option);

        option = Option.builder("t")
                .longOpt("input-type")
                .hasArg(true)
                .required()
                .desc("The input data type. If not defined will attempt to automatically select the first matching dataset on the specified path\n"
                        + "* PED_MAP - plink PED MAP files.\n"
                        + "* PLINK_BED - plink BED BIM FAM files.\n"
                        + "* VCF - bgziped vcf with tabix index file\n"
                        + "* VCFFOLDER - matches all bgziped vcf files + tabix index in a folder\n"
                        + "* SHAPEIT2 - shapeit2 phased haplotypes .haps & .sample\n"
                        + "* GEN - Oxford .gen & .sample\n"
                        + "* TRITYPER - TriTyper format folder")
                .argName("INPUT_TYPE")
                .valueSeparator(' ')
                .build();
        OPTIONS.addOption(option);
    }

    public Options getOptions(){
        return OPTIONS;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(999);
        formatter.printHelp(" ", OPTIONS);
    }


}
