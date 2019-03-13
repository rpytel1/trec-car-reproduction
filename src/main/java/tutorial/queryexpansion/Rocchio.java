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
 * This is implementation of Rocchio algorithm according to modern manner without consideration of non relevant documents (no gamma parameter)
 */
public class Rocchio {

    private float alpha;

    private float beta;
    private int termLimit = 10;
    private String targetField = "text";

    public Rocchio(float alpha, float beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Method performing query expansion using Rocchio algorithm
     * @param queryStruct query to be expanded
     * @param relevantDocuments list of documents which are relevant to the query
     * @return expanded query
     * @throws IOException
     * @throws InvalidDataException
     */
    public QueryStruct expand(QueryStruct queryStruct, List<Document> relevantDocuments) throws IOException, InvalidDataException {
        String query = queryStruct.getText();
        Set<String> queryTerms = new HashSet<>(Arrays.asList(query.split(" ")));

        Directory index = generateRelevantDirectory(relevantDocuments, 20);

        Map<String, Float> allTermFreq = extractTermFrequency(index);
        Map<String, Float> queryTermFreq = getTFIDF(index, queryTerms, targetField);


        for (Map.Entry<String, Float> term : queryTermFreq.entrySet()) {
            float modifiedFreq = term.getValue() * alpha;

            if (allTermFreq.containsKey(term.getKey()))
                modifiedFreq += allTermFreq.get(term.getKey());

            allTermFreq.put(term.getKey(), modifiedFreq);
        }

        List<Map.Entry<String, Float>> finalQueryTerms = new ArrayList<>(allTermFreq.entrySet());
        finalQueryTerms.sort(Comparator.comparing(Map.Entry::getValue));
        Collections.reverse(finalQueryTerms);

        StringBuilder queryString = new StringBuilder();

        for (Map.Entry<String, Float> entry : finalQueryTerms.subList(0, finalQueryTerms.size() > (termLimit + 1) ? termLimit + 1 : finalQueryTerms.size()))
            queryString.append(entry.getKey()).append(' ');

        queryStruct.setText(queryString.toString());

        return queryStruct;
    }

    /**
     * Method extracting frequency for each word from the previously created index
     * @param directory index
     * @return dictionary of words and their corresponding frequencies
     * @throws IOException
     */
    private Map<String, Float> extractTermFrequency(Directory directory) throws IOException {
        Map<String, Float> frequencyMap = new HashMap<>();

        IndexReader reader = DirectoryReader.open(directory);
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(targetField);
        TermsEnum termsEnum = terms.iterator();

        int docNumber = reader.numDocs();

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

    /**
     * Method creating index for relevant documents. Index is later used to retrieve characteristic of documments(e.g. term frequency)
     * @param relevantDocuments
     * @param documentLimit
     * @return
     * @throws IOException
     * @throws InvalidDataException
     */
    public static Directory generateRelevantDirectory(List<Document> relevantDocuments, int documentLimit) throws IOException, InvalidDataException {

        Analyzer analyzer = new CustomAnalyzer();

        Directory index = new RAMDirectory();
        IndexWriter indexWriter = new IndexWriter(index, new IndexWriterConfig(analyzer));

        FieldType fieldTypeText = TextFileUtils.getFieldTypeText();

        for (int i = 0; i < relevantDocuments.size() && i < documentLimit; ++i) {
            Document doc = relevantDocuments.get(i);
            String text = relevantDocuments.get(i).get("text");
            String newText = ExudeData.getInstance().filterStoppings(text);
            doc.removeField("text");
            doc.add(new Field("text", newText, fieldTypeText));
            indexWriter.addDocument(doc);
        }

        indexWriter.close();
        return indexWriter.getDirectory();
    }

    /**
     * Method retrieving TFIDF map for all corresponding words in index provided
     * @param directory index of all relevant documents
     * @param tokens
     * @param targetField
     * @return
     * @throws IOException
     */
    public static Map<String, Float> getTFIDF(Directory directory, Set<String> tokens, String targetField) throws IOException {
        Map<String, Float> frequencyMap = new HashMap<>();

        IndexReader reader = DirectoryReader.open(directory);
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(targetField);
        TermsEnum termsEnum = terms.iterator();

        int docNumber = reader.numDocs();

        ClassicSimilarity similarity = new ClassicSimilarity();

        while (termsEnum.next() != null)
            if (tokens.contains(termsEnum.term().utf8ToString())) {
                Term term = new Term(targetField, termsEnum.term().utf8ToString());
                int docFreq = reader.docFreq(term);
                long termFreq = reader.totalTermFreq(term);

                frequencyMap.put(term.text(), termFreq * similarity.idf(docFreq, docNumber));
            }

        reader.close();

        return frequencyMap;
    }

}
