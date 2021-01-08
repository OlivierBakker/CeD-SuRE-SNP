#!/bin/bash




for file in output/J_* output/Js_*;
do

sample=$(basename $file)


sbatch --out="logs/${sample}-counting-%j.out" --err="logs/${sample}-counting-%j.err" cDNA-make_barcode_counts.sh output/${sample} output/${sample}/${sample}


#bash cDNA-make_barcode_counts.sh output/${sample} output/${sample}/${sample}


done

