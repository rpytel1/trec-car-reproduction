package tutorial.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class CustomAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
        TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());
        // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
        ts = new TokenStreamComponents(ts.getTokenizer(), new LowerCaseFilter(ts.getTokenStream()));
        ts = new TokenStreamComponents(ts.getTokenizer(), new KStemFilter(ts.getTokenStream()));
        return ts;
    }
}
