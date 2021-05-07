
library(wordcloud)
mds <- read.table("wd_genotype_eval/data/plink.mds", header=T, stringsAsFactors = F )
info <- read.table("data/samplesheet.tsv", header=T, stringsAsFactors = F)[-31,]
info$trynkaId <- gsub("^0+(.*)", "\\1", info$trynkaId)

rownames(info) <- info$trynkaId

# Harmonize id's
mds[mds$FID=="SuRE", 2] <- paste0("SuRE_", mds[mds$FID=="SuRE", 2])
mds$IID <- gsub("r_(.*)_r_.*", "\\1", mds$IID)

mds <- mds[mds$IID %in% c(intersect(mds$IID, info$trynkaId), info$sureId),]

mds$IID[mds$FID == "0"] <- info[as.character(mds[mds$FID == "0",]$IID), ]$sureId

cols <- mds$FID
cols[cols == "0"] <- "red"
cols[cols == "SuRE"] <- "blue"

plot(mds$C1, mds$C2, pch=20, col=cols)
textplot(mds$C1, mds$C2, mds$IID, new=F)
