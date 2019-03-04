package tutorial;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tutorial.LuceneConstants.DATA_DIR;
import static tutorial.LuceneConstants.INDEX_DIR;


public class ImprovedIndexer {

    public ImprovedIndexer() {

    }

    public void generateIndex() throws IOException {
        Directory dir = FSDirectory.open(new File(INDEX_DIR).toPath());

// Analyzer specifies options for text processing
        Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                TokenStreamComponents ts = new TokenStreamComponents(new StandardTokenizer());
                // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
                ts = new TokenStreamComponents(ts.getTokenizer(), new LowerCaseFilter(ts.getTokenStream()));
                ts = new TokenStreamComponents( ts.getTokenizer(), new KStemFilter( ts.getTokenStream() ) );
                return ts;
            }
        };

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter ixwriter = new IndexWriter(dir, config);

// This is the field setting for metadata field.
        FieldType fieldTypeMetadata = new FieldType();
        fieldTypeMetadata.setOmitNorms(true);
        fieldTypeMetadata.setIndexOptions(IndexOptions.DOCS);
        fieldTypeMetadata.setStored(true);
        fieldTypeMetadata.setTokenized(false);
        fieldTypeMetadata.freeze();

// This is the field setting for normal text field.
        FieldType fieldTypeText = new FieldType();
        fieldTypeText.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        fieldTypeText.setStoreTermVectors(true);
        fieldTypeText.setStoreTermVectorPositions(true);
        fieldTypeText.setTokenized(true);
        fieldTypeText.setStored(true);
        fieldTypeText.freeze();


        File[] files = new File(DATA_DIR).listFiles();

        for (File file : files) {
            InputStream instream = new FileInputStream(file);
            Scanner s = new Scanner(instream).useDelimiter("\\A");
            String corpusText = s.hasNext() ? s.next() : "";
            instream.close();

            Pattern pattern = Pattern.compile(
                    "<DOC>.+?<DOCNO>(.+?)</DOCNO>.+?<HEADLINE>(.+?)</HEADLINE>.+?<TEXT>(.+?)</TEXT>.+?</DOC>",
                    Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL
            );

            Matcher matcher = pattern.matcher(corpusText);

            while (matcher.find())

            {

                String docno = matcher.group(1).trim();
                String headline = matcher.group(2).trim();
                String text = matcher.group(3).trim();


                Document d = new Document();
                d.add(new Field("docno", docno, fieldTypeMetadata));
                d.add(new Field("title", headline, fieldTypeText));
                d.add(new Field("text", text, fieldTypeText));
                ixwriter.addDocument(d);
            }
        }
        System.out.println("Number of indexed docs: " + ixwriter.numDocs());
        ixwriter.close();
        dir.close();
    }
}
