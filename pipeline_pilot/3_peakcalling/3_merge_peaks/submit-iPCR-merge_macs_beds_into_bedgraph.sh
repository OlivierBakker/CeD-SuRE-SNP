#!/bin/bash


PREFIX=$1

while read combo;
do

OUT=$(echo $combo | awk '{print $1}')
F1=$(echo $combo | awk '{print $2}')
F2=$(echo $combo | awk '{print $3}')

CMD="bash iPCR-merge_macs_beds_into_bedgraph.sh \
../1_recode_ipcr/output/ced_regions_strand_specific/${F1}${PREFIX}.cdna.bed.gz \
../1_recode_ipcr/output/ced_regions_strand_specific/${F2}${PREFIX}.cdna.bed.gz \
output/ced_regions_strand_specific/${OUT}_ced_regions_CPM_all_samples${PREFIX}"


echo $CMD
eval $CMD
echo "-------------------------------------------------"

done < combos.txt
