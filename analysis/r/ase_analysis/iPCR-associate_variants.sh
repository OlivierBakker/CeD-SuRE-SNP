#!/bin/bash
#SBATCH --cpus-per-task=22
#SBATCH --mem=64G
#SBATCH --time=05:59:00


ml RPlus

Rscript iPCR-associate_variants.r
