## ------------------------------------------------------------------------
library(data.table)
library(splitstackshape)

## ------------------------------------------------------------------------
# Joris data
#base <- "/Users/olivier/Documents/tmp/wd_sure/pipeline_output/cDNA/"
#files <- c("SuRE51_B1_T1/SuRE51_B1_T1_trimmed_table.txt.gz",
#           "SuRE51_B1_T1_totalRNA/SuRE51_B1_T1_totalRNA_trimmed_table.txt.gz",
#           "SuRE51_B1_T2/SuRE51_B1_T2_trimmed_table.txt.gz",
#           "SuRE51_B2_T1/SuRE51_B2_T1_trimmed_table.txt.gz",
#           "SuRE51_B2_T1_totalRNA/SuRE51_B2_T1_totalRNA_trimmed_table.txt.gz",
#           "SuRE51_B2_T2/SuRE51_B2_T2_trimmed_table.txt.gz")

base <- "/home/work/Documents/data/sure/pipeline_output_pilot/cDNA_output/"
files <- c("J_Stim_B1_T1/J_Stim_B1_T1_trimmed_table.txt.gz",
           "J_Stim_B1_T2/J_Stim_B1_T2_trimmed_table.txt.gz",
           "J_Stim_B2_T1/J_Stim_B2_T1_trimmed_table.txt.gz",
           "J_Stim_B2_T2/J_Stim_B2_T2_trimmed_table.txt.gz",
           "Jurkat_B1_T1/Jurkat_B1_T1_trimmed_table.txt.gz",
           "Jurkat_B1_T2/Jurkat_B1_T2_trimmed_table.txt.gz",
           "Jurkat_B2_T1/Jurkat_B2_T1_trimmed_table.txt.gz",
           "Jurkat_B2_T2/Jurkat_B2_T2_trimmed_table.txt.gz",
           "K562_B2_T1/K562_B2_T1_trimmed_table.txt.gz",
           "K562_B2_T2/K562_B2_T2_trimmed_table.txt.gz",
           "K562_B3_T1/K562_B3_T1_trimmed_table.txt.gz",
           "K562_B3_T2/K562_B3_T2_trimmed_table.txt.gz")

i <- 0
for (file in files) {

  cn <- c(gsub("\\_trimmed\\_table\\.txt\\.gz", "", basename(file)), "barcode")
  if (i == 0) {
    merged.counts <- fread(paste0("zcat < " ,base, file))
    colnames(merged.counts) <- cn
  } else {
    tmp           <- fread(paste0("zcat < ", base, file))
    colnames(tmp) <- cn
    merged.counts <- merge(merged.counts, tmp, by="barcode", all=T)
  }
  i             <- i + 1
}

cDNA <- data.frame(merged.counts, row.names=1)
rm(tmp, cn, file, files, i, merged.counts)

cDNA <- cDNA[rowSums(is.na(cDNA)) <= 2,]
cDNA <- cDNA[rowMeans(cDNA, na.rm=T) > 5,]

## ------------------------------------------------------------------------
base  <- "/home/work/Documents/data/sure/pipeline_output_pilot/iPCR_output/"
files <- c("bed-annot/iPCR-combined-bedpe_chr10_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr11_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr14_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr15_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr16_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr1_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr21_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr22_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr2_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr3_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr4_SNPannot.txt.gz",
           "bed-annot/iPCR-combined-bedpe_chr7_SNPannot.txt.gz")

i <- 0
for (file in files) {
  if (i == 0) {
    iPCR <- fread(paste0("zcat < " ,base, file))
  } else {
    tmp  <- fread(paste0("zcat < ", base, file))
    iPCR <- rbind(iPCR, tmp)
  }
  i  <- i + 1
}

iPCR <- data.frame(iPCR)
colnames(iPCR) <- c("chr", "start", "end", "length", "strand", "barcode", "count", "int.end", "int.start", "MAPQ", "MD1", "MD2", "alt.1", "alt.2", "seq.1", "seq.2", "x.1", "x.2", "SNP.relpos", "SNP.base", "SNP.abspos", "SNP.var","SNP.idx")
iPCR$chr <- gsub("chr", "", iPCR$chr)
rm(tmp, file, files, i)

## ------------------------------------------------------------------------
# Remove all associations with a single read
iPCR.filtered <- iPCR[-which(iPCR$count == 1),]

# Barplot of libsize
pdf(width=8, height=4, file="output/plots/iPCR_counts.pdf")

par(mfrow=c(1,2))
barplot(table(iPCR$count), xlab="iPCR read count", ylab="Count", main="Before QC", font=8, family="ArialMT")
barplot(table(iPCR.filtered$count), xlab="iPCR read count", ylab="Count", main="After QC")

dev.off()

# Remove BC to fragment associations not overlapping with a SNP
iPCR.filtered <- iPCR.filtered[-which(iPCR.filtered$SNP.idx == ''),]

iPCR.filtered <- cSplit(iPCR.filtered[,c("barcode","chr", "start", "end", "strand","SNP.abspos","count", "MD1", "MD2", "SNP.base")], c("SNP.abspos", "SNP.base"), sep = ",", direction = "long", type.convert = F)
iPCR.filtered <- as.data.frame(iPCR.filtered)

# Remove duplications
iPCR.filtered <- iPCR.filtered[!duplicated(iPCR.filtered),]

# Trim whitespaces (they should be there in the first place)
iPCR.filtered$SNP.abspos <- trimws(iPCR.filtered$SNP.abspos)

# Assign snp ids
iPCR.filtered$SNP.id <- paste(iPCR.filtered$chr, iPCR.filtered$SNP.abspos, sep=":")
iPCR.filtered <- iPCR.filtered[, c("SNP.id", colnames(iPCR.filtered)[-11])]

# Remove ambiguous SNPs
iPCR.filtered <- iPCR.filtered[iPCR.filtered$SNP.base %in% c("A", "T", "C", "G"),]

