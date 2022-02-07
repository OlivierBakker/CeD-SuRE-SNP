#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=4G
#SBATCH --nodes=1
#SBATCH --ntasks=1

# Loading required software
ml pigz
ml Python/3.6.3-foss-2015b

#-------------------------------------------------------------------------------#
#                            Script global Settings                             #
#-------------------------------------------------------------------------------#
SCRIPTNAME=iPCR-trim_adapters.sh
VERSION=0.0.2

# Tools
CUTADAPT=~/.local/bin/cutadapt

# IO variable defaults (for testing)
OUTDIR=./output
BASENAME="SuRE53"
READS_FORW="input/SuRE53_1_FKDL190727815-1a-2_H22WGCCX2_L2_1.fq.gz"
READS_REV="input/SuRE53_1_FKDL190727815-1a-2_H22WGCCX2_L2_2.fq.gz"

READS_FORW=$1
READS_REV=$2
OUTDIR=$3
BASENAME=$4

# Global variable defaults (are overidden if options provided)

## Description of adapter seqeunces
# Forward adapter sqeunce
# Forward 5' > 3'
# Plasmid BC (UMI)	Avr2            I-ceul		    Xcm1   Fragment
#                      CCTAGC TAACTATAACGGTCCTAAGGTAGCGAA CCAGTGAT
# NCCCGGAGCGGAATAAGCGA CCTAGC TAACTATAACGGTCCTAAGGTAGCGAA CCAGTGAT GTACTCTAGCTATGTAGGACTGGAGCGCTGTATCAGGGAAAGCTGATAGAGC

# Reverse adapter
# Reverse 5' > 3'
# Xcm1	   Fragment
# CCAGTCGT
# NCAGTCGT TTAGAGGCAAATGTTGCTATTTCAAATTCCTGGATTGAAGTTAGAGTGAATGGTNCCAAACTAAAATAGTGCTGATCGCTTGAACTAATATAATGTATTAGTTAT

ADPTR_FORW_SEQ="CCTAGCTAACTATAACGGTCCTAAGGTAGCGAACCAGTGAT"
ADPTR_REV_SEQ="^CCAGTCGT"

# If any command fails, fail the entire script
set -e

# Log the starttime
starttime=$(date +%s)

#-------------------------------------------------------------------------------#
#                              Main program loop                                #
#-------------------------------------------------------------------------------#
# Perfrom some rudementary QC
# Trim 3' end based on quality of at least 25
# Allow a maximum of 5 N bases in the read
# Have a minimum lenghth of 20 for forward and 50 for reverse reads
# Do NOT allow indels in the adapter seqeunce
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting filtering of trimmed reads in paired end mode"

CMD="${CUTADAPT} \
-g $ADPTR_FORW_SEQ \
-G $ADPTR_REV_SEQ \
-q 25 \
-m 20:50 \
-e 0.075 \
--max-n 5 \
--no-indels \
--discard-untrimmed \
--info-file ${OUTDIR}/${BASENAME}.info \
-o ${OUTDIR}/${BASENAME}_forw.fq.gz \
-p ${OUTDIR}/${BASENAME}_rev.fq.gz \
${READS_FORW} \
${READS_REV}"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] cutadapt command = ${CMD}"
eval "${CMD}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done with reverse reads"

gzip ${OUTDIR}/${BASENAME}.info

# Cleanup
#rm ${OUTDIR}/${BASENAME}_forw.tmp.fq.gz ${OUTDIR}/${BASENAME}_rev.tmp.fq.gz

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done"
