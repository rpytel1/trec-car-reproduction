#!/bin/bash
rm -rf ../index_lucene
java -jar target/create-index.jar -indexDir="../index_lucene" -corpusDir="../trec_full_corpus"
java -jar target/search-file-from-file.jar test200_queries_trec_format_lucene.json > runfile_paragraph_lucene
cd /Users/rafalpytel/Downloads/trec_eval.9.0
./trec_eval -q train.pages.cbor-hierarchical.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph_lucene > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/lucene/run_paragraph_hierarchical_l.eval
./trec_eval -q train.pages.cbor-toplevel.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph_lucene > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/lucene/run_paragraph_toplevel_l.eval
./trec_eval -q train.pages.cbor-article.qrels ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/runfile_paragraph_lucene > ~/Desktop/TU\ Delft/Q3\&Q4/Information\ Retriveal/project/trec-car-reproduction/results/lucene/run_paragraph_article_l.eval
