
# Make the reverse complement of the seqeunce
make.revcomp <- function(seq) {
    comp.table <- matrix(c("A", "T", "T", "A", "C", "G", "G", "C"), ncol=2, byrow=T)
    rownames(comp.table) <- comp.table[,1]

    comp <- c()
    i <-  1
    for (base in strsplit(seq, split="")[[1]]) {
        comp[i] <- comp.table[base, 2]
        i <- i+1
    }

    revcomp <- paste0(rev(comp), collapse="")

    return(revcomp)
}

# Is the given squence the revcomp of another seqeunce
is.revcomp <- function(seq1, seq2) {

    if (make.revcomp(seq1) == seq2) {
        return(T)
    } else {
        return(F)
    }

}

# Tests if both the sequence and the rev comp are unqiue
is.unique <- function(seq, pool) {

    for (index in pool) {
        if (index == seq) {
            return(F)
        }
        if (index == make.revcomp(seq)) {
            return(F)
        }
    }

    return(T)
}

# Tests if seq is unique and has more then n different bases
is.different <- function(seq, pool, n=2) {
    if (is.unique(seq, pool)) {
        for (index in pool) {
            # Check if the sequence has at least n unique chars
            if (sum(strsplit(seq, split="")[[1]] == strsplit(index, split="")[[1]]) > nchar(seq) - n) {
                return(F)
            }
            # Check if the revcomp of the sequence has at least n unique chars
            if (sum(strsplit(make.revcomp(seq), split="")[[1]] == strsplit(index, split="")[[1]]) > nchar(seq) - n) {
                return(F)
            }
        }
        return(T)
    } else {
        return(F)
    }
}

# Pool is the the set of unique 6mers with their reverse comp
pool      <- c("TCGAAG","CTTCGA", "GTTACT", "AGTAAC", "GTTGAC", "GTCAAC")
# The final size of the pool
max.count <- 80

for (i in 1:1000) {

    if (length(pool) >= max.count) {
        break
    }
    # Generate a random 6 nucleotide sequcne
    cur.sequence <- paste0(sample(c("A", "T", "C", "G"), size=6, replace=T), collapse="")

    # Check if the sequence and its revcomp is unique, if so add it to the pool
    if (is.different(cur.sequence, pool, n=2)) {
        pool <- c(pool, cur.sequence, make.revcomp(cur.sequence))
    }

}



