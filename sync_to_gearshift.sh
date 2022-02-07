#!/bin/bash

rsync -rP \
--prune-empty-dirs \
--include='*.sh' \
--include='*.r' \
--include='*README.txt' \
--exclude='**backup**/' \
--include='*/' \
--exclude='*' \
./ airlock+gearshift:/groups/umcg-wijmenga/tmp01/projects/sure-snp/

