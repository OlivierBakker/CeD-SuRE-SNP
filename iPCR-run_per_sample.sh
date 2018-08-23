#!/bin/bash


# Sequence of execution:
# A1. iPCR-split.
# A2. iPCR-bwa_mem.sh                         
# A3. iPCR-bwa_mem_stats.sh (OPTIONAL)        
# A4. iPCR-extract_target_regions.sh
# A5. iPCR-mark_duplicates.sh (OPTIONAL)
# A6. iPCR-base_recalibrator.sh
# A7. iPCR-haplotypecaller
# B1. iPCR-merge_bam_and_barcodes.sh (depends on A1, A4-A5)

containsElement () {
  local e match="$1"
  shift
  for e; do [[ "$e" == "$match" ]] && return 0; done
  return 1
}

# Pipeline script for the per sample portion of the SuRE-SNP pipeline
STEPS=("A1" "A2" "A3" "A4" "A5" "A6" "A7" "B1")
SAMPLE="Sample001"
WORK_DIR="./"
GATK_BUNDLE=""
PIPELINE_ROOT=""
ID_LOG=""

# Step A1
if [ $(containsElement "A1" $STEPS) == 1 ]; then

    # IO definitions
    BARCODE_INFO=""
	cur_dir="${WORK_DIR}/iPCR_split"
	mkdir -p ${cur_dir}

    A1_id=$(sbatch ${PIPELINE_ROOT}/iPCR/iPCR-split.sh)
fi

# Step A2
if [ $(containsElement "A2" $STEPS) == 1 ]; then
	cur_dir="${WORK_DIR}/iPCR_bwa_mem"
	mkdir -p ${cur_dir}

    A2_id=$(sbatch --dependency=afterok:${A1_id} ${PIPELINE_ROOT}/iPCR/iPCR-split.sh)
fi

# Step A3
if [ $(containsElement "A3" $STEPS) == 1 ]; then
	cur_dir="${WORK_DIR}/iPCR_bwa_mem_stats"
	mkdir -p ${cur_dir}

	A3_id=$(sbatch --dependency=afterok:${A2_id} ${PIPELINE_ROOT}/iPCR/iPCR-split.sh)
fi

# Step A4
if [ $(containsElement "A4" $STEPS) == 1 ]; then
	cur_dir="${WORK_DIR}/iPCR_extract_target_regions"
	mkdir -p ${cur_dir}

    FINAL_BAM=""

    A4_id=$(sbatch --dependency=afterok:${A3_id} ${PIPELINE_ROOT}/iPCR/iPCR-split.sh)
fi

# Step A5
if [ $(containsElement "A5" $STEPS) == 1 ]; then
	cur_dir="${WORK_DIR}/iPCR_mark_duplicates"
	mkdir -p ${cur_dir}


   FINAL_BAM=""

   A5_id=$(sbatch --dependency=afterok:${A4_id} ${PIPELINE_ROOT}/iPCR/iPCR-split.sh)
fi

# Step A6
if [ $(containsElement "A6" $STEPS) == 1 ]; then
	cur_dir="${WORK_DIR}/iPCR_base_recalibrator"
	mkdir -p ${cur_dir}

   A6_id=$(sbatch --dependency=afterok:${A5_id} ${PIPELINE_ROOT}/iPCR/iPCR-split.sh)
fi

# Step A7
if [ $(containsElement "A7" $STEPS) == 1 ]; then
	cur_dir="${WORK_DIR}/iPCR_haplotypecaller"
	mkdir -p ${cur_dir}

   A7_id=$(sbatch --dependency=afterok:${A6_id} ${PIPELINE_ROOT}/iPCR/iPCR-split.sh)
fi

# Step B1
if [ $(containsElement "B1" $STEPS) == 1 ]; then
    cur_dir="${WORK_DIR}/iPCR-merge_bam_and_barcodes"
	mkdir -p ${cur_dir}

	echo $FINAL_BAM
	echo $BARCODE_INFO

   B1_id=$(sbatch --dependency=afterok:${A1_id}:${A4_id} ${PIPELINE_ROOT}/iPCR/iPCR-split.sh)
fi
