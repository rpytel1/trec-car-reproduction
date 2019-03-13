#!/bin/bash
rm -rf ../index_lucene2
java -jar target/create-index.jar -indexDir="../index_lucene2" -corpusDir="hierarchical_test200_corpus_v2"
java -jar target/search-file-from-file.jar train_queries_article.json > runfile_paragraph_lucene
cd /Users/rafalpytel/Downloads/trec_eval.9.0
./trec_eval -q train.pages.cbor-hierarchical.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph_lucene > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/lucene/run_paragraph_hierarchical_l.eval
./trec_eval -q train.pages.cbor-toplevel.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph_lucene > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/lucene/run_paragraph_toplevel_l.eval
./trec_eval -q train.pages.cbor-article.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph_lucene > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/lucene/run_paragraph_article_l.eval
