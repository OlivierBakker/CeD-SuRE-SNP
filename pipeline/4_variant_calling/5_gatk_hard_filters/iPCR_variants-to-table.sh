#!/bin/bash


ml GATK/3.8-0-foss-2015b-Java-1.8.0_74


java -jar ${EBROOTGATK}/GenomeAnalysisTK.jar \
-T VariantsToTable \
-R /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta \
-V $1 \
-F CHROM -F POS -F ID -F QUAL -F AC -F AN -F BaseQRankSum -F DB -F FS -F MQ -F MQRankSum -F ReadPosRankSum -F QD -F HET -F HOM-REF -F HOM-VAR -F NO-CALL -F TYPE -F MULTI-ALLELIC \
-F ExcessHet -F InbreedingCoeff \
-o $2.table

