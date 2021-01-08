library(data.table)
library(MASS)
library(parallel)

#ncore <- 22
ncore <- 16


x    <- fread("zcat SuRE_CeD_v2.minimal.allele.specific.ipcr.gz", header=F, sep="\t")
snps <- as.character(fread("unique_snps_full.txt", data.table=F, sep="\t", header=F)[,1])
head(x)

# Neg binom based
out <- mclapply(snps, function(snp) {
  cur.x                            <- as.data.frame(x[ V2 == snp ])
  
  # K562 total count
  #cur.x$total                      <- cur.x$V26 + cur.x$V27 + cur.x$V28 + cur.x$V29
  
  # Caco total count  
  #cur.x$total                      <- cur.x$V8 + cur.x$V9 + cur.x$V10 + cur.x$V11
  
  # Caco stim total count
  cur.x$total                      <- cur.x$V12 + cur.x$V13 + cur.x$V14 + cur.x$V15
  
  # Remove zeroes and NA's
  cur.x[cur.x$total == 0, "total"] <- NA
  cur.x                            <- cur.x[!is.na(cur.x$total),]
  #cur.x$norm.count                <- ceiling(cur.x$total / cur.x$V7)
  
  # Remove outliers
  cur.x$z                          <- 2 * sqrt(cur.x$total)
  cur.x                            <- cur.x[cur.x$z < (median(cur.x$z) + 5),]
  #cur.x                           <- cur.x[cur.x$total > 1,]
  
  if (length(table(cur.x$V6)) == 2) {
    if (min(table(cur.x$V6)) > 12) {
      
      # Covariate model
      cur.x$V6                        <- as.factor(cur.x$V6)
      nb.reg                          <- glm.nb(total ~ V7 + V6, data=cur.x)
      
      # Pseudo count model
      #nb.reg                          <- glm.nb(norm.count ~ V6, data=cur.x)
      
      out <- c(snp, unlist(cur.x[1,3:5]), unlist(names(table(cur.x$V6))), table(cur.x$V6), summary(nb.reg)$coefficients[3,])
    } else {
      out <- c(snp, unlist(cur.x[1,3:5]), unlist(names(table(cur.x$V6))), table(cur.x$V6), 0,0,0,1)
    }
  } else {
    out <- c(snp, unlist(cur.x[1,3:5]), "NA", "NA", "NA", "NA", 0,0,0,1)
  }
  
  names(out) <- c("SNP","CHR", "BP", "BP_IN_READ", "A1", "A2", "COUNT_A1", "COUNT_A2", "BETA", "SE", "Z", "P")
  return(out)
}, mc.cores=ncore)


# Preprare output matrix (ugly but verks)
df.out           <- data.frame(matrix(unlist(out), ncol=12, byrow = T), stringsAsFactors=F)
rownames(df.out) <- df.out$SNP
colnames(df.out) <- c("SNP","CHR", "BP", "BP_IN_READ", "A1", "A2", "COUNT_A1", "COUNT_A2", "BETA", "SE", "Z", "P")
df.out           <- df.out[,c("SNP","CHR", "BP", "P", "BETA", "SE", "Z", "A1", "A2", "COUNT_A1", "COUNT_A2")]
df.out$BP        <- as.numeric(df.out$BP)
df.out           <- df.out[!is.na(df.out$BP),]
df.out           <- df.out[order(df.out$CHR, df.out$BP),]

write.table(df.out, sep="\t", file="SuRE_CeD_CacoStim_negbinom_covar_outlier_rm.assoc", quote=F, row.names=F)
