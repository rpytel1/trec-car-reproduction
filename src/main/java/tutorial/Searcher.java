package tutorial;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {

    IndexSearcher indexSearcher;
    QueryParser queryParser;
    Query query;
    int QUERY_LIMIT = LuceneConstants.CLASSIC_SEARCH_LIMIT;

    public Searcher(String indexDirectoryPath, String similarityName)
            throws IOException {
        Directory indexDirectory =
                FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(reader);
        configure(similarityName);
        queryParser = new QueryParser(LuceneConstants.CONTENTS,
                new StandardAnalyzer());
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
            default:{
                BM25Similarity bm25Similarity = new BM25Similarity();
                indexSearcher.setSimilarity(bm25Similarity);
            }
        }
    }

    public TopDocs search(String searchQuery)
            throws IOException, ParseException {
        query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, QUERY_LIMIT);
    }

    public Document getDocument(ScoreDoc scoreDoc)
            throws CorruptIndexException, IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

}