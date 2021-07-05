library(data.table)
library(ggplot2)
library(GenomicRanges)
library(ggsignif)
library(gridExtra)
library(pheatmap)
library(RColorBrewer)

# Function definitions
#----------------------------------------------------------------------------------------
allele.specific.density <- function(x, y, legend=c("X", "Y"), main="Density plot", ...) {
  den.x <- density(x)
  den.y <- density(y)
  
  xlim <- c(min(c(min(den.x$x), min(den.y$x))), max(c(max(den.x$x), max(den.y$x))))
  ylim <- c(min(c(min(den.x$y), min(den.y$y))), max(c(max(den.x$y), max(den.y$y))))
  
  plot(den.x, col="blue", xlim=xlim, ylim=ylim, main=main,...)
  lines(den.y, col="red")
  legend("topright", legend=legend, fill=c("blue", "red"))
}

## ------------------------------------------------------------------------
## Make a PCA plot based on a prcomp object with the % of variance on the axis labels
pca.plot <- function(prcomp.obj, comp.1=1, comp.2=2, labels=NULL, fill=NULL, color=NULL, shape=NULL, size=1, main="PCA plot", ...){
  
  var.prop <- prcomp.obj$sdev^2/sum(prcomp.obj$sdev^2)
  df.plot  <- as.data.frame(prcomp.obj$x[,c(comp.1, comp.2)])
  
  if (is.null(fill)) {
    #df.plot$fill <- rep("orange", nrow(df.plot))
  } else {
    df.plot$fill <- fill
  }
  
  if (is.null(shape)) {
    # df.plot$shape <- rep("20", nrow(df.plot))
  } else {
    df.plot$shape <- shape
  }
  
  if (is.null(color)) {
    # df.plot$color <- rep("black", nrow(df.plot))
  } else {
    df.plot$color <- color
  }
  
  
  colnames(df.plot)[1:2] <- c("C1", "C2")
  
  p <- ggplot(df.plot, aes(y=C2, x=C1, shape=shape, color=color, fill=fill)) +
    geom_point(size=size, ...) +
    theme(panel.background=element_blank(),
          panel.grid.major=element_line("#E3E3E3", 0.5, 2),
          panel.grid.minor=element_line("#E3E3E3", 0.25, 2)) +
    labs(x=paste0("PC", comp.1, " :", round(var.prop[comp.1], 3)*100, "% variance"),
         y=paste0("PC", comp.2, " :", round(var.prop[comp.2], 3)*100, "% variance")) 
  
  if (!is.null(labels)) {
    p <- p + geom_text_repel(aes(label=labels))
  }
  
  return(p)
  
}

#----------------------------------------------------------------------------------------
simple.qq.plot <- function (observedPValues, ...) {
  observedPValues <- na.omit(observedPValues)
  plot(-log10(1:length(observedPValues)/length(observedPValues)), 
       -log10(sort(observedPValues)), ...)
  abline(0, 1, col = "red")
}

#----------------------------------------------------------------------------------------
fancy.qq.plot <- function (y, main="", col="black", highlight.col="blue") {
  y <- na.omit(y)
  x <- -log10(1:length(y)/length(y))
  y <- -log10(sort(y))
  
  thresh <- -log10(0.05/length(y))
  upper <- max(c(x, y)) + 1
  
  p <- ggplot(data.frame(x=x, y=y, col=y > thresh), mapping=aes(x=x, y=y, col=col)) +
    geom_point() +
    geom_abline(slope=1, intercept=0, col="black") +
    geom_hline(yintercept=thresh, lty=2, col="grey") +
    xlab("-log10(expected p-value)") +
    ylab("-log10(observed p-value)") +
    ggtitle(main) +
    ylim(c(0, upper)) +
    xlim(c(0, upper)) +
    coord_fixed() +
    annotate("text", y=01, x=upper-2, label=paste0("N=", length(y)), col="grey") +
    scale_color_manual(values=c(`TRUE`=highlight.col, `FALSE`=col), name="Bonferroni significant")
  
  return(p)
}


#----------------------------------------------------------------------------------------
read.ase.results <- function(path, qqman.compatible=F) {
  data <- read.table(path, stringsAsFactors = F, sep="\t", header=T)
  
  if (length(unique(data$STRAND)) > 1) {
    rownames(data) <- paste(data$SNP, data$STRAND, sep="_")
  } else {
    rownames(data) <- data$SNP
  }
  
  if (qqman.compatible) {
    data[data$CHR=="X", "CHR"] <- 23
    data[data$CHR=="Y", "CHR"] <- 24
    data$CHR <- as.numeric(data$CHR)
  }
  #data$SNP <- rownames(data)
  #data     <- data[data$P != 1,]
  
  return(data)
}

#----------------------------------------------------------------------------------------
make.ase.qqman.compatible <- function(data) {
    data[data$CHR=="X", "CHR"] <- 23
    data[data$CHR=="Y", "CHR"] <- 24
    data$CHR <- as.numeric(data$CHR)
  
  return(data)
}

#----------------------------------------------------------------------------------------
read.bedfile <- function(path) {
  peakset     <- read.table(path)[,1:3]

  # Convert to genomic ranges object
  peakset.gr     <- GRanges(seqnames=peakset[,1],
                              ranges=IRanges(start=as.numeric(peakset[,2]),
                                             end=as.numeric(peakset[,3])))
  
  
  return(list(data=peakset, genomicRanges=peakset.gr))
}


## ------------------------------------------------------------------------
# Simple plotting theme for ggplot using arial family font
theme.plain <- function(p, base_size = 11, base_family = "ArialMT") {
  p <- p + theme_grey(base_size = base_size, base_family = base_family) %+replace%
    theme(panel.background = element_blank(),
          panel.border = element_blank(),
          panel.grid.major = element_blank(),
          panel.grid.minor = element_blank(),
          axis.line = element_line(color="black", size=0.75),
          axis.ticks = element_line(size=0.75),
          axis.text = element_text(size=base_size, family="ArialMT", face="plain"),
          strip.background = element_blank(),
          legend.key = element_blank(),
          legend.text = element_text(size=base_size, family = "ArialMT", face="plain"),
          complete = TRUE,
          plot.title = element_text(hjust=0.5))
  return(p)
}

## ------------------------------------------------------------------------
read.full.ase <- function(folder.name) {
 
  cat("[WARN] This function is currently a bit hacked together, colnames may not be accurate!!!\n")
  files <- list.files(folder.name, pattern="*full.allele.specific.ipcr.gz.gz")
  
  i <- 0  
  for (file in files) {
    
    cur.file <- fread(paste0("zcat ",folder.name, "/", file), data.table=F)
    if (i ==0)  {
      output <- cur.file
    } else {
      output <- rbind(output, cur.file)
    }
  
    i <- i+1
  }
  
  output <- output[,-ncol(output)]
  colnames(output) <- c("barcode","readName","sequence","alignmentStart","alignmentEnd","variantId","variantType","variantStart","variantStartInRead","refAllele","dosg2Allele","dosg0Allele","alleleInRead","cigarString","strand","sampleId","ipcrCount","C_B1_T1","C_B1_T2","C_B2_T1","C_B2_T2","Cs_B1_T1","Cs_B1_T2","Cs_B2_T1","Cs_B2_T2","K_B1_T1","K_B1_T2","K_B2_T1","K_B2_T2")
  
  cat("[WARN] This function is currently a bit hacked together, colnames may not be accurate!!!\n")
  
  
  #rownames(output) <- output[,1]
  return(output)
}

## ------------------------------------------------------------------------
read.full.ipcr <- function(folder.name) {
  
  cat("[WARN] This function is currently a bit hacked together, colnames may not be accurate!!!\n")
  files <- list.files(folder.name, pattern="*.ipcr.bgz$")
  
  i <- 0  
  for (file in files) {
    
    cur.file <- fread(paste0("zcat ",folder.name, "/", file), data.table=F)
    if (i ==0)  {
      output <- cur.file
    } else {
      output <- rbind(output, cur.file)
    }
    
    i <- i+1
  }
    return(output)
}

## ------------------------------------------------------------------------
make.coverage.track <- function(inputranges, chr=chr) {
  from <- min(inputranges@ranges@start)
  to <- max(inputranges@ranges@start + inputranges@ranges@width)

  score <- sapply(from:to, function(i) {
    cur.range <- GRanges(seqnames = chr, ranges=IRanges(start=i, width=1))
    indices <- findOverlaps(inputranges, cur.range)@from
    #score <- c(score, (sum(inputranges[indices]$total) / sum(inputranges[indices]$ipcrCount)))
    return(sum(inputranges[indices]$total))
    
  })
  
  out <- GRanges(seqnames = chr, ranges=IRanges(start=from:to, width=1), score=score)
  return(out)
}

## ------------------------------------------------------------------------
# Simple heatmap with auto labels
simple.hm <- function(data, cellwidth=12, cellheight=12, limit=NULL, range="symmetric", min.value=0, palette=NULL, ...) {
  
  if (range == "symmetric") {
    break.list <- seq(-max(abs(data)), max(abs(data)), by=max(abs(data))/100)
    if (is.null(palette)) {palette="RdBu"}
    cols       <- colorRampPalette(rev(brewer.pal(n=7, name =palette)))(length(break.list))
  } else if (range == "absolute") {
    if (is.null(palette)) {palette="Reds"}
    break.list <- seq(min.value, max(abs(data)), by=max(abs(data))/100)
    cols       <- colorRampPalette(brewer.pal(n=7, name =palette))(length(break.list))
  } else if (range == "auto") {
    break.list <- seq(-min(data), max(data), by=max(abs(data))/100)
    if (is.null(palette)) {palette="RdBu"}
    cols       <- colorRampPalette(rev(brewer.pal(n=7, name =palette)))(length(break.list))
  } else  {
    cat("[ERROR] range must be symmetric, auto, or aboslute\n")
  }
  
  pheatmap(data,
           breaks=break.list,
           col=cols,
           cellwidth=cellwidth,
           cellheight=cellheight,
           ...)
  
}


## ------------------------------------------------------------------------
manhattan <- function (x, chr = "CHR", bp = "BP", p = "P", snp = "SNP", col = c("gray10", "gray60"),
                       chrlabs = NULL, suggestiveline = -log10(1e-05), col.highlight="blue",
          genomewideline = -log10(5e-08), highlight = NULL, logp = TRUE, 
          annotatePval = NULL, annotateTop = TRUE, ...) {
  CHR = BP = P = index = NULL
  if (!(chr %in% names(x))) 
    stop(paste("Column", chr, "not found!"))
  if (!(bp %in% names(x))) 
    stop(paste("Column", bp, "not found!"))
  if (!(p %in% names(x))) 
    stop(paste("Column", p, "not found!"))
  if (!(snp %in% names(x))) 
    warning(paste("No SNP column found. OK unless you're trying to highlight."))
  if (!is.numeric(x[[chr]])) 
    stop(paste(chr, "column should be numeric. Do you have 'X', 'Y', 'MT', etc? If so change to numbers and try again."))
  if (!is.numeric(x[[bp]])) 
    stop(paste(bp, "column should be numeric."))
  if (!is.numeric(x[[p]])) 
    stop(paste(p, "column should be numeric."))
  d = data.frame(CHR = x[[chr]], BP = x[[bp]], P = x[[p]])
  if (!is.null(x[[snp]])) 
    d = transform(d, SNP = x[[snp]])
  d <- subset(d, (is.numeric(CHR) & is.numeric(BP) & is.numeric(P)))
  d <- d[order(d$CHR, d$BP), ]
  if (logp) {
    d$logp <- -log10(d$P)
  }
  else {
    d$logp <- d$P
  }
  d$pos = NA
  d$index = NA
  ind = 0
  for (i in unique(d$CHR)) {
    ind = ind + 1
    d[d$CHR == i, ]$index = ind
  }
  nchr = length(unique(d$CHR))
  if (nchr == 1) {
    d$pos = d$BP
    ticks = floor(length(d$pos))/2 + 1
    xlabel = paste("Chromosome", unique(d$CHR), "position")
    labs = ticks
  }
  else {
    lastbase = 0
    ticks = NULL
    for (i in unique(d$index)) {
      if (i == 1) {
        d[d$index == i, ]$pos = d[d$index == i, ]$BP
      }
      else {
        lastbase = lastbase + tail(subset(d, index == 
                                            i - 1)$BP, 1)
        d[d$index == i, ]$pos = d[d$index == i, ]$BP + 
          lastbase
      }
      ticks = c(ticks, (min(d[d$index == i, ]$pos) + max(d[d$index == 
                                                             i, ]$pos))/2 + 1)
    }
    xlabel = "Chromosome"
    labs <- unique(d$CHR)
  }
  xmax = ceiling(max(d$pos) * 1.03)
  xmin = floor(max(d$pos) * -0.03)
  def_args <- list(xaxt = "n", bty = "n", xaxs = "i", yaxs = "i", 
                   las = 1, pch = 20, xlim = c(xmin, xmax), ylim = c(0, 
                                                                     ceiling(max(d$logp))), xlab = xlabel, ylab = expression(-log[10](italic(p))))
  dotargs <- list(...)
  do.call("plot", c(NA, dotargs, def_args[!names(def_args) %in% 
                                            names(dotargs)]))
  if (!is.null(chrlabs)) {
    if (is.character(chrlabs)) {
      if (length(chrlabs) == length(labs)) {
        labs <- chrlabs
      }
      else {
        warning("You're trying to specify chromosome labels but the number of labels != number of chromosomes.")
      }
    }
    else {
      warning("If you're trying to specify chromosome labels, chrlabs must be a character vector")
    }
  }
  if (nchr == 1) {
    axis(1, ...)
  }
  else {
    axis(1, at = ticks, labels = labs, ...)
  }
  col = rep(col, max(d$CHR))
  if (nchr == 1) {
    with(d, points(pos, logp, pch = 20, col = col[1], ...))
  }
  else {
    icol = 1
    for (i in unique(d$index)) {
      with(d[d$index == unique(d$index)[i], ], points(pos, 
                                                      logp, col = col[icol], pch = 20, ...))
      icol = icol + 1
    }
  }
  if (suggestiveline) 
    abline(h = suggestiveline, col = "blue")
  if (genomewideline) 
    abline(h = genomewideline, col = "red")
  if (!is.null(highlight)) {
    if (any(!(highlight %in% d$SNP))) 
      warning("You're trying to highlight SNPs that don't exist in your results.")
    d.highlight = d[which(d$SNP %in% highlight), ]
    with(d.highlight, points(pos, logp, col = col.highlight, 
                             pch = 20, ...))
  }
  if (!is.null(annotatePval)) {
    topHits = subset(d, P <= annotatePval)
    par(xpd = TRUE)
    if (annotateTop == FALSE) {
      with(subset(d, P <= annotatePval), textxy(pos, -log10(P), 
                                                offset = 0.625, labs = topHits$SNP, cex = 0.45), 
           ...)
    }
    else {
      topHits <- topHits[order(topHits$P), ]
      topSNPs <- NULL
      for (i in unique(topHits$CHR)) {
        chrSNPs <- topHits[topHits$CHR == i, ]
        topSNPs <- rbind(topSNPs, chrSNPs[1, ])
      }
      textxy(topSNPs$pos, -log10(topSNPs$P), offset = 0.625, 
             labs = topSNPs$SNP, cex = 0.5, ...)
    }
  }
  par(xpd = FALSE)
}

# Scatterplot where points are colored by p-value
#----------------------------------------------------------------------------------------
xy.plot.pvalue.colored <- function(auc.1, auc.pval.1, auc.2, auc.pval.2, xlab="X", ylab="Y", main=NULL, pval.col="either", pval.name.x="x", pval.name.y="y") {
  auc.pval.1[auc.1 == 0] <- 1
  auc.pval.2[auc.2 == 0] <- 1
  
  df.plot <- data.frame(auc.1=auc.1,
                        auc.2=auc.2,
                        signif.1 = auc.pval.1 < (0.05 / length(auc.1)), 
                        signif.2 = auc.pval.2 < (0.05 / length(auc.2)))
  
  df.plot                <- na.omit(df.plot)
  df.plot$signif         <- sum(df.plot$signif.1 + df.plot$signif.2)
  df.plot$signif.both    <- (df.plot$signif.1 + df.plot$signif.2) == 2
  df.plot$signif.either  <- (df.plot$signif.1 + df.plot$signif.2) > 0
  df.plot                <- df.plot[order(df.plot$signif.either),]
  
  if (pval.col=="either") {
    df.plot$signif.col    <- df.plot$signif.either
    point.cols <- c(`FALSE`="#2c6c70", `TRUE`="#0ae4f2")
    
  } else if (pval.col=="all") {
    df.plot$signif.col    <- rep("N.S.", nrow(df.plot))
    if (sum(df.plot$signif.1) >= 1) {
      df.plot[df.plot$signif.1,]$signif.col       <- pval.name.x
    }
    
    if (sum(df.plot$signif.2) >= 1) {
      df.plot[df.plot$signif.2,]$signif.col       <- pval.name.y
    }
    
    df.plot[!df.plot$signif.either,]$signif.col <- "N.S."
    
    if (sum(df.plot$signif.both) >= 1) {
      df.plot[df.plot$signif.both,]$signif.col    <-"Both"
    }
    
    point.cols <- c("#55B397", "#59B354", "#544CB0", "#9E46B3")
    names(point.cols) <- c(pval.name.x,  pval.name.y, "N.S.", "Both")
  } else if (pval.col=="both") {
    df.plot$signif.col    <- df.plot$signif.both
    point.cols <- c(`FALSE`="#2c6c70", `TRUE`="#0ae4f2")
  }
  
  lims <- c(min(c(auc.1, auc.2), na.rm=T), max(c(auc.1, auc.2), na.rm=T))  
  
  p <- ggplot(data=df.plot, mapping=aes(x=auc.1, y=auc.2)) +
    geom_point(alpha=0.75, mapping=aes(col=df.plot$signif.col)) +
    geom_abline(slope=1, intercept=0, col="grey", lty=2) +
    coord_fixed() +
    xlab(xlab) +
    ylab(ylab) +
    ggtitle(main) + 
    scale_color_manual(values=point.cols, name=paste0("Signif. ", pval.col)) +
    geom_smooth(method="lm") +
    xlim(lims) +
    ylim(lims)
  
  return(p)
}

# Simple scatterplot
#----------------------------------------------------------------------------------------
xy.plot<- function(x, y, xlab="X", ylab="Y", main=NULL, col="") {

  df.plot <- data.frame(x=x,
                        y=y)

  lims <- c(min(c(x, y), na.rm=T), max(c(x, y), na.rm=T))  
  
  p <- ggplot(data=df.plot, mapping=aes(x=x, y=y)) +
    geom_point(alpha=0.75, color=col) +
    geom_abline(slope=1, intercept=0, col="grey", lty=2) +
    coord_fixed() +
    xlab(xlab) +
    ylab(ylab) +
    ggtitle(main) + 
    geom_smooth(method="lm") +
    xlim(lims) +
    ylim(lims)
  
  return(p)
}

# Function to convert string of chr:start-end to dataframe
#----------------------------------------------------------------------------------------
get.as.grange <- function(range.names, return.as.grange=F, chr.prefix="") {
  # Make a dataframe of the genomic range 
  ranges <- as.data.frame(t(as.data.frame(lapply(strsplit(range.names,":"), function(x){
    tmp <- strsplit(x[2], "-")[[1]]
    return(c(x[1],tmp[1], tmp[2]))
  }))), stringsAsFactors=F)
  
  # Convert strings to numeric
  ranges[,2] <- as.numeric(ranges[,2])
  ranges[,3] <- as.numeric(ranges[,3])
  rownames(ranges) <- range.names
  
  # Return either the dataframe or as a genomicRange object
  if (return.as.grange) {
    tmp <- GRanges(seqnames=paste0(chr.prefix, ranges[,1]), ranges=IRanges(start=ranges[,2], end=ranges[,3]))
    names(tmp) <- rownames(ranges)
    return(tmp)
  } else {
    return(ranges)
  }
}

# Simplify the biotypes fro enembl genes to coding / non-coding
#----------------------------------------------------------------------------------------
clean.ensembl.biotypes <- function(ensembl) {
  # non-coding = includes 3prime_overlapping_ncrna, antisense, lincRNA, miRNA, processed_transcript, sense_overlapping, sense_intronic, misc_RNA
  # coding = includes protein_coding, pseudogene, IG_C_pseudogene, IG_V_gene
  # r/sn/snoRNAs = blank includes snRNA, rRNA, snoRNA (the last three are clearly defined ncRNAs with specific function, thus not of interest to this class)
  ensembl$Gene.type <- gsub("protein_coding", "coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("pseudogene", "coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("polymorphic_coding RNA", "coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("processed_coding RNA", "coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("Mt_tRNA", "coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("IG\\_.*", "coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("TR\\_.*", "coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("3prime_overlapping_ncrna", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("antisense", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("lincRNA", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("miRNA", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("processed_transcript", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("sense_overlapping", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("sense_intronic", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("misc_RNA", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("snoRNA", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("snRNA", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("rRNA", "non-coding RNA", ensembl$Gene.type)
  ensembl$Gene.type <- gsub("Mt_r/sn/snoRNAs", "non-coding RNA", ensembl$Gene.type)
  
  return(ensembl)
}

# Read an ensembl file downloaded from biomart
#----------------------------------------------------------------------------------------
read.ensembl <- function(file, clean.biotypes=F) {
  ensembl                    <- read.table(file, sep="\t", header=T, stringsAsFactors = F)
  ensembl                    <- ensembl[!duplicated(ensembl$Gene.stable.ID),]
  rownames(ensembl)          <- ensembl$Gene.stable.ID
  ensembl$Gene.length        <- ensembl$Gene.end..bp. - ensembl$Gene.start..bp.
  
  # Clean up the biptypes
  if (clean.biotypes) {
    ensembl <- clean.ensembl.biotypes(ensembl)
  }
  
  return(ensembl)
}

# Calculate the TMP normalized counts for a count matrix
#----------------------------------------------------------------------------------------
calculate.tmp           <- function(count.matrix, size.vector) {
  
  norm.count.matrix <- count.matrix / size.vector
  scale.factor      <- colSums(norm.count.matrix) / 1e6
  norm.count.matrix <- t(apply(norm.count.matrix, 1, function(x){x / scale.factor}))
  
  return(norm.count.matrix)
}

## ------------------------------------------------------------------------
sure.palette <- list(celltype_stim=c(`k562`="#FE0000", 
                                `caco-2`="#0000CE",
                                `caco-2-stim`="#7EC1EE",
                                `jurkat`="#007D5A",
                                `jurkat-stim`="#00D69A"),
                     celltype_stim_alt=c(`K`="#FE0000", 
                                     `C`="#0000CE",
                                     `Cs`="#7EC1EE",
                                     `J`="#007D5A",
                                     `Js`="#00D69A"),
                     celltype=c(`k562`="#FE0000", 
                                `caco-2`="#0000CE",
                                `jurkat`="#007D5A"),
                     stimulation=c(`aCD3/aCD28`="#00D69A",
                                   `IFNy`="#7EC1EE",
                                   `baseline`="#0965B0"))

## ------------------------------------------------------------------------
merge.bedgraph.tracks <- function(tmp.plus, tmp.minus) {
  
  rownames(tmp.plus) <- tmp.plus[,2]
  rownames(tmp.minus) <- tmp.minus[,2]
  
  ol.pos         <- as.character(intersect(tmp.plus[,2], tmp.minus[,2]))
  
  df.plot        <- data.frame(plus=tmp.plus[ol.pos,4],
                               minus=tmp.minus[ol.pos,4],
                               chr=tmp.plus[ol.pos, 1],
                               pos=tmp.plus[ol.pos,2],
                               
                               stringsAsFactors = F)
  
  uniq.plus     <- tmp.plus[!rownames(tmp.plus) %in% ol.pos,]
  df.plot       <- rbind(df.plot,
                         data.frame(plus=uniq.plus[,4],
                                    minus=0,
                                    chr=uniq.plus[,1],
                                    pos=uniq.plus[,2]))
  
  uniq.minus    <- tmp.minus[!rownames(tmp.minus) %in% ol.pos,]
  df.plot       <- rbind(df.plot,
                         data.frame(plus=0,
                                    minus=uniq.minus[,4],
                                    chr=uniq.minus[,1],
                                    pos=uniq.minus[,2]))
  
  return(df.plot)
}


## ------------------------------------------------------------------------
convert.pvalue.to.dits <- function(x) {
  if (x > 0.05) {
    return("")
  } else if (x < 5e-4) {
    return("***")
  } else if (x < 5e-3) {
    return("**")
  } else if (x < 5e-2) {
    return("*")
  }
}
