#!/bin/bash
#SBATCH --time=02:00:00
#SBATCH --mem=2G
#SBATCH --nodes=1
#SBATCH --ntasks=1

# Loading required software
#ml pigz
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
# Plasmid BC (UMI)	Avr2            I-ceul		    Xcm1      Adapter stuff   SampIdx   Adapter	   Fragment
#                      CCTAGC TAACTATAACGGTCCTAAGGTAGCGAA CCAGTGAT NNNNNNNNNNNNNNNNNN NNNNNN AGCGTACCGTAGT
# NCCCGGAGCGGAATAAGCGA CCTAGC TAACTATAACGGTCCTAAGGTAGCGAA CCAGTGAT CGCATCGTCGGCAGCGTC GAACCA AGCGTACCGTAGT GTACTCTAGCTATGTAGGACTGGAGCGCTGTATCAGGGAAAGCTGATAGAGC

# Reverse adapter
# Reverse 5' > 3'
# Xcm1	   adapter stuff      SampIdx adapter               Fragment
# CCAGTCGT NNNNNNNNNNNNNNNNNN NNNNNN AGCGTACCGTAGT 
# NCAGTCGT CGCATCGTCGGCAGCGTC GGCTAC AGCGTACCGTAGT TTAGAGGCAAATGTTGCTATTTCAAATTCCTGGATTGAAGTTAGAGTGAATGGTNCCAAACTAAAATAGTGCTGATCGCTTGAACTAATATAATGTATTAGTTAT

ADPTR_FORW_SEQ="CCTAGCTAACTATAACGGTCCTAAGGTAGCGAACCAGTGATNNNNNNNNNNNNNNNNNNNNNNNNAGCGTACCGTAGT"
ADPTR_REV_SEQ="^CCAGTCGTNNNNNNNNNNNNNNNNNNNNNNNNAGCGTACCGTAGT"

# If any command fails, fail the entire script
set -e

# Log the starttime
starttime=$(date +%s)

#-------------------------------------------------------------------------------#
#                              Main program loop                                #
#-------------------------------------------------------------------------------#
# Perfrom some rudementary QC
# Trim 3' end based on quality of at least 25
# Allow a maximum of 4 N bases in the read
# Have a minimum lenghth of 25 for forward and 50 for reverse reads
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting filtering of trimmed reads in paired end mode"

CMD="${CUTADAPT} \
-g $ADPTR_FORW_SEQ \
-G $ADPTR_REV_SEQ \
-q 25 \
-m 25:50 \
-e 0.05 \
--max-n 4 \
--discard-untrimmed \
--action=none \
-o ${OUTDIR}/${BASENAME}_forw.tmp.fq.gz \
-p ${OUTDIR}/${BASENAME}_rev.tmp.fq.gz \
${READS_FORW} \
${READS_REV}"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] cutadapt command = ${CMD}"
eval "${CMD}"

# Cutting of adapters was split since the writing of an info file is not
# supported for paired end mode. We need the info to get the barcode. 
# Trim forward reads
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Removving adapter from forward reads"

CMD="${CUTADAPT} \
-g $ADPTR_FORW_SEQ \
-o ${OUTDIR}/${BASENAME}_forw.fq.gz \
-e 0.05 \
--info-file=${OUTDIR}/${BASENAME}.info \
${OUTDIR}/${BASENAME}_forw.tmp.fq.gz"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] cutadapt command = ${CMD}"
eval "${CMD}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done with forward reads"
gzip ${OUTDIR}/${BASENAME}.info
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Gzipped barcode fragment linker"

# Trim reverse reads
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Removing adapter from reverse reads"

CMD="${CUTADAPT} \
-g $ADPTR_REV_SEQ \
-o ${OUTDIR}/${BASENAME}_rev.fq.gz \
${OUTDIR}/${BASENAME}_rev.tmp.fq.gz"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] cutadapt command = ${CMD}"
eval "${CMD}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done with reverse reads"

# Cleanup
rm ${OUTDIR}/${BASENAME}_forw.tmp.fq.gz ${OUTDIR}/${BASENAME}_rev.tmp.fq.gz

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done"
