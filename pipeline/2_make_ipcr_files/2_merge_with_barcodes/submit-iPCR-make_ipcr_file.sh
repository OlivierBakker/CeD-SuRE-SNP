#!/bin/bash


while read sample;
do

sbatch --out="logs/${sample}-make_ipcr_file-%j.out" --err="logs/${sample}-make_ipcr_file-%j.err" iPCR-make_ipcr_file.sh $sample

done < ../../0_data/samples.txt
