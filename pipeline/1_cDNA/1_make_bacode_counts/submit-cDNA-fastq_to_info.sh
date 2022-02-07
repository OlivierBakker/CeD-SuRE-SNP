#!/bin/bash


set -e

# IO
#INPUT_FILES=/groups/umcg-wijmenga/tmp04/umcg-obbakker/data/CeD_sure_snp/cDNA/jurkat/run_3/191126_NB501840_0247_AHK3J7BGXC/SuRESNP_Jurkat2_Nov2019/*.fastq.gz
#INPUT_FILES="/groups/umcg-wijmenga/tmp04/umcg-obbakker/data/CeD_sure_snp/cDNA/k562/200205_NB501840_0262_AH5FLYBGXF/Pool_K562_SuReSNP_4Feb20/*.fastq.gz"
#INPUT_FILES="../../../../../data/CeD_sure_snp/cDNA/tmp_new_data/X201SC20030609-Z01-F001/raw_data/*/*.fq.gz"
INPUT_FILES="input/gm_thp/*/*.fq.gz"


mkdir -p logs

for file in $INPUT_FILES;
do

base=$(basename $file | sed 's/\(.*\_B.\_T.\).*/\1/g')
OUTPUT=output/${base}/

echo $base
echo $OUTPUT

mkdir -p ${OUTPUT}
sbatch --out="logs/${base}-%j.out" --err="logs/${base}-%j.err" cDNA-fastq_to_info.sh $file $OUTPUT
#echo "bash cDNA-fastq_to_info.sh $file $OUTPUT"

done


