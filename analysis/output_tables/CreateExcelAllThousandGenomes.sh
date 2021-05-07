java -jar ../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T CreateExcel \
-i /groups/umcg-wijmenga/tmp04/umcg-obbakker/data/reference/1000G/filtered/phase3/EUR/1000G_phase3_maf0.05.vcf.gz \
-o SuRE_CeD_all_1000G_EUR_maf05_25kb_ld08 \
--ld-reference /groups/umcg-wijmenga/tmp04/umcg-obbakker/data/reference/1000G/filtered/phase3/EUR/1000G_phase3 \
--ld-window 25000 \
--ld-threshold 0.8 \
-rf ../differential_peakcalling/SuRE-SNP_CeD_x3_filtered.consensus.peaks.narrowPeak \
-v Caco2=data/bundle/Caco2/Caco2.assoc.sure.annot \
-v Caco2=data/bundle/Caco2/Caco2.plus.assoc.sure.annot \
-v Caco2=data/bundle/Caco2/Caco2.minus.assoc.sure.annot \
-r Caco2=data/bundle/Caco2/Caco2_x3_ced_region_bmerged.sure.annot.bed \
-r Caco2=data/bundle/Caco2/Caco2.cCRE.sure.annot.bgz \
-v Caco2Stim=data/bundle/Caco2Stim/Caco2Stim.assoc.sure.annot \
-v Caco2Stim=data/bundle/Caco2Stim/Caco2Stim.plus.assoc.sure.annot \
-v Caco2Stim=data/bundle/Caco2Stim/Caco2Stim.minus.assoc.sure.annot \
-r Caco2Stim=data/bundle/Caco2Stim/Caco2Stim_x3_ced_region_bmerged.sure.annot.bed \
-r Caco2Stim=data/bundle/Caco2/Caco2.cCRE.sure.annot.bgz \
-v K562=data/bundle/K562/K562.assoc.sure.annot  \
-v K562=data/bundle/K562/K562.plus.assoc.sure.annot  \
-v K562=data/bundle/K562/K562.minus.assoc.sure.annot  \
-r K562=data/bundle/K562/K562_x3_ced_region_bmerged.sure.annot.bed \
-r K562=data/bundle/K562/K562.cCRE.sure.annot.bgz \
-v Jurkat=data/bundle/Jurkat/Jurkat.assoc.sure.annot \
-v Jurkat=data/bundle/Jurkat/Jurkat.plus.assoc.sure.annot \
-v Jurkat=data/bundle/Jurkat/Jurkat.minus.assoc.sure.annot \
-r Jurkat=data/bundle/Jurkat/Jurkat_x3_ced_region_bmerged.sure.annot.bed \
-r Jurkat=data/bundle/Jurkat/Jurkat.cCRE.sure.annot.bgz \
-v JurkatStim=data/bundle/JurkatStim/JurkatStim.assoc.sure.annot  \
-v JurkatStim=data/bundle/JurkatStim/JurkatStim.plus.assoc.sure.annot  \
-v JurkatStim=data/bundle/JurkatStim/JurkatStim.minus.assoc.sure.annot  \
-r JurkatStim=data/bundle/JurkatStim/JurkatStim_x3_ced_region_bmerged.sure.annot.bed \
-r JurkatStim=data/bundle/Jurkat/Jurkat.cCRE.sure.annot.bgz \
-v UseLdAllSheets=data/bundle/ponce.sure.annot \
-e data/bundle/ensembl_v102_b37.sure.annot \
-r AllSheets=data/bundle/eqtlgen_fdr_signif_with_sure_expression.sure.annot.bgz


#-r AllSheets=data/bundle/eqtlgen_fdr_signif.sure.annot.bgz

