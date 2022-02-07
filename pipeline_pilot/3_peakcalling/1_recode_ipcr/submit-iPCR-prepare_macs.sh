#!/bin/bash

mkdir -p logs

while read SAMPLES;
do

echo $SAMPLES

SAMPLES_NAME=""

for sample in ${SAMPLES};
do
SAMPLES_NAME="${SAMPLES_NAME}+${sample}"
done
SAMPLES_NAME="${SAMPLES_NAME:1}"


sbatch --out=logs/${SAMPLES_NAME}.out --err=logs/${SAMPLES_NAME}.err iPCR-prepare_macs.sh "${SAMPLES}"
#bash iPCR-prepare_macs.sh "${SAMPLES}"

done < combos_jurkat.txt
