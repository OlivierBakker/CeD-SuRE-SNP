# CeD-SuRE-SNP
Pipeline and scripts for the survey of regulatory elements and snps on coeliac disease


# Repo structure
The repo is sub-divided into two parts `analysis` representing the downstream analysis of the SuRE data and `pipeline` respresenting the pipeline used to convert the SuRE seqeuncing data to a usable format.


`analysis` is likely not of much use, as this mostly contains bespoke R and bash scripts to generate and analyze the data for the manuscript

`pipeline` should hopefully be re-usable, but a substantial ammount of tweaking is probably required to get everything to run in your compute enviroment. 

For more details on `pipeline` and `analysis`, please see the respective folders README. Also see the wiki for a detailled walkthrough of the analysis steps. 

`pipeline/iPCR-tools` contains the java project which forms the backbone of the pipeline. This has implemented some tools that may be of use outside of only the SuRE scope.

# On pipeline maintance
I will likely not be maintaing this pipeline into the far future. That being said, I'll try to update the documentation the best I can and keep an eye on any issues raised here: https://github.com/OlivierBakker/CeD-SuRE-SNP/issues
