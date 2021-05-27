library(data.table)
library(MASS)
library(parallel)

ncore          <- 22
x              <- fread("zcat SuRE_CeD_snps_indels_per_strand.minimal.ipcr.gz", header=F, sep="\t")
snps           <- as.character(fread("unique_snps_indels_full.txt", data.table=F, sep="\t", header=F)[,1])
min.allele     <- 12
max.cdna.count <- 500
#args       <- commandArgs(trailingOnly=T)
#celltype   <- args[1]
#strand     <- args[2]

associate.snp <- function(snp, celltype, strand="both") {
  
  # Select the current SNP
  if (strand=="both") {
    cur.x                              <- as.data.frame(x[ V2 == snp ])
  } else if (strand == "+" || strand == "-") {
    cur.x                              <- x[ V2 == snp ]
    cur.x                              <- as.data.frame(cur.x[ V7 == strand ])
  } else {
    cat("[ERROR] No valid strand provided. Must be '+', '-' or 'both' \n")
    q(save="no", status=-1)
  }
  
  # Determine the current celltype to regress on
  if (celltype == "Jurkat") {
    cur.x$total                      <- cur.x$V9 + cur.x$V10 + cur.x$V11 + cur.x$V12
  } else if (celltype == "JurkatStim") {
    cur.x$total                      <- cur.x$V13 + cur.x$V14 + cur.x$V15 + cur.x$V16
  } else if (celltype == "Caco2") {
    cur.x$total                      <- cur.x$V17 + cur.x$V18 + cur.x$V19 + cur.x$V20
  } else if (celltype == "Caco2Stim") {
    cur.x$total                      <- cur.x$V21 + cur.x$V22 + cur.x$V23 + cur.x$V24
  } else if (celltype == "K562") {
    cur.x$total                      <- cur.x$V25 + cur.x$V26 + cur.x$V27 + cur.x$V28
  } else {
    cat("[ERROR] No valid celltype provided.\n")
    q(save="no", status=-1)
  }
  
  # Remove outliers
  cur.x          <- cur.x[cur.x$total < max.cdna.count, ]
  
  # Calculate allele counts, with zeroes and outliers
  cur.tab        <- table(cur.x$V6)
  
  # Filter on SNPs or INDELs with != 2 alleles
  if (length(cur.tab) == 2) {
    
    # Strings containing the allele 1 & 2
    a1               <- names(cur.tab)[1]
    a2               <- names(cur.tab)[2]
    
    # Total allele count including zeroes
    c.a1             <- sum(cur.x[cur.x$V6 == a1, "V8"])
    c.a2             <- sum(cur.x[cur.x$V6 == a2, "V8"])
    
    # Remove zeroes
    cur.x            <- cur.x[cur.x$total != 0,]
    
    # Calculate allele counts, without zeroes and outliers
    cur.tab          <- table(cur.x$V6)
    
    # Filter SNPs or INDELs were the minor allele has at least N plasmids
    if ((min(cur.tab) > min.allele) & (length(cur.tab) == 2)) {
      
      # Total allele count excluding zeroes
      c.a3             <- sum(cur.x[cur.x$V6 == a1, "V8"])
      c.a4             <- sum(cur.x[cur.x$V6 == a2, "V8"])
      
      # Calculate zero inflation statistics
      mat            <- matrix(c(c.a3,
                                 c.a1 - c.a3,
                                 c.a4,
                                 c.a2 - c.a4), ncol=2, byrow=T)
      zero.inflation <- fisher.test(mat)
      
      # Reset the a1 & a2 to ensure they are consisent
      a1               <- names(cur.tab)[1]
      a2               <- names(cur.tab)[2]
      
      # Normalize with respect to iPCR
      cur.x$V6                        <- as.factor(cur.x$V6)
      cur.x$norm                      <- log2(cur.x$total / cur.x$V8)
      
      # Mean of A1 / A2
      mean.a1                         <- mean(cur.x[cur.x$V6 == a1, "norm"])
      mean.a2                         <- mean(cur.x[cur.x$V6 == a2, "norm"])
      

      nb.reg <- glm.nb(total ~ V6, data=cur.x)
      
      # Theta of fitted negbinom
      theta  <- nb.reg$theta
      
      # Mean and variance
      mu     <- mean(cur.x$total)
      var    <- mu + (mu^2 / theta)
      
      # Dispertion
      disp   <- sum((nb.reg$weights * nb.reg$residuals^2)[nb.reg$weights > 0]) /  nb.reg$df.residual
      #disp   <- var / mu^2

      #tmp    <- glm(total ~ V6, data=cur.x, family = negative.binomial(theta=1.17994))
      #disp <- sum((tmp$weights * tmp$residuals^2)[tmp$weights > 0]) /  tmp$df.residual
      
    
      # Gather output
      out <- c(snp,                           # SNP id
               unlist(cur.x[1, 3:4]),         # SNP info
               strand,                        # Orientation
               a1, a2,                        # Allele 1 & 2
               cur.tab,                       # Allele counts 1 & 2
               c.a1, c.a2,                    # Total allele counts 1 & 2
               mean.a1,                       # Mean norm. expr. A1
               mean.a2,                       # Mean norm. expr. A2
               log2(mean.a1 / mean.a2),       # Ratio of means
               disp,                          # Dispertion
               theta,                         # Dispertion param for r negbinom
               mean(cur.x$total),             # Mean
               var                           # Variance
      )

      
    } else { out <- c(snp, unlist(cur.x[1,3:4]), strand, a1, a2, cur.tab[1], cur.tab[2], c.a1, c.a2, 0,0,0,0,0,0,0)}
  } else {   out <- c(snp, unlist(cur.x[1,3:4]), strand, "NA", "NA", 0, 0, 0,0,0,0,0,0,0,0,0)}
  
  names(out) <- c("SNP","CHR", "BP", "STRAND", "A1", "A2", "COUNT_A1", "COUNT_A2", "TOTAL_A1", "TOTAL_A2", "MU_A1", "MU_A2", "LOG_MU_RATIO", "DISP", "THETA", "MEAN", "VAR")
  return(out)
}


for (celltype in c("K562", "Caco2", "Caco2Stim", "Jurkat", "JurkatStim")) {
  for (strand in c("both", "+", "-")) {
    
    cat("[INFO] Calculating associations for: ", celltype, " strand: ", strand, "\n")
    
    out              <- mclapply(snps, associate.snp, celltype=celltype, strand=strand, mc.cores=ncore)
    
    # Preprare output matrix
    df.out           <- data.frame(matrix(unlist(out), ncol=17, byrow=T), stringsAsFactors=F)
    rownames(df.out) <- df.out$SNP
    colnames(df.out) <- c("SNP","CHR", "BP", "STRAND", "A1", "A2", "COUNT_A1", "COUNT_A2","TOTAL_A1", "TOTAL_A2", "MU_A1", "MU_A2", "LOG_MU_RATIO", "DISP", "THETA", "MEAN", "VAR" )
   
    tmp <- df.out[df.out$MU_A1 !=0,]
    plot(df.out$MEAN, df.out$DISP)
    plot(df.out$MEAN, df.out$THETA)
    plot(df.out$MEAN, df.out$VAR)
    
    dev.off()
    
    
     df.out           <- df.out[,c("SNP","CHR", "BP", "P", "STAT", "A1", "A2", "MU_A1", "MU_A2", "LOG_MU_RATIO", "STRAND", "COUNT_A1", "COUNT_A2","TOTAL_A1", "TOTAL_A2", "LOG_OR_ZERO" ,"P_ZERO")]
    df.out$BP        <- as.numeric(df.out$BP)
    df.out           <- df.out[!is.na(df.out$BP),]
    df.out           <- df.out[order(df.out$CHR, df.out$BP),]
    
    # Overide strand names to make compatible with old files
    if (strand=="both") {
      strand.2 <- "both"
    } else if (strand == "-") {
      strand.2 <- "minus"
    } else if (strand == "+") {
      strand.2 <- "plus"
    }
    
    # Full sumstats IGV compatible
    write.table(df.out, sep="\t", file=paste0("../output/SuRE_CeD_snps_indels_wilcox_", strand.2, "_", celltype ,".assoc"), quote=F, row.names=F)
    
    # For Excel annotation
    if (strand == "both") {
      df.out           <- df.out[,c("SNP", "A1", "COUNT_A1", "COUNT_A2", "P", "LOG_MU_RATIO", "LOG_OR_ZERO" ,"P_ZERO")]
      write.table(df.out, sep="\t", file=paste0("../output/", celltype, ".assoc.sure.annot"), quote=F, row.names=F)
    } else {
      df.out           <- df.out[,c("SNP","P", "LOG_MU_RATIO")]
      colnames(df.out) <- paste0(colnames(df.out), "_", strand)
      write.table(df.out, sep="\t", file=paste0("../output/", celltype, ".", strand.2, ".assoc.sure.annot"), quote=F, row.names=F)
    }
    
    
  }}


