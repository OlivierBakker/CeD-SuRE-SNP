#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=4G
#SBATCH --ntasks=6

ml Python/3.6.3-foss-2015b
SAMPLE=$1

echo $SAMPLE
cat /groups/umcg-wijmenga/tmp04/umcg-obbakker/projects/pr_sure_snp/iPCR_pipeline/full_expriment/alignment/2_trim_adapters/output/*/SuRE53_${SAMPLE}_forw.fq.gz > output/${SAMPLE}_R1.tmp.fq.gz
cat /groups/umcg-wijmenga/tmp04/umcg-obbakker/projects/pr_sure_snp/iPCR_pipeline/full_expriment/alignment/2_trim_adapters/output/*/SuRE53_${SAMPLE}_rev.fq.gz > output/${SAMPLE}_R2.tmp.fq.gz

~/.local/bin/cutadapt -m 25:50 -j 6 -o output/${SAMPLE}_R1.fq.gz -p output/${SAMPLE}_R2.fq.gz output/${SAMPLE}_R1.tmp.fq.gz output/${SAMPLE}_R2.tmp.fq.gz
rm output/${SAMPLE}_R1.tmp.fq.gz output/${SAMPLE}_R2.tmp.fq.gz

