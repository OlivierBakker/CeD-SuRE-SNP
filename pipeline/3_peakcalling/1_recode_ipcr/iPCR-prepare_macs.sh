#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=13G

set -e
ml Java


INPUT_FILES=""
for file in ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/*.ipcr.bgz;
do
INPUT_FILES="$INPUT_FILES -i $file"
done

SAMPLES=$1
SAMPLES_ARG=""
SAMPLES_NAME=""

for sample in ${SAMPLES};
do
SAMPLES_ARG="${SAMPLES_ARG} -s ${sample}"
SAMPLES_NAME="${SAMPLES_NAME}+${sample}"
done

SAMPLES_NAME="${SAMPLES_NAME:1}"

mkdir -p output/ced_regions

CMD="java -Xmx12G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T Recode \
-t MACS \
-o output/ced_regions/${SAMPLES_NAME} \
-z \
-rf ../../0_data/CeDRegionV4.bed \
-f 'SAMPLE_BC_GT_EQ;500:TRUE:${SAMPLES_NAME}' \
-k IPCR_INDEXED \
${INPUT_FILES} \
${SAMPLES_ARG}"

echo $CMD
eval $CMD

mkdir -p output/genome_wide_regions

CMD="java -Xmx12G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T Recode \
-t MACS \
-o output/genome_wide_regions/${SAMPLES_NAME} \
-z \
-rf ../../0_data/WGRegions_noCeD.bed \
-f 'SAMPLE_BC_GT_EQ;500:TRUE:${SAMPLES_NAME}' \
-k IPCR_INDEXED \
${INPUT_FILES} \
${SAMPLES_ARG}"

echo $CMD
eval $CMD


