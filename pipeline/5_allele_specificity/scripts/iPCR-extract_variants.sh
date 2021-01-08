#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G

ml Java

VARIANTS_FILE=$1
OUTPUT=$2

# Define the multiple input files
P_BAM=""
S_BAM=""
IPCR=""

while read SAMPLE;
do

P_BAM="$P_BAM -i ../../4_variant_calling/3_gatk_haplotype_caller/output/${SAMPLE}.raw.gatk.bam"
S_BAM="$S_BAM -i ../../4_variant_calling/1_mark_duplicates/output/${SAMPLE}.dedup.bam"
IPCR="$IPCR -b ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/${SAMPLE}.ipcr.bgz"

done < ../../0_data/samples.txt


# Specify multiple variants
VARIANTS=""

while read VARIANT;
do
VARIANTS="${VARIANTS} -vf ${VARIANT}"

done < ${VARIANTS_FILE}

echo $VARIANTS

#-j ../4_variant_calling/1_mark_duplicates/output/${SAMPLE}.dedup.bam \

# Make a bam of reads overlapping a variant
CMD="java -Xmx24G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T AssignVariantAlleles \
${S_BAM} \
${IPCR} \
${VARIANTS} \
-g ../../4_variant_calling/5_gatk_hard_filters/output/SuRE_CeD.filtered.snps.indels.sorted.vcf.gz \
-t FULL \
-z \
-o ../output/variant_extractions/${OUTPUT} \
--bam-out"

eval $CMD
