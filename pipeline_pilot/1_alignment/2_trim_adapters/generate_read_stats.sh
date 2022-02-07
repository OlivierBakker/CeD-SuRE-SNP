#!/bin/bash



grep "Pairs written (passing filters):" logs/*.out | awk '{print $1"\t"$5"\t"$6}' | sed 's/\.out:Pairs//g' | sed 's/,//g' | sed 's/(//g' | sed 's/%)//g' > post_filter_readcounts.tsv
