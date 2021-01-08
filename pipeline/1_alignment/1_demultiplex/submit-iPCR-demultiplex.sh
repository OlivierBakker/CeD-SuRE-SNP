#!/bin/bash


mkdir -p logs

while read line;
do

echo $line
sbatch --out=logs/%j.out --err=logs/%j.err iPCR-demultiplex.sh $line

done < input/input_files.txt
