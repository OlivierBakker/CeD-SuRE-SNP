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
  data <- read.table(path, stringsAsFactors = F, sep="\t", header=T, row.names = 1)
  
  if (qqman.compatible) {
    data[data$CHR=="X", "CHR"] <- 23
    data[data$CHR=="Y", "CHR"] <- 24
    data$CHR <- as.numeric(data$CHR)
  }
  data$SNP <- rownames(data)
  data     <- data[data$P != 1,]
  
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

#----------------------------------------------------------------------------------------
get.snp.data <- function(raw.ase.data, snp, sample) {
  cur.x                                 <- as.data.frame(raw.ase.data[ V2 == snp ])
  
  # K562 total count
  cur.x$k562.total                      <- cur.x$V16 + cur.x$V17 + cur.x$V18 + cur.x$V19
  
  # Caco total count
  cur.x$caco.total                      <- cur.x$V8 + cur.x$V9 + cur.x$V10 + cur.x$V11
  
  # Caco stim total count
  cur.x$cacoStim.total                  <- cur.x$V12 + cur.x$V13 + cur.x$V14 + cur.x$V15
  
  
  if (sample %in% c("k562", "K562")) {
    cur.x$total <- cur.x$k562.total
  } else if (sample %in% c("caco", "caco2", "Caco", "Caco2")) {
    cur.x$total <- cur.x$caco.total
  } else if (sample %in% c("cacoStim", "CacoStim", "caco2Stim", "caco2stim")) {
    cur.x$total <- cur.x$cacoStim.total
  } else {
    cat("[ERROR] sample not found")
  }
  
  # Remove zeroes, extreme outliers and NA's
  cur.x[cur.x$total == 0 | cur.x$total > 500, "total"] <- NA
  
  cur.x                            <- cur.x[!is.na(cur.x$total),]
  cur.x$norm                       <- log2((cur.x$total) / cur.x$V7)
  cur.x$norm[cur.x$norm == -Inf]   <- NA
  
  cur.tab                          <- table(cur.x$V6)
  
  label.lookup                     <- c(paste0(names(cur.tab)[1], " (N=", cur.tab[1], ")"),
                                        paste0(names(cur.tab)[2], " (N=", cur.tab[2], ")"))
  names(label.lookup)              <- names(cur.tab)
  cur.x$V6                         <- label.lookup[cur.x$V6]
  
  cur.x$V6                         <- factor(cur.x$V6)
  
  return(cur.x)
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
# ASE plot using normalized sure scores
plot.ase.normalized <- function(cur.x, cols=c("#FF6863", "#4082F5"), col="black") {
  p <- ggplot(cur.x, aes(y=norm, x=V6, fill=V6)) + 
    geom_violin(col=NA) +
    geom_boxplot(alpha=0.5, width=0.1, position="identity", col=col) +
    xlab(paste0("Genotype for: ", snp)) +
    ylab(paste0("Normalized SuRE score for ", sample)) +
    scale_fill_manual(name="Genotype", values=cols) +
    geom_signif(comparisons =list(levels(cur.x$V6)), tip_length = 0)

  p1 <- theme.plain(p)
  return(p1)
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
simple.hm <- function(data, cellwidth=12, cellheight=12, limit=NULL, range="symmetric", min.value=0, ...) {
  
  if (range == "symmetric") {
    break.list <- seq(-max(abs(data)), max(abs(data)), by=max(abs(data))/100)
    cols       <- colorRampPalette(rev(brewer.pal(n=7, name ="RdBu")))(length(break.list))
  } else if (range == "absolute") {
    break.list <- seq(min.value, max(abs(data)), by=max(abs(data))/100)
    cols       <- colorRampPalette(brewer.pal(n=7, name ="Reds"))(length(break.list))
  } else if (range == "auto") {
    break.list <- seq(-min(data), max(data), by=max(abs(data))/100)
    cols       <- colorRampPalette(rev(brewer.pal(n=7, name ="RdBu")))(length(break.list))
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

## ------------------------------------------------------------------------
sure.palette <- list(celltype_stim=c(`k562`="#FE0000", 
                                `caco-2`="#0000CE",
                                `caco-2-stim`="#7EC1EE",
                                `jurkat`="#007D5A",
                                `jurkat-stim`="#00D69A"),
                     celltype=c(`k562`="#FE0000", 
                                `caco-2`="#0000CE",
                                `jurkat`="#007D5A"),
                     stimulation=c(`aCD3/aCD28`="#00D69A",
                                   `IFNy`="#7EC1EE",
                                   `baseline`="#0965B0"))
