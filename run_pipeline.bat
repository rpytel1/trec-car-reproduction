cd C:\\Users\\Vasilis\\Documents\\galago-3.15-bin\\bin
call galago build --indexPath=C:\\Users\\Vasilis\\Documents\\galagoTrials\\test200_trial_paragraph --inputPath+C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\paragraph_corpus_test200.trectext --stemmer+krovetz --tokenizer/fields+docno --tokenizer/fields+text
call galago batch-search C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\test200_queries_trec_format.json > C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\runfile_paragraph
cd C:\\Users\\Vasilis\\Documents\\my_trec_eval
call trec_eval.exe -q C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\test200-train\\train.pages.cbor-hierarchical.qrels C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\runfile_paragraph > C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\run_paragraph_hierarchical.eval
call trec_eval.exe -q C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\test200-train\\train.pages.cbor-toplevel.qrels C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\runfile_paragraph > C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\run_paragraph_toplevel.eval
call trec_eval.exe -q C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\test200-train\\train.pages.cbor-article.qrels C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\runfile_paragraph > C:\\Users\\Vasilis\\PycharmProjects\\firstTrialsIR\\run_paragraph_article.eval
