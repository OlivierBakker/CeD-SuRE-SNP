#!/bin/bash
#SBATCH --time=23:00:00
#SBATCH --mem=4G
#SBTACH --ntasks=1
#SBATCH --nodes=1
#SBATCH --job-name="SureSNP sample selection"


module load Java


java -jar /groups/umcg-wijmenga/tmp04/umcg-obbakker/tools/MafSampleSubset-1.0-SNAPSHOT-all.jar \
--genotype /groups/umcg-wijmenga/tmp04/umcg-obbakker/data/celiac/trynka_ponce/case_only/TrynkaPonceMergedCases \
--input-type PLINK_BED \
--snplist /groups/umcg-wijmenga/tmp04/umcg-obbakker/data/celiac/celiac_snps.txt \
--output-prefix output/sample_list/CaseOnlySampleList \
--permutations 100 \
--subset-size 30 \
-h 2 \
-v \
--snp-priority-maf 0.1 \
--sample-priority 2 \
--target-maf 0.05 
