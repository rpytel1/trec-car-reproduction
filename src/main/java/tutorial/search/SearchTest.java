package tutorial.search;

import com.uttesh.exude.exception.InvalidDataException;
import org.apache.lucene.queryparser.classic.ParseException;
import tutorial.QueryStruct;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchTest {
    public static void main(String[] args) throws IOException, ParseException, InvalidDataException, InterruptedException {


        List<QueryStruct> queries = new ArrayList<>();

        for (int i = 0; i < 13; i++) {
            queries.add(new QueryStruct("1", "greek god"));
            queries.add(new QueryStruct("2", "ass"));
            queries.add(new QueryStruct("3", "bros"));
        }
        ImprovedSearcher improvedSearcher = new ImprovedSearcher("/Users/rafalpytel/Desktop/TU Delft/Q3&Q4/Information Retriveal/project/trec-car-reproduction/index_Dir", "tfidf", 10, 1.2f);
        List<QueryStruct> extendedQueries = expandQueries(queries, improvedSearcher, "rch");
        for (QueryStruct query : extendedQueries) {
            improvedSearcher.search(query, "rch");
        }
    }

    static public List<QueryStruct> expandQueries(List<QueryStruct> queries, ImprovedSearcher improvedSearcher, String queryMode) throws IOException, InterruptedException {
        if (queryMode.equals("rm1")) {
            return improvedSearcher.expandQueryRM1List(queries);
        }
        return queries;
    }
}
