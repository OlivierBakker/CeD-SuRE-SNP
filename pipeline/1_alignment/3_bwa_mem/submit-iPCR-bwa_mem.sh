#!/bin/bash

set -e

while read lane;
do

while read sample;
do

echo "[INFO] submitting $sample"
mkdir -p output/$(basename $lane)

# Define the read group header for the BAM file
LANE_INFO="$(basename $lane | sed 's/_/\t/g')"

# Individual components
LANE="$(echo $LANE_INFO | awk '{print $5}')"
SAMPLE="$sample"
LIBRARY="$(echo $LANE_INFO | awk '{print $2}')"
PLATFORM="ILLUMINA"
FLOWCELL="$(echo $LANE_INFO | awk '{print $4}')"


# Paste everything into the required components, for more info see
# https://gatkforums.broadinstitute.org/gatk/discussion/6472/read-groups
# ID and PU are the same here, this is since ID needs to be unique
# And we are running per lane per sample for mapping
# These read groups are really important for the genotyping step later on
# since they are used to indentify potential biases based on the 
# lane, flowcell etc
ID="${FLOWCELL}.${LANE}.${SAMPLE}"
PU="${FLOWCELL}.${LANE}"
SM="${SAMPLE}"
PL="${PLATFORM}"
LB="${LIBRARY}"

READ_GROUP='"@RG\tID:${ID}\tPU:${PU}\tSM:${SM}\tPL:${PL}\tLB:${LB}"'

CMD="sbatch --output='logs/$(basename $lane)_${sample}.out' --error='logs/$(basename $lane)_${sample}.err' iPCR-bwa_mem.sh \
-o ./output/$(basename $lane) \
-i /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/BWA/0.7.12-goolf-1.7.20/human_g1k_v37.fasta \
-f ../2_trim_adapters/output/$(basename $lane)/SuRE_CeD_${sample}_forw.fq.gz \
-r ../2_trim_adapters/output/$(basename $lane)/SuRE_CeD_${sample}_rev.fq.gz \
-g ${READ_GROUP} \
-n 4"

eval $CMD

done < input/samples.txt
done < input/input_files.txt
