#!/bin/bash
#SBATCH --time=23:59:00
#SBATCH --mem=200G



ml Java

# IPCR
#java -Xms40G -Xmx195G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar -T GenerateBarcodeComplexityCurve -b ../6_merge_with_barcodes/SuRE_all.info.gz -o bla4 -m 100000000



# cDNA
#62741292
#62700000
java -Xms40G -Xmx195G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar -T GenerateBarcodeComplexityCurve -b ../../../../cDNA_prep/2_splitFastq/output/J_B1.barcodes.gz -o cdna/SuRE_cdna_J_B1 -m 62741292 -s



