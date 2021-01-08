#!/bin/bash


set -e

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


sbatch --out=logs/${SAMPLES_NAME}.out --err=logs/${SAMPLES_NAME}.err iPCR-macs.sh "${SAMPLES_NAME}"


done < combos_jurkat.txt

