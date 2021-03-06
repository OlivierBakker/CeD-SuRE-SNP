#!/bin/bash
#SBATCH --time=23:59:00
#SBATCH --mem=8G
#SBATCH --cpus-per-task=1
#SBATCH --nodes=1


#-------------------------------------------------------------------------------#
#                            Script global Settings                             #
#-------------------------------------------------------------------------------#
# Version info
SCRIPTNAME=iPCR-extract_target_regions.sh
VERSION=0.0.1

# Output
OUTDIR="./"

# Settings
LOG=false

# Tools
PICARD="java -XX:ParallelGCThreads=1 -Xmx7g -jar \${EBROOTPICARD}/picard.jar"

# If any command fails, fail the entire script
set -e

# Log the starttime
starttime=$(date +%s)

ml picard

#-------------------------------------------------------------------------------#
#                                 Parse options                                 #
#-------------------------------------------------------------------------------#
OPTIND=1         # Reset in case getopts has been used previously in the shell.
USAGE=
usage() {
  echo >&2 "usage: ${SCRIPTNAME} [options]"
  echo >&2 "OPTIONS:"
  echo >&2 "  -i: bamfile[required]"
  echo >&2 "  -o: directory for generated output files  [./]"
  echo >&2 "  -l: write messages to logfile (OUTDIR/BASENAME.log) instead of stdout"
  echo >&2 "  -b: sets basename used for all output files [default: based on input filename]"
  echo >&2 "  -c: do not clean up intermediate files [default: $CLEAN]"
  echo >&2 "  -n: number of cores used where possible [default: $NCORES]"
  echo >&2 "  -h: print this message"
  echo >&2 ""
  exit 1;
}

while getopts "h?o:r:i:l:b:n:c" opt; do
  case $opt in
  	i)
	    INPUT_BAM=$OPTARG;
	    ;;
    l)
      LOG="true";
      ;;
    n)
      NCORES=$OPTARG;
      ;;
    n)
      NCORES=$OPTARG;
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
  BASENAME=$(basename ${INPUT_BAM} | sed -e 's/\..*bam.*//')
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
mkdir -p picard_tmp

# Sorting reads
PICARD_CMD="${PICARD} \
SortSam \
I=${INPUT_BAM} \
O=${OUTDIR}/$BASENAME.sorted.bam \
SORT_ORDER=coordinate \
TMP_DIR=./picard_tmp"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Using basename: ${BASENAME}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting sorting on coordinate"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Picard command = ${PICARD_CMD}"
echo "=========================================================================="

eval $PICARD_CMD


# Mark duplicate mapped reads
PICARD_CMD="${PICARD} \
MarkDuplicates \
I=${OUTDIR}/$BASENAME.sorted.bam \
O=${OUTDIR}/$BASENAME.dedup.bam \
M=${OUTDIR}/$BASENAME.dupmetrics \
create_index=true"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Using basename: ${BASENAME}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting marking of duplicates"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Picard command = ${PICARD_CMD}"
echo "=========================================================================="

eval $PICARD_CMD

echo "=========================================================================="
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"

# Log the TMP or intermediate files not critical for the output to the done file
# These can then be cleaned later or right away if -c is specified
echo "" > ${OUTDIR}/${BASENAME}.done

if [ ${CLEAN} == "true" ]; then
  echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Cleaning intermediate files"
  # Could loop over the .done file, but for safety's sake ill did it like this, to avoid any mishaps
fi

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done"
