#!/bin/bash

set -e

while read sample;
do

echo "[INFO] Proccessing $sample"

cat ../../1_alignment/2_trim_adapters/output/*/SuRE_CeD_${sample}.info.gz > output/${sample}.info.gz

done < ../../0_data/samples.txt
