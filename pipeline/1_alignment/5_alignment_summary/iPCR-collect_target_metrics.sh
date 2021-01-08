#!/bin/bash

set -e

ml picard

INPUT_BAM=$1
BASENAME=$(basename $INPUT_BAM)
INTERVAL_LIST=../../0_data/CeD_target_regions.interval.list
OUTDIR=./output
PICARD="java -jar $EBROOTPICARD/picard.jar"

#-------------------------------------------------------------------------------#
#                              Main program loop                                #
#-------------------------------------------------------------------------------#

# Generate the Q score distribution
PICARD_CMD="${PICARD} QualityScoreDistribution \
I=${INPUT_BAM} \
O=${OUTDIR}/${BASENAME}.qualscores \
CHART=${OUTDIR}/${BASENAME}.chart.pdf"

echo ""
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Calculating statistics"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Picard command = ${PICARD_CMD}"

echo "==========================================================================" 

eval $PICARD_CMD

echo "==========================================================================" 


PICARD_CMD="${PICARD} CollectTargetedPcrMetrics \
I=${INPUT_BAM} \
O=${OUTDIR}/${BASENAME}.target.metrics \
AMPLICON_INTERVALS=${INTERVAL_LIST} \
TARGET_INTERVALS=${INTERVAL_LIST}"

echo ""
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Calculating statistics"
echo "[INFO - $(date '+%Y-%m-%d %H:%M:%S')] Picard command = ${PICARD_CMD}"

echo "==========================================================================" 

eval $PICARD_CMD
