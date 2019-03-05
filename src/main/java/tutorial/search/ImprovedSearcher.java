package tutorial.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tutorial.QueryStruct;
import tutorial.utils.LuceneConstants;

import java.io.File;
import java.io.IOException;

public class ImprovedSearcher {
    IndexSearcher indexSearcher;
    int QUERY_LIMIT = LuceneConstants.CLASSIC_SEARCH_LIMIT;
    QueryParser parser;

    public ImprovedSearcher(String indexDirectoryPath, String similarityName) throws IOException {
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());
                ts = new TokenStreamComponents(ts.getTokenizer(), new LowerCaseFilter(ts.getTokenStream()));
                ts = new TokenStreamComponents(ts.getTokenizer(), new KStemFilter(ts.getTokenStream()));

                return ts;
            }
        };

        String field = "text"; // the field you hope to search for
        parser = new QueryParser(field, analyzer); // a query parser that transforms a text string into Lucene's query object

        // Okay, now let's open an index and search for documents
        Directory dir = FSDirectory.open(new File(indexDirectoryPath).toPath());
        IndexReader index = DirectoryReader.open(dir);
        indexSearcher = new IndexSearcher(index);
        configure(similarityName);
    }

    public ImprovedSearcher(String indexDirectoryPath, String similarityName, int requested) throws IOException {
        this(indexDirectoryPath, similarityName);
        QUERY_LIMIT = requested;
    }

    private void configure(String similarityName) {
        switch (similarityName) {
            case "tfidf": {
                ClassicSimilarity classicSimilarity = new ClassicSimilarity();
                indexSearcher.setSimilarity(classicSimilarity);
                break;
            }
            case "LTR": {
                QUERY_LIMIT = LuceneConstants.MAX_SEARCH;
            }
            default: {
                BM25Similarity bm25Similarity = new BM25Similarity();
                indexSearcher.setSimilarity(bm25Similarity);
            }
        }
    }

    public void search(QueryStruct queryStruct) throws ParseException, IOException {

        Query query = parser.parse(QueryParser.escape(queryStruct.getText()));


        TopDocs docs = indexSearcher.search(query, QUERY_LIMIT);

        int rank = 1;
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            int docid = scoreDoc.doc;
            double score = scoreDoc.score;
            Document document = indexSearcher.doc(scoreDoc.doc);

            System.out.println(queryStruct.getNumber()+" " + "Q0"+" "+ document.get("docno")+" "+rank + " " + score + " " + "lucene");
            rank++;
        }
    }
}
