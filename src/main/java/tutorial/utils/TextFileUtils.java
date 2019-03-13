package tutorial.utils;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

public class TextFileUtils {
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
