#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G

ml Java

SAMPLE=$1

VARIANT="rs9259131"
REGION="6	29841800	29861800"


mkdir -p output/${VARIANT}
echo -e "${REGION}" > output/${VARIANT}/region.bed 

# Make a bam of reads overlapping SNP without gatk bam
CMD="java -Xmx24G -jar ../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T AssignVariantAlleles \
-i ../4_variant_calling/1_mark_duplicates/output/${SAMPLE}.dedup.bam \
-g ../4_variant_calling/5_gatk_hard_filters/output/SuRE_CeD.filtered.snps.vcf.gz \
-b ../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v2/${SAMPLE}.ipcr.bgz \
-t FULL \
-z \
-v ${SAMPLE} \
-o output/${VARIANT}/${SAMPLE} \
-vf ${VARIANT} \
--bam-out"

eval $CMD


# Make a bam of reads overlapping SNP without gatk bam
CMD="java -Xmx24G -jar ../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T Recode \
-i ../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v2/${SAMPLE}.ipcr.bgz \
-k IPCR_INDEXED \
-t IPCR_INDEXED \
-z \
-o output/${VARIANT}/${SAMPLE} \
-rf output/${VARIANT}/region.bed"

eval $CMD



