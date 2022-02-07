#!/bin/bash




for file in output/G* output/T*;
do

sample=$(basename $file)

sbatch --out="logs/${sample}-counting-%j.out" --err="logs/${sample}-counting-%j.err" cDNA-make_barcode_counts.sh output/${sample} output/${sample}/${sample}


#bash cDNA-make_barcode_counts.sh output/${sample} output/${sample}/${sample}


done

