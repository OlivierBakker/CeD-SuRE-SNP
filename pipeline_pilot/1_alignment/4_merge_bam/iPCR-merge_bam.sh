#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=32G
#SBATCH --ntasks=1
#SBATCH --nodes=1

ml picard
ml SAMtools

#-------------------------------------------------------------------------------#
#                            Script global Settings                             #
#-------------------------------------------------------------------------------#
# Version info
SCRIPTNAME=iPCR-merge_bam.sh
VERSION=0.0.1

# Output
OUTDIR="./"

# Settings
NCORES=1
LOG=false

# Tools
PICARD="java -jar $EBROOTPICARD/picard.jar"
SAMTOOLS="samtools"

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
  echo >&2 "  -i: File with input SAMs [required]"
  echo >&2 "  -l: write messages to logfile (OUTDIR/BASENAME.log) instead of stdout"
  echo >&2 "  -b: sets basename used for all output files [REQUIRED]"
  echo >&2 "  -n: number of cores used where possible [default: $NCORES]"
  echo >&2 "  -c: do not clean up intermediate files [default: $CLEAN]"
  echo >&2 "  -h: print this message"
  echo >&2 ""
  exit 1;
}

while getopts "h?f:o:i:l:b:n:c" opt; do
  case $opt in
    l)
      LOG="true";
      ;;
    n)
      NCORES=$OPTARG;
      ;;
    i)
      INPUT_FILE=$OPTARG;
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

# Check required subdirectories exist
if [ ! -d ${OUTDIR} ]; then mkdir -p ${OUTDIR}; echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] making directory \"${OUTDIR}\" for output"; echo ""; fi

# Write stdout to stdout or a log file
if [ ${LOG} == "true" ]; then 
  LOG="${OUTDIR}/${BASENAME}.log"
  exec >${LOG}
  exec 2>&1
fi

#-------------------------------------------------------------------------------#
#                              Main program loop                                #
#-------------------------------------------------------------------------------#

INPUT_FILES=""

COUNTER=0
while read file;
do

CMD="${PICARD} SortSam \
INPUT=${file} \
SORT_ORDER=queryname \
OUTPUT=./tmp/$(basename $file)_${COUNTER}.querysorted.tmp.bam \
TMP_DIR=./tmp"

eval $CMD

#INPUT_FILES="${INPUT_FILES} I=${file}"
INPUT_FILES="${INPUT_FILES} I=./tmp/$(basename $file)_${COUNTER}.querysorted.tmp.bam"


COUNTER=$((COUNTER + 1))
done < ${INPUT_FILE}


# Construct the command
CMD="${PICARD} MergeSamFiles \
${INPUT_FILES} \
SORT_ORDER=queryname \
O=${OUTDIR}/${BASENAME}.querysorted.tmp.bam \
VALIDATION_STRINGENCY=LENIENT \
ASSUME_SORTED=true \
TMP_DIR=./tmp"


echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Using basename: ${BASENAME}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting picard"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] picard command = ${CMD}"
echo "=========================================================================="

eval $CMD

echo "=========================================================================="


CMD="${SAMTOOLS} view \
-b \
-h \
-f 2 \
-F 256 \
-q 30 \
${OUTDIR}/${BASENAME}.querysorted.tmp.bam > \
${OUTDIR}/${BASENAME}.querysorted.bam"


eval $CMD


echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done"
