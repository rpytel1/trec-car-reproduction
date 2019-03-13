package tutorial.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tutorial.utils.CustomAnalyzer;
import tutorial.utils.TextFileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class implementing process of indexing the corpus
 */
public class ImprovedIndexer {

    public ImprovedIndexer() {

    }

    /**
     * Method performing indexing the corpus by extracting it's content from TRECTEXT files and creating
     * helpful fields in the index to later retrieve them in searching process
     *
     * @param indexDir
     * @param corpusDir
     * @throws IOException
     */
    public void generateIndex(String indexDir, String corpusDir) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir).toPath());

        Analyzer analyzer = new CustomAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter ixwriter = new IndexWriter(dir, config);

        FieldType fieldTypeMetadata = TextFileUtils.getFiledTypeMeta();

        FieldType fieldTypeText = TextFileUtils.getFieldTypeText();

        File[] files = new File(corpusDir).listFiles();

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

            while (matcher.find()) {

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
