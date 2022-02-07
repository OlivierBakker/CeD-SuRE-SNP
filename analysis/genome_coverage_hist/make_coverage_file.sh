#!/bin/bash

ml BEDTools

zcat ../../pipeline/3_peakcalling/1_recode_ipcr/output/genome_wide_regions/all_samples.ipcr.bed.gz | sed "s/\(.*\t.*\t.*\)\t/\1/g" | bedtools coverage -a hg19_1000kb_windows.bed -b stdin > sure_snp_ipcr_1000kb_window_coverage.bed


zcat ../../pipeline/3_peakcalling/1_recode_ipcr/output/ced_regions/K_B1_T1+K_B1_T2.ipcr.bed.gz | sed "s/\(.*\t.*\t.*\)\t/\1/g" | bedtools coverage -a hg19_1000kb_windows.bed -b stdin > sure_snp_ipcr_1000kb_window_coverage_ced_regions.bed

