package com.broadtech.qp.index.test;

import com.broadtech.bdp.common.ctl.CtlConfig;
import com.broadtech.bdp.common.util.GreatLogger;
import com.broadtech.bdp.common.util.TokenUtils;
import com.broadtech.qp.index.status.RuntimeStatus;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 仅构建索引, 不存储field<br>
 * Only documents are indexed: term frequencies and positions are omitted.
 * Phrase and other positional queries on the field will throw an exception, and scoring
 * will behave as if any term in the document appears only once.
 */
public class DocsIndexProcessor extends BaseIndexProcessor {
    public DocsIndexProcessor(RuntimeStatus status, CtlConfig ctl) {
        super(status, ctl);
    }

    @Override
    protected String getBaseIndexPath() {
        return DataQueue.BASE_PATH + "/docs";
    }

    @Override
    protected void indexDoc(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.fieldSep, ctl.fieldNames.size());
        Field field;
        String fieldName;
        Object fieldValue;
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
            switch (ctl.getFieldTypeByIndex(i).getSimpleName()) {
                case "Integer":
                case "Byte":
                case "Short":
                    field = new IntPoint(fieldName, ((Number) fieldValue).intValue());
                    break;
                case "Long":
                    field = new LongPoint(fieldName, ((Number) fieldValue).longValue());
                    break;
                case "Date":
                case "Timestamp":
                    field = new LongPoint(fieldName, ((Date) fieldValue).getTime());
                    break;
                case "Float":
                    field = new FloatPoint(fieldName, ((Number) fieldValue).floatValue());
                    break;
                case "Double":
                    field = new DoublePoint(fieldName, ((Number) fieldValue).doubleValue());
                    break;
                case "BigDecimal":
                    field = new BigIntegerPoint(fieldName, ((BigDecimal) fieldValue).toBigInteger());
                    break;
                default: // StringField
                    field = new StringField(fieldName, (String) fieldValue, Field.Store.NO);
                    break;
            }
            doc.add(field);
        }
        indexWriter.addDocument(doc);
    }
}
