#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G

set -e

ml Python/3.6.3-foss-2015b
source ../../../pipeline/3_peakcalling/2_macs/macs2_venv/bin/activate

OUTDIR1=output/narrow/

mkdir -p $OUTDIR1

# genome wide
CMD1="macs2 callpeak \
--treatment ../1_recode_ipcr/output/${1}.cdna.bed.gz \
--control ../1_recode_ipcr/output/${1}.ipcr.bed.gz \
--name SuRE_CeD_${1} \
--outdir ${OUTDIR1} \
--format BEDPE \
--keep-dup all \
--nomodel \
--bdg \
--SPMR \
-g hs"


echo $CMD1
eval $CMD1


# gzip change outdir accordingly
gzip ${OUTDIR1}/SuRE_CeD_${1}_treat_pileup.bdg ${OUTDIR1}/SuRE_CeD_${1}_control_lambda.bdg
