#!/bin/bash

set -e

rm input/*.toMerge

# Generate a text file with all the paths to BAMS to merge
while read lane;
do
while read sample;
do

echo "../3_bwa_mem/output/$(basename $lane)/SuRE_CeD_${sample}.bam" >> input/$sample.toMerge

done < ../../0_data/samples_full.txt
done < input/input_files.txt


# Merge all the BAMs for 1 sample
mkdir -p output
mkdir -p logs

while read sample;
do

sbatch --out logs/%j.out --err logs/%j.err \
iPCR-merge_bam.sh \
-o output/ \
-b ${sample}_merged \
-i input/${sample}.toMerge


done < ../../0_data/samples_full.txt


