#!/bin/bash
#SBATCH --time=00:05:00
#SBATCH --mem=4G



FILE=$1
NAME="$(basename $FILE)"
NAME="$(echo $NAME | sed 's/\.tsv\.gz//g')"

zcat ${FILE} | awk '{if ($6 > 418) {print $1"\t"$2"\t"$3"\t"$4"\t"$5"\t"$6"\t"$7}}' | sed 's/chr//g' > data/bundle/JASPAR/hg19_filtered/${NAME}.tmp

