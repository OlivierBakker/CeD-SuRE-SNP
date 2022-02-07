#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=16G

set -e

ml Python/3.6.3-foss-2015b
source macs2_venv/bin/activate


OUTDIR1=output/narrow/ced_regions_strand_specific
OUTDIR2=output/narrow/ced_regions_strand_specific

mkdir -p $OUTDIR1 $OUTDIR2

# ced regions
CMD1="macs2 callpeak \
--treatment ../1_recode_ipcr/output/ced_regions_strand_specific/${1}.plus.cdna.bed.gz \
--control ../1_recode_ipcr/output/ced_regions_strand_specific/${1}.plus.ipcr.bed.gz \
--name CeD_filtered500_plus${1} \
--outdir ${OUTDIR1} \
--format BEDPE \
--keep-dup all \
--nomodel \
--bdg \
--SPMR \
-g 14e6"


# ced regions
CMD2="macs2 callpeak \
--treatment ../1_recode_ipcr/output/ced_regions_strand_specific/${1}.minus.cdna.bed.gz \
--control ../1_recode_ipcr/output/ced_regions_strand_specific/${1}.minus.ipcr.bed.gz \
--name CeD_filtered500_minus${1} \
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
