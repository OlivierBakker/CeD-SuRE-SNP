#!/bin/bash

#-------------------------------------------------------------------------------#
#                            Script global Settings                             #
#-------------------------------------------------------------------------------#
# Version info
SCRIPTNAME=iPCR-merge_bam_and_barcodes.sh
VERSION=0.0.1

# Output
OUTDIR="./"

# Settings
LOG=false

# Tools
PIPELINE_DIR="${pwd}"
IPCR_TOOLS="java -XX:ParallelGCThreads=1 -Xmx4g -jar ${PIPELINE_DIR}/iPCR/iPCR-tools/build/libs/IPCRTools-1.0-SNAPSHOT-all.jar"
PICARD="java -XX:ParallelGCThreads=1 -Xmx4g -jar ${EBROOTPICARD}/picard.jar"

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
  echo >&2 "  -i: input bam file [required]"
  echo >&2 "  -r: input barcode file [required]"
  echo >&2 "  -o: directory for generated output files  [./]"
  echo >&2 "  -l: write messages to logfile (OUTDIR/BASENAME.log) instead of stdout"
  echo >&2 "  -b: sets basename used for all output files [default: based on input filename]"
  echo >&2 "  -c: do not clean up intermediate files [default: $CLEAN]"
  echo >&2 "  -h: print this message"
  echo >&2 ""
  exit 1;
}

while getopts "h?i:r:o:l:b:c" opt; do
  case $opt in
  	i)
	    INPUT_BAM=$OPTARG;
	    ;;
    b)
      INPUT_BARCODE=$OPTARG;
      ;;
    o)
      OUTDIR=$OPTARG;
      ;;
    l)
      LOG="true";
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
  echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Using basename: ${BASENAME}"
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
# Sorting the BAM file on query name
PICARD_CMD="${PICARD} SortSam \
I=${INPUT_BAM} \
O=${OUTDIR}/${BASENAME}.sorted.bam \
SORT_ORDER=queryname"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Started sorting BAM on query name"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Picard command = ${PICARD_CMD}"
echo "==========================================================================" 

eval $PICARD_CMD

echo "==========================================================================" 

# Merge the BAM with the barcode file generated in iPCR-split.sh
IPCR_CMD="${IPCR_TOOLS} -T MergeBamWithBarcodes \
--input-bam ${OUTDIR}/${BASENAME}.sorted.bam \
--barcode-info ${INPUT_BARCODE} \
-s \
| sort -k 1,1 \
| uniq -c \
> ${OUTDIR}/${BASENAME}.raw.ipcr"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting merge"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] iPCR tools command = ${IPCR_CMD}"
echo "=========================================================================="

eval $IPCR_CMD

echo "=========================================================================="
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"

# Log the TMP or intermediate files not critical for the output to the done file
# These can then be cleaned later or right away if -c is specified
echo "${OUTDIR}/${BASENAME}.sorted.bam" > ${OUTDIR}/${BASENAME}.done

if [ ${CLEAN} == "true" ]; then
  echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Cleaning intermediate files"
  # Could loop over the .done file, but for safety's sake ill did it like this, to avoid any mishaps
  rm "${OUTDIR}/${BASENAME}.sorted.bam";
fi

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done"
