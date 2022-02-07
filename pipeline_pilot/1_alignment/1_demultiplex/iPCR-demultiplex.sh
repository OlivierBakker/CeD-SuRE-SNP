#!/bin/bash
#SBATCH --time=23:59:00
#SBATCH --mem=4G
#SABTCH --nodes=1
#SBATCH --ntasks=1

# Loading required software
ml pigz
ml Python/3.6.3-foss-2015b

#-------------------------------------------------------------------------------#
#                            Script global Settings                             #
#-------------------------------------------------------------------------------#
SCRIPTNAME=iPCR-demultiplex.sh
VERSION=0.0.1

# Tools
CUTADAPT=~/.local/bin/cutadapt

# IO variable defaults (for testing)
OUTDIR=./output

# Basename for output
BASENAME="SuRE_CeD"

# Fasta formatted CutAdapt compatible wildcards for sample indeces
SAMP_IDX="sample_indexes.fasta"

# Input prefix for fastq files
INPUT_PREFIX=$1
READS_FORW="${INPUT_PREFIX}_1.fq.gz"
READS_REV="${INPUT_PREFIX}_2.fq.gz"

OUTDIR="${OUTDIR}/$(basename ${INPUT_PREFIX})"

# If any command fails, fail the entire script
set -e

# Log the starttime
starttime=$(date +%s)

#-------------------------------------------------------------------------------#
#                              Main program loop                                #
#-------------------------------------------------------------------------------#
# Demultiplex in paired end mode 
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Demultiplexing on exact matches"

# Match read wildcards allows N in the read to match
# action=none will not remove the adapter from the read
# Allows for 1 mismatch in the index sequence

mkdir -p ${OUTDIR}

CMD="${CUTADAPT} \
-g file:${SAMP_IDX} \
-o ${OUTDIR}/${BASENAME}_{name}_forw.fq.gz \
-p ${OUTDIR}/${BASENAME}_{name}_rev.fq.gz \
-e 0.2 \
--match-read-wildcards \
--action=none \
--no-indels \
${READS_FORW} \
${READS_REV}"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] cutadapt command = ${CMD}"
eval "${CMD}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done with demultiplexing"

