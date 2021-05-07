#!/bin/bash

for file in ../../../pipeline/5_allele_specificity/output/sumstats_both_strands/SuRE_CeD_*_wilcox.assoc; do tmp=$(basename $file); awk '{print $1"\t"$10"\t"$11"\t"$4}' $file > ${tmp}; done

for ct in K562 Caco CacoStim; do cat SuRE_CeD_INDEL_${ct}_wilcox.assoc | sed 's/NA/0/g' > ${ct}.assoc.sure.annot; tail -n +2 SuRE_CeD_SNP_${ct}_wilcox.assoc | sed 's/NA/0/g' >> ${ct}.assoc.sure.annot; done


awk '{print $1"\t"$10"\t"$11"\t"$4}' ../../../pipeline/5_allele_specificity/output/sumstats_both_strands/SuRE_CeD_snps_indels_jurkatStim_wilcox.assoc | sed 's/NA/0/g' > JurkatStim.assoc.sure.annot
awk '{print $1"\t"$10"\t"$11"\t"$4}' ../../../pipeline/5_allele_specificity/output/sumstats_both_strands/SuRE_CeD_snps_indels_jurkat_wilcox.assoc | sed 's/NA/0/g' > Jurkat.assoc.sure.annot


rm *_wilcox.assoc


#file=$1
#ct=$1


for ct in K562 Caco2 Caco2Stim Jurkat JurkatStim;
do

file=../../../pipeline/5_allele_specificity/output/SuRE_CeD_snps_indels_wilcox_per_strand_${ct}.assoc

echo -e "SNP\tP+" > ${ct}.plus.assoc.sure.annot
grep + $file | awk '{print $1"\t"$4}' >> ${ct}.plus.assoc.sure.annot

echo -e "SNP\tP-" > ${ct}.minus.assoc.sure.annot
grep - $file | awk '{print $1"\t"$4}' >> ${ct}.minus.assoc.sure.annot
done
