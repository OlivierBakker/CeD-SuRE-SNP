#!/bin/bash

sample="SuRE_1"

while read sample;
do

CMD="sbatch --output='logs/${sample}.out' --error='logs/${sample}.err' iPCR-haplotypecaller.sh \
-i ../2_gatk_base_recalibrator/output/${sample}.recal.bam \
-r /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta \
-d /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/dbsnp_138.b37.vcf \
-u input/CeD_target_regions.interval.list \
-o output/"

eval $CMD

done < ../../0_data/samples.txt
