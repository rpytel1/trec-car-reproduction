package tutorial.search;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;

import static tutorial.utils.LuceneConstants.INDEX_DIR;

public class SearchTester {


    public static void main(String[] args) {
        try {
            if (args[0].startsWith("-query=")) {
                args[0] = args[0].substring(7);
                if (args[0].contains("/")) {
                    args[0] = args[0].replace("/", "//");
                }
            }
            if (args[1].startsWith("-type=")) {
                args[1] = args[1].substring(6);
            }
            ImprovedSearcher improvedSearcher = new ImprovedSearcher(INDEX_DIR, args[1]);
//            improvedSearcher.search(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}