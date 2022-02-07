#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=53G
#SBATCH --ntasks=1

ml Java

SAMPLE=$1


java -Xms40G -Xmx50G \
-jar ../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T MakeBarcodeComplexityCurve \
-b ../../pipeline/1_cDNA/1_make_bacode_counts/output_full_experiment/${SAMPLE}/${SAMPLE}.barcodes.gz \
-o output/${SAMPLE} \
--simple-reader
