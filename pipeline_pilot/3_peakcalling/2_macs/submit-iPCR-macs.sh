#!/bin/bash


set -e

mkdir -p logs



for sample in ApaLI_220_S10_R1_001 AsiSI_225_S11_R1_001 AsiSI_Probes_226_S12_R1_001 BpuEI_219_S9_R1_001 BpuEI_AsiSI_227_S13_R1_001 Uncut_228_S14_R1_001;
do
bash iPCR-macs.sh "${sample}"
done






