#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=128G

ml Java

set -e

mkdir -p output/collapsed_v3

INPUT_BARCODES=""

for file in ../../1_cDNA/1_make_bacode_counts/output/J*_B*_T*/*.barcode.counts.gz;
do
INPUT_BARCODES="${INPUT_BARCODES} -b $file"
done


# Make the iPCR file and merge with barcode data
# Collpase duplicate iPCR records
CMD="java -Xmx126G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T Recode \
-i output/collapsed_v2/${1}.ipcr.bgz \
-o output/collapsed_v3/${1} \
-t IPCR_INDEXED \
-z \
${INPUT_BARCODES}"

#--replace-cdna-samples \

echo $CMD
eval $CMD





