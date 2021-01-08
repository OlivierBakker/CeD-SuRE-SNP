#!/bin/bash



while read sample;
do

CMD="sbatch --output='logs/${sample}.out' --error='logs/${sample}.err' iPCR-mark_duplicates.sh \
-i ../../alignment/5_filter_bam/output/${sample}.querysorted.bam \
-o output/"

eval $CMD

#echo $CMD

done < samples_rescue.txt


