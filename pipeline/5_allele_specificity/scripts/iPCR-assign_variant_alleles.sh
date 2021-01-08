#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G

ml Java

SAMPLE=$1


# Define the multiple input files
#P_BAM=""
#S_BAM=""
#IPCR=""

#while read SAMPLE;
#do

#P_BAM="$P_BAM -i ../4_variant_calling/3_gatk_haplotype_caller/output/${SAMPLE}.raw.gatk.bam"
#S_BAM="$S_BAM -j ../4_variant_calling/1_mark_duplicates/output/${SAMPLE}.dedup.bam"
#IPCR="$IPCR -b ../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed/${SAMPLE}.ipcr.bgz"


#done < ../0_data/samples.txt
#done < ../0_data/samples_test.txt


# No secondary BAM, use filtered alignment info
CMD="java -Xmx16G -jar ../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T AssignVariantAlleles \
-i  ../4_variant_calling/1_mark_duplicates/output/${SAMPLE}.dedup.bam \
-b ../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/${SAMPLE}.ipcr.bgz \
-g ../4_variant_calling/5_gatk_hard_filters/output/SuRE_CeD.filtered.snps.indels.sorted.vcf.gz \
-t MINIMAL \
-o output/snps_indels/${SAMPLE} \
-z 
-v ${SAMPLE}"

eval ${CMD}

# Make coverage track of reads overlapping a SNP
CMD="java -Xmx16G -jar ../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T AssignVariantAlleles \
-g ../4_variant_calling/5_gatk_hard_filters/output/SuRE_CeD.filtered.snps.vcf.gz \
-t BEDGRAPH \
-o output/SuRE_CeD_test \
-z \
${P_BAM} \
${S_BAM} \
${IPCR} \
-vf rs140496 \
-c C_B1_T1 \
-c C_B1_T2 \
-c C_B2_1 \
-c C_B2_T2"

#-c K_B1_T1 K_B1_T2 K_B2_T1 K_B2_T2"
#eval $CMD


# Select all the reads in the iPCR file from a BAM file
CMD="java -Xmx62G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T SubsetBam \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_recode/${SAMPLE}.ipcr.bgz \
-b ../../1_alignment/4_merge_bam/output/${SAMPLE}_merged.querysorted.bam \
-s \
-o input/${SAMPLE}"

#eval $CMD


# Make a bam of reads overlapping SNP
CMD="java -Xmx24G -jar ../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T AssignVariantAlleles \
-i ../4_variant_calling/3_gatk_haplotype_caller/output/${SAMPLE}.raw.gatk.bam \
-j ../4_variant_calling/1_mark_duplicates/output/${SAMPLE}.dedup.bam \
-g ../4_variant_calling/5_gatk_hard_filters/output/SuRE_CeD.filtered.snps.vcf.gz \
-b ../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed/${SAMPLE}.ipcr.bgz \
-t FULL \
-z \
-v ${SAMPLE} \
-o output/rs140496/${SAMPLE} \
-vf rs140496 \
--bam-out"

#eval $CMD


# Make a bam of reads overlapping SNP without gatk bam
CMD="java -Xmx24G -jar ../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T AssignVariantAlleles \
-i ../4_variant_calling/1_mark_duplicates/output/${SAMPLE}.dedup.bam \
-g ../4_variant_calling/5_gatk_hard_filters/output/SuRE_CeD.filtered.snps.vcf.gz \
-b ../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v2/${SAMPLE}.ipcr.bgz \
-t FULL \
-z \
-v ${SAMPLE} \
-o output/rs9259131/${SAMPLE} \
-vf rs9259131 \
--bam-out"

#eval $CMD

