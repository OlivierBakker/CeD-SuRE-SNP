#!/bin/bash


while read sample;
do

sbatch CatReads.sh $sample

done < ../../0_data/samples.txt
