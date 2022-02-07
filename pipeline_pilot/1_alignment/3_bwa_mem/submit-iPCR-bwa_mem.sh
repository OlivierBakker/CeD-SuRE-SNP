#!/bin/bash

set -e


sample=$1

echo "[INFO] submitting $sample"
mkdir -p output/$(basename sample)


READ_GROUP='"@RG\tID:1\tPU:1\tSM:1\tPL:1\tLB:1"'


CMD="bash iPCR-bwa_mem.sh \
-o ./output/$(basename $sample) \
-i /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/BWA/0.7.12-goolf-1.7.20/human_g1k_v37.fasta \
-f ../2_trim_adapters/output/$(basename $sample)_forw.fq.gz \
-r ../2_trim_adapters/output/$(basename $sample)_rev.fq.gz \
-g ${READ_GROUP} \
-n 4"

eval $CMD

