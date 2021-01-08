table.file <- "SuRE53_unfiltered.table"


variant.metrics <- read.table(table.file, stringsAsFactors = F, header=T)


plot(density(variant.metrics$FS))
plot(density(variant.metrics$FS), xlim=c(0,20))
