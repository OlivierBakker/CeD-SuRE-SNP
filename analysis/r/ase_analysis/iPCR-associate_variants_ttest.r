library(data.table)
library(MASS)
library(parallel)

#ncore <- 22
ncore <- 16

x    <- fread("zcat SuRE_CeD_SNP.minimal.allele.specific.ipcr.gz", header=F, sep="\t")
snps <- as.character(fread("unique_snps_full.txt", data.table=F, sep="\t", header=F)[,1])


# Run in parallel
out <- mclapply(snps, function(snp) {

#tryCatch({
  # Select the current SNP
  cur.x                            <- as.data.frame(x[ V2 == snp ])
  
  # K562 total count
  #cur.x$total                      <- cur.x$V16 + cur.x$V17 + cur.x$V18 + cur.x$V19

  # Caco total count
  #cur.x$total                      <- cur.x$V8 + cur.x$V9 + cur.x$V10 + cur.x$V11
  
  # Caco stim total count
  cur.x$total                      <- cur.x$V12 + cur.x$V13 + cur.x$V14 + cur.x$V15

  # Remove zeroes, extreme outliers and NA's
  cur.x[cur.x$total == 0 | cur.x$total > 500, "total"] <- NA
  cur.x                            <- cur.x[!is.na(cur.x$total),]
  
  if (length(table(cur.x$V6)) == 2) {
    if (min(table(cur.x$V6)) > 12) {
      
        # Covariate model
        cur.x$V6                        <- as.factor(cur.x$V6)
	cur.x$norm                      <- log2(cur.x$total / cur.x$V7)
        nb.reg                          <- lm(norm ~ V5 + V6, data=cur.x)

        # Determine outliers using cooks distance
        #cooks.dist                      <- cooks.distance(nb.reg)
        #cutoff                          <- qf(0.5, df1=3, df2=length(cooks.dist)-3)
        #cutoff <- 1

        #if (sum(cooks.dist > cutoff) > 0) {
        #  cat("[INFO] Detected", sum(cooks.dist > cutoff) ," outliers for,", snp, " refitting model\n")
        #  # Refitting using trimmed mean
        #  tmean                             <- mean(cur.x$total[cooks.dist < cutoff])
        #  cur.x[cooks.dist > cutoff,]$total <- round(tmean)
        #  nb.reg                            <- glm.nb(total ~ V7 + V6, data=cur.x)
        #}

        #if (sum(cooks.dist > cutoff) > 0.25*nrow(x)) {
        #   out <- c(snp, unlist(cur.x[1,3:5]), unlist(names(table(cur.x$V6))), table(cur.x$V6), 0,0,0,1)
        #} else {
           out <- c(snp, unlist(cur.x[1,3:5]), unlist(names(table(cur.x$V6))), table(cur.x$V6), summary(nb.reg)$coefficients[3,])
        #} 
    } else {
      out <- c(snp, unlist(cur.x[1,3:5]), unlist(names(table(cur.x$V6))), table(cur.x$V6), 0,0,0,1)
    }
  } else {
    out <- c(snp, unlist(cur.x[1,3:5]), "NA", "NA", "NA", "NA", 0,0,0,1)
  }

  names(out) <- c("SNP","CHR", "BP", "BP_IN_READ", "A1", "A2", "COUNT_A1", "COUNT_A2", "BETA", "SE", "Z", "P")
  return(out)
 
#}, error=function(e) {
#  cat("[ERROR] Error during fitting of model for ", snp, " skipping\n")
#  #out <- c(snp, unlist(cur.x[1,3:5]), unlist(names(table(cur.x$V6))), table(cur.x$V6), summary(nb.reg)$coefficients[3,])
#  out <- rep(0, 12)
#  names(out) <- c("SNP","CHR", "BP", "BP_IN_READ", "A1", "A2", "COUNT_A1", "COUNT_A2", "BETA", "SE", "Z", "P")
#  return(out)
#})
}, mc.cores=ncore)



# Preprare output matrix (ugly but verks)
df.out           <- data.frame(matrix(unlist(out), ncol=12, byrow = T), stringsAsFactors=F)
rownames(df.out) <- df.out$SNP
colnames(df.out) <- c("SNP","CHR", "BP", "BP_IN_READ", "A1", "A2", "COUNT_A1", "COUNT_A2", "BETA", "SE", "Z", "P")
df.out           <- df.out[,c("SNP","CHR", "BP", "P", "BETA", "SE", "Z", "A1", "A2", "COUNT_A1", "COUNT_A2")]
df.out$BP        <- as.numeric(df.out$BP)
df.out           <- df.out[!is.na(df.out$BP),]
df.out           <- df.out[order(df.out$CHR, df.out$BP),]

write.table(df.out, sep="\t", file="SuRE_CeD_SNP_CacoStim_ttest_covar.assoc", quote=F, row.names=F)
