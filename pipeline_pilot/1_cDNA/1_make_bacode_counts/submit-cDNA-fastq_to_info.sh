#!/bin/bash


set -e

# IO
INPUT_FILES="input/pilot_original/K*.fq.gz"


mkdir -p logs

for file in $INPUT_FILES;
do

base=$(basename $file | sed 's/\(.*\_B.\_T.\).*/\1/g')
OUTPUT=output/${base}/

echo $base
echo $OUTPUT

mkdir -p ${OUTPUT}
#sbatch --out="logs/${base}-%j.out" --err="logs/${base}-%j.err" cDNA-fastq_to_info.sh $file $OUTPUT
bash cDNA-fastq_to_info.sh $file $OUTPUT

done


