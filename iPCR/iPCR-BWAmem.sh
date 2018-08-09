#!/bin/bash

# forward reads
# reverse reads
# reference + index
# outdir
# prefix for output


# Input
FORWARD=$1
REVERSE=$2
REFERENCE=$3

# Output
OUTDIR=
PREFIX=

# Settings
NCORES=1

# Tools
BWA=bwa
SAMTOOLS=samtools


# Read group information
# For more details see: https://gatkforums.broadinstitute.org/gatk/discussion/6472/read-groups
SAMPLE_ID="$(basename $FORWARD)"
PLATFORM="ILLUMINA"
DNA_PREP_LIB="NA?"
READ_GROUP_ID="flowcell + lane name and number"
PLATFORM_UNIT="{FLOWCELL_BARCODE}.{LANE}.{SAMPLE_BARCODE}"

# Final readgroup for this sample
READ_GROUP="@RG\tID:${READ_GROUP_ID}\tPU:${PLATFORM_UNIT}\tSM:${SAMPLE_ID}\tPL:${PLATFORM}\tLB:${DNA_PREP_LIB}"

# TODO: Maybe use samtools view instead of sort for faster speeds, since the BAM is sorted later on in the pipeline anyway, or remove the later sorting step

$BWA mem \
-t $NCORES \
-M \
-R $READ_GROUP \
$REFERENCE \
$FORWARD \
$REVERSE \
| $SAMTOOLS sort \
-@${NCORES} \
-o ${OUTDIR}/${PREFIX}.bam