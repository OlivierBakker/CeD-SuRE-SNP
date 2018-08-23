#!/bin/bash

#-------------------------------------------------------------------------------#
#                            Script global Settings                             #
#-------------------------------------------------------------------------------#
# Version info
SCRIPTNAME=iPCR-name.sh
VERSION=0.0.1

# Output
OUTDIR="./"

# Settings
NCORES=1
LOG=false

# Tools
TOOL=bla

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
  echo >&2 "  -i: input bam"
  echo >&2 "  -b: sets basename used for all output files [default: based on input filename]"
  echo >&2 "  -o: directory for generated output files  [./]"
  echo >&2 "  -t: target intervals (used for calculating statistics)"
  echo >&2 "  -l: write messages to logfile (OUTDIR/BASENAME.log) instead of stdout"
  echo >&2 "  -n: number of cores used where possible [default: $NCORES]"
  echo >&2 "  -c: do not clean up intermediate files [default: $CLEAN]"
  echo >&2 "  -h: print this message"
  echo >&2 ""
  exit 1;
}

while getopts "h?f:o:r:i:l:b:n:c" opt; do
  case $opt in
    l)
      LOG="true";
      ;;
    n)
      NCORES=$OPTARG;
      ;;
    i)
      INPUT_BAM=$OPTARG;
      ;;
    t)
      TARGETS=$OPTARG;
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
if [ -z ${OUTDIR+x} ]; then echo "[ERROR - $(date '+%Y-%m-%d %H:%M:%S')] option -o not set (directory for output files)"; usage; exit 1; fi
if [ -z ${INPUT_BAM+x} ]; then "[ERROR - $(date '+%Y-%m-%d %H:%M:%S')] option -i not set"; usage; exit 1; fi
if [ -z ${BASENAME+x} ]; then
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
# Comment
CMD="${TOOL}"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] INFO"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] command = ${CMD}"

echo "==========================================================================" 

eval $CMD

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