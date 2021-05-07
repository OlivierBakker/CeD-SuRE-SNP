library(optparse)
library(data.table)

option_list = list(
    make_option(c("-i", "--input"), type="character",
                help="Input folder containing the SuRE pipeline output."),
    make_option(c("-p", "--pattern"), type="character",
                help="Regex pattern for the files to merge.\n\t\t[default=./%default]", default="SuRE-counts_chr\\d{1,2}_BC\\.txt\\.bz2"),
    make_option(c("-o", "--output"), type="character", default="./merged_SuRE_counts.txt",
                help="Output file name.\n\t\t[default=./%default]"),
    make_option(c("-z", "--zipped"), action="store_true", default=TRUE,
                help="Are the files zipped.\n\t\t[default=%default]"),
    make_option(c("-t", "--header"), type="character", default="col1",
                help="Do the files have headers.\n\t\t[default=%default]")
)

opt_parser    <- OptionParser(option_list=option_list, description="\nMerge SuRE pipeline output counts")
opt           <- parse_args(opt_parser)

tryCatch({
    cat(paste0("[INFO]\tInput folder:\t", opt$input ,"\n"))
    cat(paste0("[INFO]\tPatten:\t", opt$pattern, "\n"))

    files     <- paste0(opt$input, "/", list.files(opt$input, pattern=opt$pattern))
    cat(paste0("[INFO]\tConsidering ", length(files), " files\n"))

    if (length(files) <= 0){
        simpleError("No files matching specified pattern")
    }
}, warning=function(w){
    cat(paste0("\n[WARN]\t", w, "\n"))
},error=function(e){
    cat(paste0("\n[ERROR]\t", e, "\n"))
    print_help(opt_parser)
    q(save="no")
})

i <- 0
for (file in files) {
    if (opt$zipped) {
        counts <- fread(paste0("zcat < ", file), data.table=F)
    } else {
        counts <- fread(file, data.table=F)
    }
    if (i == 0) {
        header        <- as.character(read.table(file, nrow=1, stringsAsFactors=F))
        merged.counts <- counts
        if (opt$header == "col1") {
            colnames(merged.counts) <- c("X",header)
        }
    } else {
        colnames(counts) <- c("X",header)
        merged.counts <- rbind(merged.counts, counts)
    }
    i <- i + 1
}
warnings()
write.table(merged.counts, file=opt$output, quote=F, row.names=F)
