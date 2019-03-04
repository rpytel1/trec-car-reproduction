package tutorial;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

import static tutorial.LuceneConstants.INDEX_DIR;

public class SearchTester {

    Searcher searcher;
//    String similarityName = "tfidf2";

    public static void main(String[] args) {
        SearchTester tester;
        try {
            tester = new SearchTester();
            if (args[0].startsWith("-query=")) {
                args[0] = args[0].substring(7);
                if (args[0].contains("/")) {
                    args[0] = args[0].replace("/", "//");
                }
            }
            if (args[1].startsWith("-type=")) {
                args[1] = args[1].substring(6);
            }
            tester.search(args[0], args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    private void search(String searchQuery, String similarityName) throws IOException, ParseException {
        searcher = new Searcher(INDEX_DIR, similarityName);
        long startTime = System.currentTimeMillis();

        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();
        System.out.println(hits.totalHits +
                " documents found. Time :" + (endTime - startTime));
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("File: "
                    + doc.get(LuceneConstants.FILE_PATH) + " Score: " + scoreDoc.score);
        }
    }
}