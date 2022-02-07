#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G
#SBATCH --cpus-per-task=1


CT=$1
BG=$2
OUTPUT="output/$CT"


CMD="findMotifsGenome.pl \
data/${CT}_x3_ced_region_bmerged.sure.annot.bed \
hg19"


if [ "${BG}" != "" ]; then

TMP_BG="$(echo ${BG}| sed 's/.bed//g')"
TMP_BG="$(echo ${TMP_BG}| sed 's/_x3_ced_region_bmerged.sure.annot//g')"


CMD="${CMD} ${OUTPUT}_$(basename ${TMP_BG}) -bg ${BG}"
else
CMD="${CMD} ${OUTPUT}_hg19"
fi

CMD="${CMD} \
-size 200"

echo $CMD
eval $CMD
