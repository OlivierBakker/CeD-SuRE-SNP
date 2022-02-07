#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G

ml Java



# No secondary BAM, use filtered alignment info
CMD="java -Xmx16G -jar ../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T AssignVariantAlleles \
-i input/sure_pilot_B_merged.sorted.bam \
-b ../2_make_ipcr_files/1_merge_with_barcodes/output/collapsed/sure_pilot_B.ipcr.bgz \
-g ../0_data/1kg_v3_EUR_sure_pilot_snps.vcf.gz \
-t MINIMAL \
-o output/sure_pilot_B \
-z "

eval ${CMD}
