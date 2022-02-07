#!/bin/bash
#SBATCH --time=05:59:00
#SBATCH --mem=20G

set -e

ml Java

INPUT_DIR=$1
OUTPUT=$2
INPUT_FILES=""
IPCR_TOOLS="../../../pipeline/0_tools/IPCRTools-1.0-SNAPSHOT-all.jar"


# Generate the input CLA
for file in $INPUT_DIR/*.info.gz;
do
INPUT_FILES="$INPUT_FILES -i $file"
done


CMD="java -Xmx32G -jar ${IPCR_TOOLS} \
-T MakeBarcodeCounts \
-o ${OUTPUT} \
-z \
$INPUT_FILES"

echo $CMD
eval $CMD

#gzip $OUTPUT


# OLD
#for barcodefile in output/run_3/*.barcodes;
#do
#echo "processing $barcodefile"
#sort $barcodefile | uniq -c | awk '{print $1"\t"$2}' > $barcodefile.counts
#done
