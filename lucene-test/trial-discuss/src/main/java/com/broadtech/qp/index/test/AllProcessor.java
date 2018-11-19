package com.broadtech.qp.index.test;

import com.broadtech.bdp.common.ctl.CtlConfig;
import com.broadtech.bdp.common.util.GreatLogger;
import com.broadtech.bdp.common.util.TokenUtils;
import com.broadtech.qp.index.status.RuntimeStatus;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 仅构建索引, 不存储field<br>
 * {@link IndexOptions#DOCS} and stored
 */
public class AllProcessor extends BaseIndexProcessor {
    public AllProcessor(RuntimeStatus status, CtlConfig ctl) {
        super(status, ctl);
    }

    @Override
    protected String getBaseIndexPath() {
        return DataQueue.BASE_PATH + "/docsAndFreqAndPosAndOffsets";
    }

    @Override
    protected void indexDoc(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.fieldSep, ctl.fieldNames.size());
        Field field, storedField;
        String fieldName;
        Object fieldValue;

        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setTokenized(false);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

        Document doc = new Document();
        for (int i = 0; i < fields.size(); i++) {
            try {
                fieldValue = ctl.getConcreteFieldValue(fields.get(i), (short) i);
            } catch (Exception e) {
                fieldValue = null;
                GreatLogger.error(GreatLogger.Level.plain, this.getClass(), "indexDoc", "字段类型化失败", fields.get(i), e);
            }
            if (fieldValue == null) continue;
            fieldName = ctl.fieldNames.get(i).toLowerCase();
            storedField = null;
            switch (ctl.getFieldTypeByIndex(i).getSimpleName()) {
                case "Integer":
                case "Byte":
                case "Short":
                    field = new IntPoint(fieldName, ((Number) fieldValue).intValue());
                    storedField = new StoredField(fieldName, ((Number) fieldValue).intValue());
                    break;
                case "Long":
                    field = new LongPoint(fieldName, ((Number) fieldValue).longValue());
                    storedField = new StoredField(fieldName, ((Number) fieldValue).longValue());
                    break;
                case "Date":
                case "Timestamp":
                    field = new LongPoint(fieldName, ((Date) fieldValue).getTime());
                    storedField = new StoredField(fieldName, ((Date) fieldValue).getTime());
                    break;
                case "Float":
                    field = new FloatPoint(fieldName, ((Number) fieldValue).floatValue());
                    storedField = new StoredField(fieldName, ((Number) fieldValue).floatValue());
                    break;
                case "Double":
                    field = new DoublePoint(fieldName, ((Number) fieldValue).doubleValue());
                    storedField = new StoredField(fieldName, ((Number) fieldValue).doubleValue());
                    break;
                case "BigDecimal":
                    field = new BigIntegerPoint(fieldName, ((BigDecimal) fieldValue).toBigInteger());
                    storedField = new StoredField(fieldName, ((BigDecimal) fieldValue).doubleValue());
                    break;
                default:
                    field = new Field(fieldName, (String) fieldValue, fieldType);
                    break;
            }
            doc.add(field);
            if (storedField != null) doc.add(storedField);
        }
        indexWriter.addDocument(doc);
    }
}
