#!/bin/bash


mkdir -p logs
mkdir -p output

while read sample;
do

CMD="sbatch --output='logs/${sample}.out' --error='logs/${sample}.err' iPCR_subset_bam.sh $sample"
eval $CMD

#echo $CMD

done < ../../0_data/samples.txt


