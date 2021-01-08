#!/bin/bash

set -e

INPUTS=""

mkdir -p output/summaries

while read sample;
do

INPUTS="$INPUTS -i output/collapsed/${sample}.ipcr.bgz"

CMD="java -Xmx4G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T GetInsertSizes \
-t IPCR \
-o output/summaries/${sample}_ipcr_insert_sizes \
-i output/collapsed/${sample}.ipcr.bgz"

#echo $CMD
#eval $CMD


done < ../../0_data/samples.txt

CMD="java -Xmx4G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T GetInsertSizes \
-t IPCR \
-o output/summaries/SuRE_CeD_all_ipcr_insert_sizes \
${INPUTS}"

echo $CMD
eval $CMD
