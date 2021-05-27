library(data.table)

trynka <- fread("~/Documents/data/Trynka/TrynkaSamplesFreezerLocations.csv", sep="\t", data.table=F)
trynka$`Location 2`[trynka$`Location 2` == ""] <- NA

# Select only cohorts were material is availible
#cohorts <- c("Netherlands_Cases", "Netherlands_Controls", "Poland_Cases", "Poland_Controls")
cohorts <- c("Italy_MilanRomeNaples_Cases", "Italy_MilanRomeNaples_Controls", "Netherlands_Cases", "Netherlands_Controls", "Poland_Cases", "Poland_Controls")
final.set <- trynka[trynka$Cohort %in% cohorts, ]

# Select items with duplicate freezer locations
final.set[is.na(final.set$`Location 2`),"Location 2"] <- final.set[is.na(final.set$`Location 2`), "Location"]
duplicates <- final.set[final.set$Location != final.set$`Location 2`, ]

# Remove samples with duplicate freezer locations
final.set <- final.set[final.set$Location == final.set$`Location 2`, ]

# Remove samples were no material is availible
final.set <- final.set[final.set$Location != "x",]

# Remove Serena's samples
dup.serena <- c("210-4258","210-3977","207-0025_5450377035_R03C01","060883","060882","060878","208-5145","060881","210-7565","209-10218")
final.set <- final.set[!final.set[,2] %in% dup.serena,]

# Some id's are differnent in the bimbam
converter           <- fread("~/Documents/data/Trynka/NonMatchingIdLinkageFile.txt", data.table = F)
rownames(converter) <- converter[,7]
final.set[final.set[,2] %in% intersect(final.set[,2], rownames(converter)),2]       <- converter[intersect(final.set[,2], rownames(converter)), 2]


#final.set[final.set$Location %in% c("Netherlands_Cases", "Netherlands_Controls"), 2] <- gsub("^00([1-9]\\d{3})", "\\1", final.set[final.set$Location %in% c("Netherlands_Cases", "Netherlands_Controls"),2])
write.table(final.set[,c(1,2)], file="~/Documents/data/Trynka/TrynkaAvaillibleSampleList.txt", quote=F, col.names=F, row.names=F)



