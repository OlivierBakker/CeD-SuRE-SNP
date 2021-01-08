#!/bin/bash


sample="SuRE_2_merged"


while read sample;
do

echo $sample
#CMD="bash 7_iPCR-base_recalibration.sh \
CMD="sbatch --output='logs/${sample}.out' --error='logs/${sample}.err' iPCR-base_recalibration.sh \
-i ../1_mark_duplicates/output/${sample}.dedup.bam \
-r /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta \
-d /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/dbsnp_138.b37.vcf \
-o output/ "
eval $CMD


done < samples.txt
