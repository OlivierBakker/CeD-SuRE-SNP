#!/bin/bash



grep "#" $1 > $2
grep -v "#" $1 | awk '{print $1"\t"$2"\t"$3"\t"$4"\t"$5"\t"$6"\t"$7"\t"$8}' >> $2
