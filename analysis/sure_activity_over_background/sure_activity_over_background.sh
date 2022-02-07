#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G


ml Java

# Make the arguments for the input iPCR files
INPUT_FILES=""
for file in ../../pipeline/2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v4/*.ipcr.bgz;
do
INPUT_FILES="$INPUT_FILES -i $file"
done


java -Xmx16G \
-jar ../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T SureActivityOverBackground \
${INPUT_FILES} \
-f ../../pipeline/0_data/CeDRegionV4.bed \
-q ../../pipeline/3_peakcalling/3_merge_peaks/output/ced_regions/${1}_x3_ced_regions_bmerged.narrowPeak \
-p 100 \
-s ${1}_B1_T1 -s ${1}_B1_T2 -s ${1}_B2_T1 -s ${1}_B2_T2

