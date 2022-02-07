#!/bin/bash

set -e


while read lane;
do

while read sample;
do

echo "[INFO] submitting $sample"
mkdir -p output/$(basename $lane)


#echo "" > $(pwd)/logs/$(basename lane)_${sample}.out
#echo "" > $(pwd)/logs/$(basename lane)_${sample}.err

sbatch --output="logs/$(basename $lane)_${sample}.out" --error="logs/$(basename $lane)_${sample}.err" iPCR-trim_adapters.sh \
${lane}/SuRE_CeD_${sample}_forw.fq.gz \
${lane}/SuRE_CeD_${sample}_rev.fq.gz \
output/$(basename $lane) \
SuRE_CeD_${sample}

done < input/samples.txt
done < input/input_files.txt
