#!/bin/bash
#SBATCH --time=00:59:00
#SBATCH --mem=128G

set -e

ml Java

SAMPLE=$1

CMD="java -Xmx124G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T SubsetBam \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v2/${SAMPLE}.ipcr.bgz \
-b ../../1_alignment/4_merge_bam/output/${SAMPLE}_merged.querysorted.bam \
-o output/${SAMPLE}.dedup \
-s"

echo $CMD
eval $CMD
