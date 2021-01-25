#!/bin/bash

for file in ../../ase_analysis/output/summary_statistics/*_wilcox.assoc; do tmp=$(basename $file); awk '{if ($4 !=1){print $1"\t"$4"\t"$10"\t"$11}}' $file > ${tmp}; done

for ct in K562 Caco CacoStim; do cat SuRE_CeD_INDEL_${ct}_wilcox.assoc > ${ct}.assoc.sure.annot; tail -n +2 SuRE_CeD_SNP_${ct}_wilcox.assoc >> ${ct}.assoc.sure.annot; done

rm *_wilcox.assoc
