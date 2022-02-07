#!/bin/bash


# CeD regions
PREFIX="../2_macs/output/narrow/"

while read curcombo;
do

INPUTS=""
OUTPUT="$(echo $curcombo | awk '{print $1}')_x3_ced_regions_bmerged"

for combo in $(echo $curcombo | awk '{print $2"\t"$3}');
do
  	INPUTS="${INPUTS} -i ${PREFIX}${combo}_peaks.narrowPeak"
done


CMD="java -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T OverlapPeaks \
${INPUTS} \
-o output/ced_regions/${OUTPUT} \
--score-filter 3 \
--discard-unique \
--trim-pattern CeD_filtered500_"

echo $CMD
eval $CMD

echo "----------------------------------------------------------------------"

done < combos.txt


#-------------------------------------------------------------------------#
# Genome wide
PREFIX="../2_macs/output/narrow/genome_wide/WG_filtered500_"

while read curcombo;
do

INPUTS=""
OUTPUT="$(echo $curcombo | awk '{print $1}')_x3_genome_wide_bmerged"

for combo in $(echo $curcombo | awk '{print $2"\t"$3}');
do
	INPUTS="${INPUTS} -i ${PREFIX}${combo}_peaks.narrowPeak"
done


CMD="java -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T OverlapPeaks \
${INPUTS} \
-o output/genome_wide/${OUTPUT} \
--score-filter 3 \
--discard-unique \
--trim-pattern WG_filtered500_"

echo $CMD
eval $CMD

echo "----------------------------------------------------------------------" 

done < combos.txt
