#!/bin/bash

# AUTHOR / DATE
#   Ludo Pagie; March 22, 2016; iPCR-trim.bash
#   Edited by Olivier Bakker: August 2018
#
# INTRO / BACKGROUND
#   WARN: Adapted for usage in the UMCG SuRE-SNP pipeline August 2018
#   bash script (awk, bowtie2, samtools and cutadapt) to process raw fastq
#   files containing data from iPCR samples. The barcodes and gDNA are
#   extracted from the reads Barcodes with length != 20, or which contain N's
#   are discarded
#
# USAGE / INPUT / ARGUMENTS / OUTPUT
# USAGE:
#   required:
#   1 pair of fastq files for input are given as final arguments 
#   -o: output directory
#   optional:
#   -f: forward adapter sequence 
#   -r: reverse adapter sequence 
#   -d: digestion site
#   -l: write to logfile instead of stdout
#   -b: basename [based on input file name]
#   -c: if set; do not clean intermediate files
#   -n: number of cores used in parallel processes (1)
# INPUT:
#   iPCR fastq files
# OUTPUT:
#   fastq files
#
# VERSIONS:
#   -170203: initial version, VERSION set to 0.0.1
#   -180808: VERSION set to 0.0.2
#
# TODO
#   - parameterize filter criteria (min MAPQ score, BC length, etc)

# Parameter settings
SCRIPTNAME=iPCR-trim.sh
VERSION=0.0.2

# Tools
GAWK=gawk
CUTADAPT=cutadapt

# Global variable defaults (are overidden if options provided)
NCORES=1
MIN_READ_LENGTH=25
RESTRICT_SITE=""
CLEAN=true;
LOG="false"

# PARSE OPTIONS
OPTIND=1         # Reset in case getopts has been used previously in the shell.
USAGE=
usage() {
  echo >&2 "usage: ${SCRIPTNAME} -o[frdlbnch] forw-reads.fastq[.gz/bz2] rev-reads.fastq[.gz/.bz2]"
  echo >&2 "OPTIONS:"
  echo >&2 "  -o: directory for generated output files  [required]"
  echo >&2 "  -f: forward read adapter sequence [required]"
  echo >&2 "  -r: reverse read adapter sequence [required]"
  echo >&2 "  -d: digestion site used for minimzing iPCR circle"
  echo >&2 "  -l: write messages to logfile (OUTDIR/BASENAME.log) instead of stdout"
  echo >&2 "  -b: sets basename used for all output files [default: based on input filename]"
  echo >&2 "  -n: number of cores used where possible [default: $NCORES]"
  echo >&2 "  -c: do not clean up intermediate files [default: $CLEAN]"
  echo >&2 "  -h: print this message"
  echo >&2 ""
  exit 1;
}

while getopts "h?f:o:r:d:lb:n:c" opt; do
  case $opt in
    l)
      LOG="true";
      ;;
    n)
      NCORES=$OPTARG;
      ;;
    f)
      ADPTR_FORW_SEQ=$OPTARG;
      ;;
    r)
      ADPTR_REV_SEQ=$OPTARG;
      ;;
    d)
      RESTRICT_SITE=$OPTARG;
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

# The remaining CLI arguments should be a pair of filenames which are the forward and reverse reads fastq files.
# Check we have exactly 2 remaining arguments
if [ ! $# -eq 2 ]; then
  echo "[ERROR - $(date '+%Y-%m-%d %H:%M:%S')] too few, or too many, arguments left after options are parsed (should be 1 pair of fastq filenames).\nThe remaining args are:"
  while test $# -gt 0; do
    echo $1
    shift
  done
  echo "[ERROR - $(date '+%Y-%m-%d %H:%M:%S')] Aborting"
  usage
  exit 1
fi

# Retrieve input fastq files from command line
declare -a FASTQ_FNAMES=( "$@" );

# Check; file exists, whether compressed (set CAT)
abort_flag="false"
for f in ${FASTQ_FNAMES[@]}; do
  if [ ! -f ${f} ]; then
    echo "[ERROR - $(date '+%Y-%m-%d %H:%M:%S')] fastq file (${f}) doesn't exist." 
    abort_flag=true
  fi
done

if [ $abort_flag == 'true' ]; then
  echo "[ERROR - $(date '+%Y-%m-%d %H:%M:%S')] Aborting"
  usage
  exit 1
fi
unset abort_flag

# Determine file extension of the first file (assuming both files are compressed identically)
f=${FASTQ_FNAMES[0]}
extension="${f##*.}"

# Determine CAT command
case ${extension} in
  gz)
    CAT="gzip -cd ";
    ;;
  bz2)
    CAT="bzip2 -cd ";
    ;;
  *)
    CAT="cat ";
    ;;
esac
unset extension
unset f

# Make name of fastq file absolute
for (( i=0; i<2; i++ )); do
  D=`dirname "${FASTQ_FNAMES[$i]}"`
  B=`basename "${FASTQ_FNAMES[$i]}"`
  DD="`cd $D 2>/dev/null && pwd || echo $D`"
  FASTQ_FNAMES[$i]="$DD/$B"
done

# Check all required options are set
if [ -z ${OUTDIR+x} ]; then echo "option -o not set (directory for output files)"; usage; exit 1; fi
if [ -z ${BASENAME+x} ]; then
  # Create BASENAME based on 1st input fastq filename remove ".fastq.*" (or ".fq.*") from filename
  BASENAME=$(basename ${FASTQ_FNAMES[0]} | sed -e 's/.[fF]\(ast\|AST\)\?[qQ].*//')
fi

# Check required subdirectories exist
if [ ! -d ${OUTDIR} ]; then mkdir -p ${OUTDIR}; echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] making directory \"${OUTDIR}\" for output"; echo ""; fi

# Make path to OUTDIR absolute
OUTDIR="`cd \"$OUTDIR\" 2>/dev/null && pwd || echo \"$OUTDIR\"`"

# Write stdout to stdout or a log file
if [ ${LOG} == "true" ]; then 
  LOG="${OUTDIR}/${BASENAME}.log"
  exec >${LOG}
  exec 2>&1
fi

### Logging ###
# Print values of variables and CLI args for log
# Print header for log
LINE="[INFO - $(date '+%Y-%m-%d %H:%M:%S')] running "${SCRIPTNAME}" (version: "$VERSION")"
SEPARATOR=$(head -c ${#LINE} </dev/zero | tr '\0' '=')
echo $SEPARATOR; 
echo $LINE; 
echo $SEPARATOR
starttime=$(date +%s)
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] User set variables:"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] directory for output files=${OUTDIR}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] basename for output files=${BASENAME}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] adapter sequence=${ADPTR_SEQ}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] LOG=${LOG}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] NCORES=${NCORES}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] MIN_READ_LENGTH=${MIN_READ_LENGTH}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] ADPTR_FORW_SEQ=${ADPTR_FORW_SEQ}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] ADPTR_REV_SEQ=${ADPTR_REV_SEQ}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] RESTRICT_SITE=${RESTRICT_SITE}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] CLEAN=${CLEAN}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] fastq files for input:"
for f in ${FASTQ_FNAMES[@]};
do 
    echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] $f";
done

# Print software version info
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Used software:"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] unix/host $(uname -a)"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] bash: $(bash --version | head -n 1)"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] gawk: executable used: ${GAWK} $(${GAWK} --version | head -n 1)"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] cutadapt: executable used: ${CUTADAPT} $(${CUTADAPT} --version)"

# Setwd processing directory
cd ${OUTDIR}
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] finished prepping for processing"
echo "=========================================================================="
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] MAIN: starting to process fastq file" 

STATS="${OUTDIR}/${BASENAME}.stats"

### Trimming reads ###
# construct cutadapt command:
createCMD () {
  CMD="${CUTADAPT} -g ${ADPTR} -o ${OUTDIR}/${BASENAME}_${DIR}.fastq \
         --info-file=${OUTDIR}/${BASENAME}_${DIR}.info -O4 ${FASTQ} >> ${OUTDIR}/${BASENAME}_${DIR}.stats"
  if [[ ! -z ${RESTRICT_SITE} ]]
  then
    echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] extending cutadapt CMD because restrict-site is not empty (${RESTRICT_SITE})"
    CMD="${CMD}; \
           mv ${OUTDIR}/${BASENAME}_${DIR}.fastq ${OUTDIR}/tmp.${BASENAME}_${DIR}.fastq; \
           ${CUTADAPT} -a ${RESTRICT_SITE} -o ${OUTDIR}/${BASENAME}_${DIR}.fastq -O4 \
           ${OUTDIR}/tmp.${BASENAME}_${DIR}.fastq >> ${OUTDIR}/${BASENAME}_${DIR}.stats; \
  	 rm -f ${OUTDIR}/tmp.${BASENAME}_${DIR}.fastq"
  fi
}

# Trim forw reads
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] forward reads file = ${FASTQ_FNAMES[0]}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] starting to trim adapter in forward reads; trim adapter from 5'" 
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] and trim all after digest restriction site (${RESTRICT_SITE}) site on 3'"

ADPTR=${ADPTR_FORW_SEQ}
FASTQ=${FASTQ_FNAMES[0]}
DIR=forw
createCMD;
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] cutadapt command = ${CMD}"
eval "${CMD}"

# Record some stats in file STATS
cat "${OUTDIR}/${BASENAME}_${DIR}.stats" | \
  ${GAWK} '
    BEGIN {OFS="\t"}
    /Total reads processed/{
      printf ("totalReadCount\t%d\n", 
              gensub(/,/,"","g", gensub(/^.*:\s*(.*)$/,"\\1", "g"))); 
      exit}' >> ${STATS}
cat "${OUTDIR}/${BASENAME}_${DIR}.stats" | \
  ${GAWK} '
    BEGIN {OFS="\t"}
    /Reads with adapters/{
      printf ("trimmedForwReadCount\t%d\n", 
              gensub(/,/,"","g", gensub(/^.*:\s*(.*)$/,"\\1", "g"))); 
      exit}' >> ${STATS}
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] finished trimming adapter in forward reads"

# Trim reverse reads
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] reverse reads file = ${FASTQ_FNAMES[1]}"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] starting to trim adapter in reverse reads; trim adapter from 5'" 
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] and trim all after digest restriction site (${RESTRICT_SITE}) site on 3'"

ADPTR=${ADPTR_REV_SEQ}
FASTQ=${FASTQ_FNAMES[1]}
DIR=rev
createCMD;
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] cutadapt command = ${CMD}"
eval "${CMD}"

# Record some stats in file STATS
cat "${OUTDIR}/${BASENAME}_${DIR}.stats" | \
  ${GAWK} '
    BEGIN {OFS="\t"}
    /Reads with adapters/{
      printf ("trimmedRevReadCount\t%d\n", 
              gensub(/,/,"","g", gensub(/^.*:\s*(.*)$/,"\\1", "g"))); 
      exit}' >> ${STATS}
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] finished trimming adapter in reverse reads"
echo "=========================================================================="

### Filter reads ###
# Remove all reads which are $MIN_READ_LENGTH basepairs or shorter
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] starting to filtered reads too short for aligning to genome"

# Filter_read_length
FORW="${OUTDIR}/${BASENAME}_forw.fastq.tmp"
REV="${OUTDIR}/${BASENAME}_rev.fastq.tmp"
FORW_FLTR="${OUTDIR}/${BASENAME}_forw.fastq"
REV_FLTR="${OUTDIR}/${BASENAME}_rev.fastq"
INFO_FORW="${OUTDIR}/${BASENAME}_forw.info"

mv $FORW_FLTR $FORW
mv $REV_FLTR $REV

${GAWK} -v file1=${FORW}  -v file2=${REV} -v out1=${FORW_FLTR} -v out2=${REV_FLTR} -v min_length=${MIN_READ_LENGTH} '
  BEGIN {
    OFS="\n"
    FS="\t"
    incl=0;
    excl=0;
    while((getline id1 < file1)>0) { 
      getline seq1 < file1; getline p1 < file1; getline qual1 < file1; 
      getline id2 <file2; getline seq2 < file2; getline p2 < file2; getline qual2 < file2;
      if ( length(seq1) > min_length && length(seq2) > min_length ) {
        print id1,seq1,p1,qual1 > out1;
        print id2,seq2,p2,qual2 > out2;
        incl++;
      } else {
        excl++;
      }
    }
    print "removed "excl" paired-end reads for which either forward or reverse read were "min_length"bp or shorter\n";
    print incl" reads passed this filter\n\n";
    printf ("lengthFilteredReadCount\t%d\n\n", incl);
  }' >> ${STATS}

# Delete temporary fastq files
rm -f *fastq.tmp

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] finished filtered read on length"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] finished processing ${BASENAME} files"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"