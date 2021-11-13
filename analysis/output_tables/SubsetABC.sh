#!/bin/bash

ml HTSlib

set -e

zcat data/bundle/AllPredictions.AvgHiC.ABC0.015.minus150.ForABCPaperV3.txt.gz | head -n 1 |  awk '{print $1"\t"$2"\t"$3"\t"$7"\t"$21"\t"$24}' > $2.tmp
zcat data/bundle/AllPredictions.AvgHiC.ABC0.015.minus150.ForABCPaperV3.txt.gz | grep $1 |  awk '{print $1"\t"$2"\t"$3"\t"$7"\t"$21"\t"$24}' | sed 's/chr//g' | sort -k1,1V -k2,2n >> $2.tmp
bgzip -c $2.tmp > $2.bgz
rm $2.tmp
tabix -s 1 -b 2 -e 3 --skip-lines 1 $2.bgz
