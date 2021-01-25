#!/bin/bash

ml Java


INPUT_FILES=""
for file in ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed/*.ipcr.bgz;
do
INPUT_FILES="$INPUT_FILES -i $file"
done

SAMPLE=$1

CMD="java -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T Recode \
-k IPCR_INDEXED \
-t IPCR \
-f 'ANY_BC_GT_EQ;100' \
-o output/${SAMPLE}_qteq100 \
${INPUT_FILES}"

echo $CMD
eval $CMD
