

for file in data/bundle/JASPAR/hg19/*.tsv.gz;
do

echo $file;
sbatch FilterJasparTfs.sh $file

done
