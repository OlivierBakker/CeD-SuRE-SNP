#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=13G

set -e
ml Java


INPUT_FILES=""
for file in ../../2_make_ipcr_files/1_merge_with_barcodes/output/collapsed/*.ipcr.bgz;
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

mkdir -p output/

CMD="java -Xmx12G -jar ../../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T Recode \
-t MACS \
-o output/${SAMPLES_NAME} \
-z \
-k IPCR_INDEXED \
${INPUT_FILES} \
${SAMPLES_ARG}"

echo $CMD
eval $CMD


