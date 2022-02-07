#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=128G

ml Java

set -e

mkdir -p output/uncollapsed
mkdir -p output/collapsed


INPUT_BARCODES=""

for file in ../../1_cDNA/1_make_bacode_counts/output/*/*.barcode.counts.gz;
do
INPUT_BARCODES="${INPUT_BARCODES} -n $file"
done


# Make the iPCR file and merge with barcode data
CMD="java -Xmx124G -jar ../../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T MakeIpcrFile \
-i ../../1_alignment/4_merge_bam/output/${1}_merged.querysorted.bam \
-b ../../1_alignment/2_trim_adapters/output/${1}.info.gz \
-o output/uncollapsed/${1} \
-t IPCR \
${INPUT_BARCODES}"

echo $CMD
eval $CMD


# Collpase duplicate iPCR records
CMD="java -Xmx124G -jar ../../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T CollapseIpcr \
-i output/uncollapsed/${1}.ipcr \
-o output/collapsed/${1} \
-t IPCR_INDEXED"

echo $CMD
eval $CMD

# Not needed for Indexed IPCR
#gzip -f output/collapsed/${1}.*

# Cleanup tmpfiles
rm output/uncollapsed/${1}.*




