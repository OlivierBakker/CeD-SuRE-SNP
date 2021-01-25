#!/bin/bash


ml liftOverUcsc/20161011


URL=$1
NAME=$2

set -e

OUTPUT="data/bundle/ccre"
#URL=http://gcp.wenglab.org/Seven-Group/ENCFF413MAJ_ENCFF529EAW_ENCFF560IHK.7group.bed
#NAME=caco2

mkdir -p ${OUTPUT}

# Download the ccRE data
wget ${URL} -O ${OUTPUT}/${NAME}.cCRE.hg38.bed

# Liftover to hg19
liftOver -bedPlus=8 ${OUTPUT}/${NAME}.cCRE.hg38.bed data/hg38ToHg19.over.chain.gz ${OUTPUT}/${NAME}.cCRE.hg19.bed ${OUTPUT}/${NAME}.cCRE.hg19.unlifted

# Format for sure SNP excel generator
echo -e "chr	start	end	cCRE	cCRE_cellType" > ${OUTPUT}/${NAME}.cCRE.sure.annot

grep -v "Unclassified"  ${OUTPUT}/${NAME}.cCRE.hg19.bed | \
awk -v celltype="${NAME}" -F $'\t' 'BEGIN {OFS=FS}{print $1,$2,$3,$10,celltype}' | \
sed 's/chr//g' | \
sort -k1,1 -k2,2n -k3,3n >> ${OUTPUT}/${NAME}.cCRE.sure.annot

# Zip
gzip -f ${OUTPUT}/${NAME}.cCRE.hg19.unlifted  ${OUTPUT}/${NAME}.cCRE.hg19.bed

# Zip and index main file
bgzip -c  ${OUTPUT}/${NAME}.cCRE.sure.annot >  ${OUTPUT}/${NAME}.cCRE.sure.annot.bgz
tabix -s 1 -b 2 -e 3 --skip-lines 1 ${OUTPUT}/${NAME}.cCRE.sure.annot.bgz

# Cleanup
rm ${OUTPUT}/${NAME}.cCRE.hg38.bed  ${OUTPUT}/${NAME}.cCRE.sure.annot
