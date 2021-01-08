#!/bin/bash


ml GATK/3.8-0-foss-2015b-Java-1.8.0_74

#bash 9_iPCR-genotype_gvcfs.sh \
#sbatch 9_iPCR-genotype_gvcfs.sh \
sbatch iPCR-genotype_gvcfs.sh \
-i ../3_gatk_haplotype_caller/output/ \
-d /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/dbsnp_138.b37.vcf \
-r /apps/data/ftp.broadinstitute.org/bundle/2.8/b37/human_g1k_v37.fasta \
-o output/ \
-b SuRE_CeD 
