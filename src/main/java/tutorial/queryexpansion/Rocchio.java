package tutorial.queryexpansion;

import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import tutorial.QueryStruct;
import tutorial.utils.CustomAnalyzer;
import tutorial.utils.TextFileUtils;

import java.io.IOException;
import java.util.*;

/**
 * This is a modern version of Rocchio which does not make use of the gamma (considering the (ona average) large size of irrelevant documents, this is probably a good idea).
 */
public class Rocchio {
    /**
     * Alpha weight (see the Rocchio algorithm)
     */
    private float alpha;
    /**
     * Beta weight (see the Rocchio algorithm)
     */
    private float beta;
    /**
     * Maximal number of terms of the new expanded query
     */
    private int termLimit = 10;
    /**
     * Maximal number of documents to be considered in the relevant index
     */
    private int documentLimit;
    /**
     * The target field for the search operation
     */
    private String targetField = "text";

    /**
     * The query builder, used to build the Lucene query for the expanded plain query
     */

    public Rocchio(float alpha, float beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public QueryStruct expand(QueryStruct queryStruct, List<Document> relevantDocuments) throws IOException, InvalidDataException {
        /* Get the set of words for the query */
        String query = queryStruct.getText();
        Set<String> queryTerms = new HashSet<>(Arrays.asList(query.split(" ")));

        /* Get the frequency maps */
        Directory index = generateRelevantDirectory(relevantDocuments, 10);

        /* This (or part of this) will be the query vector */
        Map<String, Float> allTermFreq = extractTermFrequency(index);
        Map<String, Float> queryTermFreq = getTFIDF(index, queryTerms, targetField);


        for (Map.Entry<String, Float> term : queryTermFreq.entrySet()) {
            /* Multiply the current entry with alpha */
            float modifiedFreq = term.getValue() * alpha;

            /* Check if the entry exists in the allTermFreq; if it does extract the beta * TF-IDF */
            if (allTermFreq.containsKey(term.getKey()))
                modifiedFreq += allTermFreq.get(term.getKey());

            /* Update or introduce the term in the map */
            allTermFreq.put(term.getKey(), modifiedFreq);
        }

        List<Map.Entry<String, Float>> finalQueryTerms = new ArrayList<>(allTermFreq.entrySet());
        finalQueryTerms.sort(Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(finalQueryTerms);

        StringBuilder queryString = new StringBuilder();

        for (Map.Entry<String, Float> entry : finalQueryTerms.subList(0, finalQueryTerms.size() > (termLimit + 1) ? termLimit + 1 : finalQueryTerms.size()))
            queryString.append(entry.getKey()).append(' ');

        System.out.println(queryString.toString());
        queryStruct.setText(queryStruct.getText() + " " + queryString.toString());

        return queryStruct;
    }


    private Map<String, Float> extractTermFrequency(Directory directory) throws IOException {
        /* Declare the Map */
        Map<String, Float> frequencyMap = new HashMap<>();

        /* Get the target field terms */
        IndexReader reader = DirectoryReader.open(directory);
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(targetField);
        TermsEnum termsEnum = terms.iterator();

        /* Get the number of documents */
        int docNumber = reader.numDocs();

        /* Declare the similarity which will allow us to compute the IDF */
        ClassicSimilarity similarity = new ClassicSimilarity();

        while (termsEnum.next() != null) {
            Term term = new Term(targetField, termsEnum.term().utf8ToString());
            int docFreq = reader.docFreq(term);
            long termFreq = reader.totalTermFreq(term);

            /* Compute the TF-IDF * beta */
            frequencyMap.put(term.text(), beta * termFreq * similarity.idf(docFreq, docNumber));
        }


        /* Close the reader */
        reader.close();

        return frequencyMap;
    }

    public static Directory generateRelevantDirectory(List<Document> relevantDocuments, int documentLimit) throws IOException, InvalidDataException {

        Analyzer analyzer = new CustomAnalyzer();
        /* In-memory */
        Directory index = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(index, new IndexWriterConfig(analyzer));

        FieldType fieldTypeText = TextFileUtils.getFieldTypeText();
        /* Add the docs to the index */
        for (int i = 0; i < relevantDocuments.size() && i < documentLimit; ++i) {
            Document doc = relevantDocuments.get(i);
            String text = relevantDocuments.get(i).get("text");
            String newText = ExudeData.getInstance().filterStoppings(text);
            doc.removeField("text");
            doc.add(new Field("text", newText, fieldTypeText));
            indexWriter.addDocument(doc);
        }

        /* Store the documents, and finalize the indexing process */
        indexWriter.close();
        return indexWriter.getDirectory();
    }

    public static Map<String, Float> getTFIDF(Directory directory, Set<String> tokens, String targetField) throws IOException {
        /* Declare the Map */
        Map<String, Float> frequencyMap = new HashMap<>();

        /* Get the target field terms */
        IndexReader reader = DirectoryReader.open(directory);
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(targetField);
        TermsEnum termsEnum = terms.iterator();

        /* Get the number of documents */
        int docNumber = reader.numDocs();

        /* Declare the similarity */
        ClassicSimilarity similarity = new ClassicSimilarity();

        while (termsEnum.next() != null)
            if (tokens.contains(termsEnum.term().utf8ToString())) {
                Term term = new Term(targetField, termsEnum.term().utf8ToString());
                int docFreq = reader.docFreq(term);
                long termFreq = reader.totalTermFreq(term);

                /* Compute the TF-IDF */
                frequencyMap.put(term.text(), termFreq * similarity.idf(docFreq, docNumber));
            }

        /* Close the reader */
        reader.close();

        return frequencyMap;
    }

}
