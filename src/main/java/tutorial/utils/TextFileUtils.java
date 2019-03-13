package tutorial.utils;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

/**
 * Class providing utils for Field Types used widely when creating fields during indexing process
 */
public class TextFileUtils {
    /**
     * Method retrieving text fieldType
     * @return
     */
    public static FieldType getFieldTypeText() {
        FieldType fieldTypeText = new FieldType();
        fieldTypeText.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        fieldTypeText.setStoreTermVectors(true);
        fieldTypeText.setStoreTermVectorPositions(true);
        fieldTypeText.setTokenized(true);
        fieldTypeText.setStored(true);
        fieldTypeText.freeze();
        return fieldTypeText;
    }

    /**
     * Method retrieving Metadate field type
     * @return
     */
    public static FieldType getFiledTypeMeta(){
        FieldType fieldTypeMetadata = new FieldType();
        fieldTypeMetadata.setOmitNorms(true);
        fieldTypeMetadata.setIndexOptions(IndexOptions.DOCS);
        fieldTypeMetadata.setStored(true);
        fieldTypeMetadata.setTokenized(false);
        fieldTypeMetadata.freeze();
        return fieldTypeMetadata;
    }
}
