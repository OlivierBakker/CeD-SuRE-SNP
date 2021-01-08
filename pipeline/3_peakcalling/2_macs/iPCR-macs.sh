#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G

set -e

ml Python/3.6.3-foss-2015b
source macs2_venv/bin/activate


OUTDIR1=output/narrow/genome_wide
OUTDIR2=output/narrow/ced_regions

mkdir -p $OUTDIR1 $OUTDIR2

# genome wide
CMD1="macs2 callpeak \
--treatment ../1_recode_ipcr/output/genome_wide_regions/${1}.cdna.bed.gz \
--control ../1_recode_ipcr/output/genome_wide_regions/${1}.ipcr.bed.gz \
--name WG_filtered500_${1} \
--outdir ${OUTDIR1} \
--format BEDPE \
--keep-dup all \
--nomodel \
--bdg \
--SPMR \
-g hs"

# ced regions
CMD2="macs2 callpeak \
--treatment ../1_recode_ipcr/output/ced_regions/${1}.cdna.bed.gz \
--control ../1_recode_ipcr/output/ced_regions/${1}.ipcr.bed.gz \
--name CeD_filtered500_${1} \
--outdir ${OUTDIR2} \
--format BEDPE \
--keep-dup all \
--nomodel \
--bdg \
--SPMR \
-g 14e6"


echo $CMD1
eval $CMD1


echo $CMD2
eval $CMD2


# gzip change outdir accordingly

gzip ${OUTDIR2}/*${1}_treat_pileup.bdg ${OUTDIR2}/*${1}_control_lambda.bdg
gzip ${OUTDIR1}/WG_filtered500_${1}_treat_pileup.bdg ${OUTDIR1}/WG_filtered500_${1}_control_lambda.bdg
