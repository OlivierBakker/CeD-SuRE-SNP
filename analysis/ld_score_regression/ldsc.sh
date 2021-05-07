

mkdir -p ldsc_annotations
mkdir -p ldsc_ld_scores

# Make the annotations and partioned LD scores
for celltype in C Cs J Js K;
do

# Convert peaks to bed file
cat ../../pipeline/3_peakcalling/3_merge_peaks/output/ced_regions/${celltype}_x3_ced_regions_bmerged.narrowPeak | awk '{print "chr"$1"\t"$2-1"\t"$3-1}'  > data/${celltype}.bed


python ldsc/make_annot.py \
--bed-file data/${celltype}.bed \
--bimfile data/1000G_EUR_cm_annotated_immunochip_variants_only.bim \
--annot-file ldsc_annotations/${celltype}_ldsc_annotation.annot;


python ldsc/ldsc.py \
--l2 \
--bfile data/1000G_EUR_cm_annotated_immunochip_variants_only \
--ld-wind-cm 1 \
--annot ldsc_annotations/${celltype}_ldsc_annotation.annot \
--thin-annot \
--out ldsc_annotations/${celltype};

done;

# Baseline LDscores
python ldsc/ldsc.py \
--bfile data/1000G_EUR_cm_annotated_immunochip_variants_only \
--l2 \
--ld-wind-cm 1 \
--out ldsc_annotations/baseline_ldscores_immunochip_ponce


# Convert sumstats
python ldsc/munge_sumstats.py \
--N 27786 \
--N-cas 12953 \
--N-con 14833 \
--snp rsId37 \
--a1 A1 \
--a2 A2 \
--p P \
--out data/ponce_b37 \
--sumstats data/ponce_summary_stats_b37.txt

# Run
python ldsc/ldsc.py \
--h2 data/ponce_b37.sumstats.gz \
--ref-ld ldsc_annotations/baseline_ldscores_immunochip_ponce,ldsc_annotations/C,ldsc_annotations/Cs,ldsc_annotations/J,ldsc_annotations/Js,ldsc_annotations/K \
--w-ld ldsc_annotations/baseline_ldscores_immunochip_ponce \
--overlap-annot \
--frqfile data/1000G_EUR_cm_annotated_immunochip_variants_only \
--out ponce