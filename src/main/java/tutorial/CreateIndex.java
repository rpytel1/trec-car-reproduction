package tutorial;

import java.io.IOException;

import static tutorial.LuceneConstants.DATA_DIR;
import static tutorial.LuceneConstants.INDEX_DIR;

public class CreateIndex {

    Indexer indexer;

    public static void main(String[] args) {
        SearchTester tester;
        try {
            ImprovedIndexer improvedIndexer = new ImprovedIndexer();
            improvedIndexer.generateIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createIndex() throws IOException {
        indexer = new Indexer(INDEX_DIR);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(DATA_DIR, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed + " File indexed, time taken: "
                + (endTime - startTime) + " ms");
    }
}
