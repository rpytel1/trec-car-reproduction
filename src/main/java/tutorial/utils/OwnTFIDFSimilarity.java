package tutorial.utils;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

/**
 * Our implementation of Cosine Similarity ranking function.
 */
public class OwnTFIDFSimilarity extends SimilarityBase {

    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {
        double tf = 1 + (Math.log(termFreq) / Math.log(2));
        double idf = Math.log((stats.getNumberOfDocuments() + 1) / stats.getDocFreq()) / Math.log(2);
        float dotProduct = (float) (tf * idf);
        return dotProduct;
    }

    @Override
    public String toString() {
        return null;
    }

}