#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G

ml BEDTools

IN_1=$1
IN_2=$2
OUTPUT=$3

set -e
zcat ${IN_1} ${IN_2} | sort -k 1,1 > ${OUTPUT}.tmp.bed

TOTAL="$(wc -l ${OUTPUT}.tmp.bed | awk '{print $1}')"
echo "[INFO] Total count ${TOTAL}"

SCALE=$(awk -v total="${TOTAL}" 'BEGIN{print 1000000/total}')

echo "[INFO] Scaling by ${SCALE}"

bedtools genomecov \
-g /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta.fai \
-i ${OUTPUT}.tmp.bed \
-scale ${SCALE} \
-bg > ${OUTPUT}.bdg

rm ${OUTPUT}.tmp.bed
gzip ${OUTPUT}.bdg
