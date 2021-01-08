#!/bin/bash
#SBATCH --time=23:59:00
#SBATCH --mem=64G
#SBATCH --ntasks=22

ml pigz
ml Python/3.6.3-foss-2015b

set -e

# IO
INPUT=$1
OUTPUT=$2

# Use the latest cutadapt
CUTADAPT=~/.local/bin/cutadapt

base="$(basename $INPUT)"
echo "[INFO] Proccesing $base $INPUT"
	
CMD="${CUTADAPT} \
--no-indels \
-j 22 \
-g CCTAGCTAACTATAACGGTCCTAAGGTAGCGAA \
-o ${OUTPUT}/${base} \
--info-file ${OUTPUT}/${base}.info \
${INPUT}"

eval $CMD

gzip ${OUTPUT}/${base}.info






# OLD
#gunzip -c output/run_3_v2/${base} | awk '{ if(NR%4==2){ print $0} }' > output/run_3_v2/${base}.barcodes
#cutadapt -g CCTAGCTAACTATAACGGTCCTAAGGTAGCGAA  -o output_v3/${base} --info-file output_v3/${base}.info $file
#for file in /groups/umcg-wijmenga/tmp04/umcg-obbakker/data/CeD_sure_snp/cDNA/jurkat/*.fastq.gz;
#cutadapt -l 20 -o output/run_3/${base} $file

