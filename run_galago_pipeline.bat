cd path\\to\\your\\galago\\bin\\directory\\galago-3.15-bin\\bin
call galago build --indexPath=path\\to\\put\\the\\index\\train_paragraph --inputPath+path\\to\\corpus\\train_big_corpus.trectext --stemmer+krovetz --tokenizer/fields+docno --tokenizer/fields+text
call galago batch-search path\\to\\queries\\file\\train_paragraph_exp1_article.json > path\\to\\save\\the\\runfile
cd path\\to\\trec_eval\\exe\\my_trec_eval
call trec_eval.exe -q path\\to\\qrels\\train.pages.cbor-hierarchical.qrels path\\to\\runfile > path\\to\\save\\eval\\file\\run_paragraph.eval
