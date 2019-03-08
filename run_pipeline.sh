#!/bin/bash
rm -rf ../index_galago
alias galago='/Users/rafalpytel/Downloads/galago-3.15-bin/bin/galago'

galago build --indexPath=../index_galago --inputPath+hierarchical_test200_corpus_v2 --stemmer+krovetz --tokenizer/fields+docno --tokenizer/fields+text
galago batch-search test200_queries_trec_format.json > runfile_paragraph
cd /Users/rafalpytel/Downloads/trec_eval.9.0
./trec_eval -q train.pages.cbor-hierarchical.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/galago/run_paragraph_hierarchical.eval
./trec_eval -q train.pages.cbor-toplevel.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/galago/run_paragraph_toplevel.eval
./trec_eval -q train.pages.cbor-article.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/galago/run_paragraph_article.eval
