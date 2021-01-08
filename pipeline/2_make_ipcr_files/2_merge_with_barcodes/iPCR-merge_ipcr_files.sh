#!bin/bash
# Merge gzipped iPCR files

set -e

OUTPUT="SuRE_CeD_high_alignment_samples.ipcr"
COUNTER=0

while read sample;
do

if [ $COUNTER -eq 0 ]; then
	zcat output/${sample}.ipcr.gz | head -n 1 > $OUTPUT 
fi

echo "$sample"
zcat output/${sample}.ipcr.gz | tail -n +2 >> $OUTPUT


COUNTER=$((COUNTER + 1))

done < cursamples.txt

gzip $OUTPUT
