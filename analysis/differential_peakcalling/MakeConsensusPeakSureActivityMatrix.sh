#!/bin/bash

ml Java

IPCR_TOOLS="java -jar -Xmx32G ../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar"

PEAKS=""

for cur_peak in data/peaks/*.narrowPeak;
do
PEAKS="${PEAKS} -i ${cur_peak}"
done;

OUTPUT="SuRE-SNP_CeD_x3_filtered"

# Make consensus peaks
CMD="${IPCR_TOOLS} -T OverlapPeaks \
${PEAKS} \
-o ${OUTPUT}.consensus.peaks"

eval $CMD



IPCR=""

for cur_ipcr in ../../pipeline/2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/*.ipcr.bgz;
do
IPCR="${IPCR} -ipcr ${cur_ipcr}"
done;


# Make count matrix
CMD="${IPCR_TOOLS} -T GetPeakCounts \
-i ${OUTPUT}.consensus.peaks.narrowPeak \
${IPCR} \
-o ${OUTPUT}"

eval $CMD


CMD="${IPCR_TOOLS} -T GetPeakCounts \
-i data/peaks/SuRE_CeD_caco2_iFNY_de_genes_5kb_window.bed \
${IPCR} \
-o SuRE_CeD_caco2_iFNY_de_genes_5kb_window"

#eval $CMD

CMD="${IPCR_TOOLS} -T GetPeakCounts \
-i data/peaks/SuRE_CeD_jurkat_acd3acd28_de_genes_5kb_window.bed \
${IPCR} \
-o SuRE_CeD_jurkat_acd3acd28_de_genes_5kb_window"

#eval $CMD
