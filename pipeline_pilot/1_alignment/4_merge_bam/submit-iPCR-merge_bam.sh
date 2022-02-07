#!/bin/bash

set -e

sample=$1


# Merge all the BAMs for 1 sample
mkdir -p output
mkdir -p logs

bash iPCR-merge_bam.sh \
-o output/ \
-b ${sample}_merged \
-i input/${sample}.toMerge




