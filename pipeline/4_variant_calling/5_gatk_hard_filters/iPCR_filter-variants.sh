#!/bin/bash


ml GATK/3.8-0-foss-2015b-Java-1.8.0_74
ml VCFtools
ml HTSlib
ml BCFtools


#java -jar ${EBROOTGATK}/GenomeAnalysisTK.jar \
#-T SelectVariants \
#-R /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta \
#-V $1 \
#-o $2 \
#--maxIndelSize 40 \
#--maxNOCALLnumber 3 \
#--restrictAllelesTo BIALLELIC \
#--selectTypeToExclude MIXED \
#-select "QD > 10.0" \
#-select "FS < 10.0" \
#-select "MQ > 50.0" \
#-select "ReadPosRankSum > -4.0 && ReadPosRankSum < 4.0" \
#-select "AF > 0.05"


# Filter SNPs
java -jar ${EBROOTGATK}/GenomeAnalysisTK.jar \
-T SelectVariants \
-R /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta  \
-V $1 \
-selectType SNP \
--maxNOCALLnumber 3 \
--restrictAllelesTo BIALLELIC \
-o ${2}.raw.snps.vcf 

java -jar ${EBROOTGATK}/GenomeAnalysisTK.jar \
-T VariantFiltration \
-R /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta  \
-V ${2}.raw.snps.vcf \
--filterExpression "QD < 10.0 || FS > 10.0 || MQ < 50.0 || SOR > 3.0 || MQRankSum < -5.0 || ReadPosRankSum < -4.0 || ReadPosRankSum > 4.0 || AF < 0.05" \
--filterName "FailedSureSnpFilter" \
--missingValuesInExpressionsShouldEvaluateAsFailing \
-o ${2}.filtered.snps.vcf.tmp

# Filter INDELs
java -jar ${EBROOTGATK}/GenomeAnalysisTK.jar \
-T SelectVariants \
-R /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta \
-V $1 \
-selectType INDEL \
-o ${2}.raw.indels.vcf \
--restrictAllelesTo BIALLELIC \
--maxIndelSize 40 \
--maxNOCALLnumber 3 \

java -jar ${EBROOTGATK}/GenomeAnalysisTK.jar \
-T VariantFiltration \
-R /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta \
-V ${2}.raw.indels.vcf \
--filterExpression "QD < 10.0 || FS > 25.0 || SOR > 10.0 || ReadPosRankSum < -4.0 || ReadPosRankSum > 4.0 || AF < 0.05" \
--filterName "FailedSureIndelFilter" \
--missingValuesInExpressionsShouldEvaluateAsFailing \
-o ${2}.filtered.indels.vcf.tmp

# Merge and zip
vcftools --vcf ${2}.filtered.snps.vcf.tmp --remove-filtered-all --recode --recode-INFO-all --stdout | vcf-sort | bgzip -c > ${2}.filtered.snps.vcf.gz
vcftools --vcf ${2}.filtered.indels.vcf.tmp --remove-filtered-all --recode --recode-INFO-all --stdout | vcf-sort | bgzip -c > ${2}.filtered.indels.vcf.gz

tabix -p vcf ${2}.filtered.snps.vcf.gz
tabix -p vcf ${2}.filtered.indels.vcf.gz

#bcftools concat ${2}.filtered.snps.vcf.gz ${2}.filtered.indels.vcf.gz  > ${2}.filtered.vcf


rm ${2}*.tmp* ${2}.raw*

