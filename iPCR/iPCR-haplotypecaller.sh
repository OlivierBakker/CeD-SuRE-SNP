#!/bin/bash

# Version info
SCRIPTNAME=iPCR-base_recalibration.sh
VERSION=0.0.1

# Output
OUTDIR="./"

# Settings
LOG=false

# PARSE OPTIONS
OPTIND=1         # Reset in case getopts has been used previously in the shell.
USAGE=
usage() {
  echo >&2 "usage: ${SCRIPTNAME} [options]"
  echo >&2 "OPTIONS:"
  echo >&2 "  -i: bamfile[required]"
  echo >&2 "  -r: reference sequence fasta [required]"
  echo >&2 "  -d: dbsnp VCF [required]"
  echo >&2 "  -u: interval list [required]"
  echo >&2 "  -o: directory for generated output files  [./]"
  echo >&2 "  -l: write messages to logfile (OUTDIR/BASENAME.log) instead of stdout"
  echo >&2 "  -b: sets basename used for all output files [default: based on input filename]"
  echo >&2 "  -c: do not clean up intermediate files [default: $CLEAN]"
  echo >&2 "  -h: print this message"
  echo >&2 ""
  exit 1;
}

while getopts "h?o:r:d:i:l:b:u:c" opt; do
  case $opt in
  	i)
	    INPUT_BAM=$OPTARG;
	    ;;
    r)
      REF_SEQ=$OPTARG;
      ;;
    d)
      DBSNP=$OPTARG;
      ;;
    l)
      LOG="true";
      ;;
    u)
      INTERVALS=$OPTARG;
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

# Tools
GATK="java -Xmx4g -jar \${EBROOTGATK}/GenomeAnalysisTK.jar"

starttime=$(date +%s)

#Check all required options are set
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

GATK_CMD="${GATK} \
-T HaplotypeCaller \
-R ${REF_SEQ} \
-I ${INPUT_BAM} \
--emitRefConfidence GVCF \
--dbsnp ${DBSNP} \
-L ${INTERVALS} \
-o ${OUTDIR}/${BASENAME}.raw.snps.indels.g.vcf"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting base recalibration"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] GATK command = ${GATK_CMD}"
echo "=========================================================================="

eval $GATK_CMD

echo "=========================================================================="

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"