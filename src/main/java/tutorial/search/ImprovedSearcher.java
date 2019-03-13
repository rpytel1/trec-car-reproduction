package tutorial.search;

import com.uttesh.exude.exception.InvalidDataException;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tutorial.QueryStruct;
import tutorial.queryexpansion.Rocchio;
import tutorial.utils.CustomAnalyzer;
import tutorial.utils.LuceneConstants;
import tutorial.utils.OwnTFIDFSimilarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ImprovedSearcher {
    IndexSearcher indexSearcher;
    int QUERY_LIMIT = LuceneConstants.CLASSIC_SEARCH_LIMIT;
    float K1;
    QueryParser parser;

    public ImprovedSearcher(String indexDirectoryPath, String similarityName) throws IOException {
        Analyzer analyzer = new CustomAnalyzer();

        String field = "text";
        parser = new QueryParser(field, analyzer);
        Directory dir = FSDirectory.open(new File(indexDirectoryPath).toPath());
        IndexReader index = DirectoryReader.open(dir);
        indexSearcher = new IndexSearcher(index);
        configure(similarityName);
    }

    public ImprovedSearcher(String indexDirectoryPath, String similarityName, int requested, float k1) throws IOException {
        this(indexDirectoryPath, similarityName);
        QUERY_LIMIT = requested;
        this.K1 = k1;
    }

    private void configure(String similarityName) {
        switch (similarityName) {
            case "tfidf": {
                OwnTFIDFSimilarity classicSimilarity = new OwnTFIDFSimilarity();
                indexSearcher.setSimilarity(classicSimilarity);
                break;
            }
            case "LTR": {
                QUERY_LIMIT = LuceneConstants.MAX_SEARCH;
            }
            default: {
                BM25Similarity bm25Similarity = new BM25Similarity(K1, 0.75f);
                indexSearcher.setSimilarity(bm25Similarity);
            }
        }
    }

    public List<QueryStruct> expandQueryRM1List(List<QueryStruct> queryStructList) throws IOException, InterruptedException {
        List<QueryStruct> toReturn = new ArrayList<>();
        Process proc;
        int maxI = (int) (Math.ceil(((double) queryStructList.size() / 20)));
        for (int step = 0; step < 20; step++) {
            StringBuilder superCommand = new StringBuilder("");
            int start = step * maxI;
            int end = (step + 1) * maxI;
            end = end > queryStructList.size() ? queryStructList.size() : end;

            for (int i = start; i < end; i++) {
                QueryStruct queryStruct = queryStructList.get(i);
                String queryText = queryStruct.getText();
                String command = "/Users/rafalpytel/Downloads/galago-3.15-bin/bin/galago get-rm-terms --query=\"#combine(" + queryText + ")\" --numTerms=10 --index=/Users/rafalpytel/Desktop/TU\\ Delft/Q3\\&Q4/Information\\ Retriveal/project/galago_test2";
                superCommand.append(command);
                if (i != end - 1) {
                    superCommand.append(" && ");
                }
            }
            proc = Runtime.getRuntime().exec(new String[]{"bash", "-c", superCommand.toString()});
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line;
            int j = 0;
            int k = start;
            StringBuilder extendedQuery = new StringBuilder(queryStructList.get(k).getText());
            while ((line = reader.readLine()) != null) {

                String term = line.split("\t")[0];
                extendedQuery.append(" " + term);
                j++;
                if (j % 10 == 0) {
                    QueryStruct toAdd = queryStructList.get(k);
                    toAdd.setText(extendedQuery.toString());
                    toReturn.add(toAdd);
                    k++;
                    if (k < queryStructList.size() - 1)
                        extendedQuery = new StringBuilder(queryStructList.get(k).getText());
                }
            }
            proc.waitFor();
        }
        return toReturn;
    }

    public QueryStruct expandQueryRCH(QueryStruct queryStruct) throws ParseException, IOException, InvalidDataException {
        Rocchio rocchio = new Rocchio(1.0f, 0.8f);

        Query query = parser.parse(QueryParser.escape(queryStruct.getText()));

        TopDocs docs = indexSearcher.search(query, 100);
        List<Document> relevantDocs = Arrays.stream(docs.scoreDocs).map(m -> {
            try {
                return indexSearcher.doc(m.doc);
            } catch (IOException e) {
                e.printStackTrace();
                return new Document();
            }
        }).collect(Collectors.toList());
        return rocchio.expand(queryStruct, relevantDocs);
    }

    public QueryStruct expandQuery(QueryStruct queryStruct, String type) throws IOException, InterruptedException, ParseException, InvalidDataException {
        switch (type) {
            case "rch": {
                return expandQueryRCH(queryStruct);
            }
            default: {
                return queryStruct;
            }
        }
    }

    public void search(QueryStruct queryStruct, String mode) throws ParseException, IOException, InterruptedException, InvalidDataException {
        QueryStruct extendedQueryStruct = expandQuery(queryStruct, mode);
        Query query = parser.parse(QueryParser.escape(extendedQueryStruct.getText()));
        TopDocs docs = indexSearcher.search(query, QUERY_LIMIT);

        int rank = 1;
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            double score = scoreDoc.score;
            Document document = indexSearcher.doc(scoreDoc.doc);

            System.out.println(queryStruct.getNumber() + " " + "Q0" + " " + document.get("docno") + " " + rank + " " + score + " " + "lucene");
            rank++;
        }
    }
}
