#!/bin/bash

ml Java

set -e



# Overlap RAW
FILES=""

for file in ../1_concat_info_files/output/*.info.gz;
do
FILES="$FILES -i $file"
done

#FILES="-i info_files/SuRE_4.info.gz"

CMD="java -Xmx300G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T MakeBarcodeStats \
-b ../../1_cDNA/1_make_bacode_counts/output/k562/B1_T1.barcode.counts.gz \
-o testing \
-t INFO \
$FILES"


#echo $CMD
#eval $CMD


# Overlap iPCR

FILES=""

for file in output/collapsed_recode/*.ipcr.gz;
do
FILES="$FILES -i $file"
done

#FILES="-i info_files/SuRE_4.info.gz"

# -b ../../1_cDNA/1_make_bacode_counts/output/K562_all.barcode.counts.gz \

CMD="java -Xmx300G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T MakeBarcodeStats \
-b ../../1_cDNA/1_make_bacode_counts/output/jurkat/Jurkat_B1_run3.barcode.counts.gz \
-o testing \
-t IPCR \
$FILES"


echo $CMD
eval $CMD
