#!/bin/bash

#-------------------------------------------------------------------------------#
#                            Script global Settings                             #
#-------------------------------------------------------------------------------#
# Version info
SCRIPTNAME=iPCR-genotype_gvcfs.sh
VERSION=0.0.1

# Output
OUTDIR="./"

# Settings
LOG=false

# Tools
GATK="java -XX:ParallelGCThreads=1 -Xmx4g -jar \${EBROOTGATK}/GenomeAnalysisTK.jar"

# If any command fails, fail the entire script
set -e

# Log the starttime
starttime=$(date +%s)

ml GATK/3.8-0-foss-2015b-Java-1.8.0_74

#-------------------------------------------------------------------------------#
#                                 Parse options                                 #
#-------------------------------------------------------------------------------#

OUTDIR="output"
BASENAME="SuRE_53"
LOG="false"

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
# Jointly call genotypes from the individual GVCFs

b37="/apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta"
OMNI="/apps/data/ftp.broadinstitute.org/bundle/2.8/b37/1000G_omni2.5.b37.vcf"
HAPMAP="/apps/data/ftp.broadinstitute.org/bundle/2.8/b37/hapmap_3.3.b37.vcf"
1000G="/apps/data/ftp.broadinstitute.org/bundle/2.8/b37/1000G_phase1.snps.high_confidence.b37.vcf"
MILLS="/apps/data/ftp.broadinstitute.org/bundle/2.8/b37/Mills_and_1000G_gold_standard.indels.b37.vcf"
DBSNP="/apps/data/ftp.broadinstitute.org/bundle/2.8/b37/dbsnp_138.b37.vcf"

GATK_CMD="${GATK} \
-T VariantRecalibrator \ 
-R ${b37} \ 
-input $1 \ 
-resource:hapmap,known=false,training=true,truth=true,prior=15.0 ${HAPMAP}  \ 
-resource:omni,known=false,training=true,truth=true,prior=12.0 ${OMNI} \ 
-resource:1000G,known=false,training=true,truth=false,prior=10.0 ${1000G} \ 
-resource:dbsnp,known=true,training=false,truth=false,prior=2.0 ${DBSNP} \ 
-an DP \ 
-an QD \ 
-an FS \ 
-an SOR \ 
-an MQ \
-an MQRankSum \ 
-an ReadPosRankSum \ 
-an InbreedingCoeff \
-mode SNP \ 
-tranche 100.0 -tranche 99.9 -tranche 99.0 -tranche 90.0 \ 
-recalFile ${OUTDIR}/${BASENAME}.snp.recal \ 
-tranchesFile ${OUTDIR}/${BASENAME}.snp.tranches \
-rscriptFile recalibrate_SNP_plots.R"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting creating model for SNPs"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] GATK command = ${GATK_CMD}"
echo "=========================================================================="

eval $GATK_CMD


GATK_CMD="${GATK} \
-T ApplyRecalibration \ 
-R ${b37} \ 
-input $1 \ 
-mode SNP \ 
--ts_filter_level 99.0 \ 
-recalFile ${OUTDIR}/${BASENAME}.snp.recal \ 
-tranchesFile ${OUTDIR}/${BASENAME}.snp.tranches \ 
-o ${OUTDIR}/${BASENAME}.recalibrated_snps_raw_indels.vcf"


echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Applying model for SNPs"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] GATK command = ${GATK_CMD}"
echo "=========================================================================="

eval $GATK_CMD

# INDELS

GATK_CMD="${GATK} \
-T VariantRecalibrator \
-R ${b37} \
-input ${OUTDIR}/${BASENAME}.recalibrated_snps_raw_indels.vcf \
-resource:mills,known=false,training=true,truth=true,prior=12.0 ${MILLS} \
-resource:dbsnp,known=true,training=false,truth=false,prior=2.0 ${DBSNP} \
-an QD \
-an DP \
-an FS \
-an SOR \
-an MQRankSum \
-an ReadPosRankSum \
-an InbreedingCoef \
-mode INDEL \
-tranche 100.0 -tranche 99.9 -tranche 99.0 -tranche 90.0 \
--maxGaussians 4 \
-recalFile ${OUTDIR}/${BASENAME}.indel.recal \
-tranchesFile ${OUTDIR}/${BASENAME}.indel.tranches \
-rscriptFile recalibrate_INDEL_plots.R"

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Starting creating model for INDELs"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] GATK command = ${GATK_CMD}"
echo "=========================================================================="

eval $GATK_CMD


GATK_CMD="${GATK} \
-T ApplyRecalibration \ 
-R ${b37} \ 
-input ${OUTDIR}/${BASENAME}.recalibrated_snps_raw_indels.vcf \ 
-mode INDEL \ 
--ts_filter_level 99.0 \ 
-recalFile ${OUTDIR}/${BASENAME}.indel.recal \ 
-tranchesFile ${OUTDIR}/${BASENAME}.indel.tranches \ 
-o ${OUTDIR}/${BASENAME}.recalibrated_variants.vcf"


echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Applying model for INDELs"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] GATK command = ${GATK_CMD}"
echo "=========================================================================="

eval $GATK_CMD


echo "=========================================================================="
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] script ran for $(( ($(date +%s) - ${starttime}) / 60)) minutes"

# Log the TMP or intermediate files not critical for the output to the done file
# These can then be cleaned later or right away if -c is specified
echo "" > ${OUTDIR}/${BASENAME}.done

echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Done"