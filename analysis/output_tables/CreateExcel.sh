ml Java
#- lege kolom zonder header geeft warning
#- Lege regels aan het eind


java -jar ../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T CreateExcel \
-i data/bundle/SuRE_CeD.all.sitesonly.vcf.gz \
-o SuRE_CeD_ase_results_2020-12-11 \
--ld-reference /groups/umcg-wijmenga/tmp04/umcg-obbakker/data/reference/1000G/filtered/phase3/EUR/1000G_phase3 \
--ld-window 250000 \
--ld-threshold 0.8 \
-rf data/bundle/CeDRegionV4.bed \
-v Caco2=data/bundle/Caco2/Caco2.assoc.sure.annot \
-r Caco2=data/bundle/Caco2/Caco2_x3_ced_region_bmerged.sure.annot.bed \
-r Caco2=data/bundle/Caco2/Caco2.cCRE.sure.annot.bgz \
-v Caco2Stim=data/bundle/Caco2Stim/Caco2Stim.assoc.sure.annot \
-r Caco2Stim=data/bundle/Caco2Stim/Caco2Stim_x3_ced_region_bmerged.sure.annot.bed \
-r Caco2Stim=data/bundle/Caco2/Caco2.cCRE.sure.annot.bgz \
-v K562=data/bundle/K562/K562.assoc.sure.annot  \
-r K562=data/bundle/K562/K562_x3_ced_region_bmerged.sure.annot.bed \
-r K562=data/bundle/K562/K562.cCRE.sure.annot.bgz \
-v Jurkat=data/bundle/Jurkat/Jurkat.assoc.sure.annot  \
-r Jurkat=data/bundle/Jurkat/Jurkat_x3_ced_region_bmerged.sure.annot.bed \
-r Jurkat=data/bundle/Jurkat/Jurkat.cCRE.sure.annot.bgz \
-v JurkatStim=data/bundle/JurkatStim/JurkatStim.assoc.sure.annot  \
-r JurkatStim=data/bundle/JurkatStim/JurkatStim_x3_ced_region_bmerged.sure.annot.bed \
-r JurkatStim=data/bundle/Jurkat/Jurkat.cCRE.sure.annot.bgz \
-v UseLdAllSheets=data/bundle/trynka.sure.annot \
-r AllSheets=data/bundle/ensembl.bed

-r AllSheets=data/bundle/JASPAR_2020_hg19_filtered.bed.bgz \
-r AllSheets=data/bundle/encode/all_celltypes_encode3_chipseq_tfbs.sorted.collapsed.bgz



java -Xmx12G -jar ../../0_tools/IPCRTools-1.0-SNAPSHOT-all.jar \
-T Recode \
-t MACS \
-o output/ced_regions/J_B2_T1+J_B2_T2 \
-z \
-f 'SAMPLE_BC_GT_EQ;500:TRUE:J_B2_T1+J_B2_T2' \
-k IPCR_INDEXED \
-rf ../../0_data/CeDRegionV4.bed \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_10.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_11.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_12.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_13.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_14.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_15.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_16.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_18.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_19.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_1.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_20.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_21.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_22.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_23.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_24.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_25.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_26.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_27.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_28.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_29.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_2.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_30.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_31.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_3.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_4.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_5.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_6.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_7.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_8.ipcr.bgz \
-i ../../2_make_ipcr_files/2_merge_with_barcodes/output/collapsed_v3/SuRE_9.ipcr.bgz \
-s J_B2_T1 -s J_B2_T2
