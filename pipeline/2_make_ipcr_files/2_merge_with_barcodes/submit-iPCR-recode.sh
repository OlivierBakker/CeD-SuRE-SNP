#!/bin/bash


while read sample;
do

#sbatch run_ipcrtools.sh $sample
sbatch --out="logs/${sample}-recode-%j.out" --err="logs/${sample}-recode-%j.err" iPCR-recode.sh $sample

done < ../../0_data/samples.txt
