#!/bin/bash
#SBATCH --mem=64G
#SBATCH --time=05:59:00

set -e

ml BEDTools

bedtools bamtobed -i ../4_merge_bam/output/${1}.querysorted.bam > output/${1}.bed

bedtools intersect -a input/windows_nochr.bed -b output/${1}.bed -c > output/${1}.peaks

bedtools intersect -a ../../0_data/CeDRegionV3.bed -b output/${1}.bed -c > output/${1}.ced.peaks


gzip output/*.bed output/*.peaks
