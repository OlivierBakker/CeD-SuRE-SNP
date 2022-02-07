#!/bin/bash



while read sample;
do


echo $sample
sbatch CreateBarcodeComplexityCurve.sh $sample 


done < samples.txt
