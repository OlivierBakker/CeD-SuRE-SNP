#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=65G

set -e

ml Java

OUTPUT=$2
IPCR_TOOLS="../../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar"
SAMPLE=$1

# Generate the input CLA
CMD="java -Xmx64G -jar ${IPCR_TOOLS} \
-T MakeBarcodeStats \
-b ../1_make_bacode_counts/output/${SAMPLE}/${SAMPLE}.barcode.counts.gz \
-o output/${SAMPLE} \
-i ../../2_make_ipcr_files/1_merge_with_barcodes/output/collapsed/sure_pilot_B.ipcr.bgz"

echo $CMD
eval $CMD


#for file in ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed/*.ipcr.bgz;
#do

#bn=$(basename $file)


# Generate the input CLA
#CMD="java -Xmx64G -jar ${IPCR_TOOLS} \
#-T MakeBarcodeStats \
#-b ../1_make_bacode_counts/output/${SAMPLE}/${SAMPLE}.barcode.counts.gz \
#-o output/${SAMPLE}_${bn} \
#-i $file"

#eval $CMD
#tail -n+3 -q output/${SAMPLE}_${bn}.barcodeOverlapPerBin.tsv | awk '{for(i=0;i<=20;++i)print $i}'

#done


