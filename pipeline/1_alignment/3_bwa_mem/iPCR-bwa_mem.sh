#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=12G
#SBATCH --cpus-per-task=4
#SBATCH --nodes=1

ml SAMtools
ml BWA

#-------------------------------------------------------------------------------#
#                            Script global Settings                             #
#-------------------------------------------------------------------------------#
# Version info
SCRIPTNAME=iPCR-BWAmem.sh
VERSION=0.0.1

# Output
OUTDIR="./"

# Settings
NCORES=1
LOG=false

# Tools
BWA=bwa
SAMTOOLS=samtools
STATS_SCRIPT="iPCR-bwa_mem_stats.sh"

# If any command fails, fail the entire script
set -e

# Log the starttime
starttime=$(date +%s)

#-------------------------------------------------------------------------------#
#                                 Parse options                                 #
#-------------------------------------------------------------------------------#
OPTIND=1         # Reset in case getopts has been used previously in the shell.
USAGE=
usage() {
  echo >&2 "usage: ${SCRIPTNAME} [options]"
  echo >&2 "OPTIONS:"
  echo >&2 "  -o: directory for generated output files  [./]"
  echo >&2 "  -i: BWA index [required]"
  echo >&2 "  -f: Forward reads [required]"
  echo >&2 "  -r: Reverse reads [required]"
  echo >&2 "  -g: Read group header [required]"
  echo >&2 "  -t: target intervals (BED file used for calculating statistics)"
  echo >&2 "  -l: write messages to logfile (OUTDIR/BASENAME.log) instead of stdout"
  echo >&2 "  -b: sets basename used for all output files [default: based on input filename]"
  echo >&2 "  -n: number of cores used where possible [default: $NCORES]"
  echo >&2 "  -c: do not clean up intermediate files [default: $CLEAN]"
  echo >&2 "  -h: print this message"
  echo >&2 ""
  exit 1;
}

while getopts "h?f:o:r:i:l:b:n:g:c" opt; do
  case $opt in
    l)
      LOG="true";
      ;;
    n)
      NCORES=$OPTARG;
      ;;
    f)
      FORWARD=$OPTARG;
      ;;
    r)
      REVERSE=$OPTARG;
      ;;
    g)
      READGROUP=$OPTARG;
      ;;
    t)
      TARGETS=$OPTARG;
      ;;
    i)
      REFERENCE=$OPTARG;
      ;;
    o)
      OUTDIR=$OPTARG;
      ;;
    b)
      BASENAME=$OPTARG;
      ;;
    c)
      CLEAN=false;
      ;;
    h)
      usage;
      ;;
    \?)
      echo "option not recognized: "$opt
      usage
      ;;
  esac
done
shift $(( OPTIND - 1 ))

#-------------------------------------------------------------------------------#
#                              IO definitions                                   #
#-------------------------------------------------------------------------------#
# Check all required options are set
if [ -z ${OUTDIR+x} ]; then echo "option -o not set (directory for output files)"; usage; exit 1; fi
if [ -z ${BASENAME+x} ]; then
  # Create BASENAME based on 1st input fastq filename remove ".fastq.*" (or ".fq.*") from filename
  BASENAME=$(basename ${FORWARD} | sed -e 's/_forw.[fF]\(ast\|AST\)\?[qQ].*//')
fi

# Check required subdirectories exist
if [ ! -d ${OUTDIR} ]; then mkdir -p ${OUTDIR}; echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] making directory \"${OUTDIR}\" for output"; echo ""; fi

# Write stdout to stdout or a log file
if [ ${LOG} == "true" ]; then 
  LOG="${OUTDIR}/${BASENAME}.log"
  exec >${LOG}
  exec 2>&1
fi

# Check if script has been run succesfully previously
if [ -f "${OUTDIR}/${BASENAME}.done" ]; then
  echo "[WARN - $(date '+%Y-%m-%d %H:%M:%S')] Overwriting previous results"
  rm "${OUTDIR}/${BASENAME}.done"
fi

#-------------------------------------------------------------------------------#
#                              Main program loop                                #
#-------------------------------------------------------------------------------#
# Read group information
# For more details see: https://gatkforums.broadinstitute.org/gatk/discussion/6472/read-groups
#SAMPLE_ID="TEST1"
#PLATFORM="ILLUMINA"
#DNA_PREP_LIB="NA?"
#READ_GROUP_ID="abababa001lane1"
#PLATFORM_UNIT="AAAA.001.CCCC"

# Final readgroup for this sample
#READ_GROUP="'@RG\tID:${READ_GROUP_ID}\tPU:${PLATFORM_UNIT}\tSM:${SAMPLE_ID}\tPL:${PLATFORM}\tLB:${DNA_PREP_LIB}'"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] BWA"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Read group header: ${READGROUP}"
#echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Read group id's:"
#echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] SAMPLE_ID=${SAMPLE_ID}"
#echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] PLATFORM=${PLATFORM}"
#echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] DNA_PREP_LIB=${DNA_PREP_LIB}"
#echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] PLATFORM_UNIT=${PLATFORM_UNIT}"
#echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Final header=${READ_GROUP}"

# TODO: Maybe use samtools view instead of sort for faster speeds, since the BAM is sorted later on in the pipeline anyway, or remove the later sorting step

# Construct the command
BWA_CMD="${BWA} mem \
-t ${NCORES} \
-M \
-R '${READGROUP}' \
${REFERENCE} \
${FORWARD} \
${REVERSE} \
| ${SAMTOOLS} sort \
-@ ${NCORES} \
-n \
-O BAM \
-o ${OUTDIR}/${BASENAME}.bam"


#> ${OUTDIR}/${BASENAME}.sam"

#| ${SAMTOOLS} sort \
#--threads 1 \
#-O BAM \
#-o ${OUTDIR}/${BASENAME}.bam"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Using basename: ${BASENAME}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting BWA"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] BWA command = ${BWA_CMD}"
echo "=========================================================================="

eval $BWA_CMD

if [ ! -z ${TARGETS} ]
then
  STATS_CMD="${STATS_SCRIPT} \
-i ${OUTDIR}/${BASENAME}.bam \
-o ${OUTDIR} \
-b ${BASENAME} \
-t ${TARGETS}"

  echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Calclating statistics"
  echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] STATS command = ${STATS_CMD}"
  echo "=========================================================================="

  eval $STATS_CMD
  
else
  echo "[WARN - $(date '+%Y-%m-%d %H:%M:%S')] Not calculating amplification statistics, no targets file provided."
fi

echo "=========================================================================="
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"

# Log the TMP or intermediate files not critical for the output to the done file
# These can then be cleaned later or right away if -c is specified
echo "" > ${OUTDIR}/${BASENAME}.done

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done"
