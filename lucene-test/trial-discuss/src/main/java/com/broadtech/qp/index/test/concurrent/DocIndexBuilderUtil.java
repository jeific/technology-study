package com.broadtech.qp.index.test.concurrent;

import com.broadtech.bdp.api.ICallback;
import com.broadtech.bdp.common.ctl.CtlConfig;
import com.broadtech.bdp.common.util.GreatLogger;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.TokenUtils;
import com.broadtech.qp.index.ResourcesUtil;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;

/**
 * Created by jeifi on 2017/7/25.
 */
public class DocIndexBuilderUtil {
    private final static Logger logger = Logger.getLogger(DocIndexBuilderUtil.class);

    /**
     * Only documents are indexed: term frequencies and positions are omitted.
     * Phrase and other positional queries on the field will throw an exception, and scoring
     * will behave as if any term in the document appears only once.
     */
    public static void onlyDocumentsIndexed(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
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
                GreatLogger.error(GreatLogger.Level.plain, DocIndexBuilderUtil.class, "OnlyDocumentsIndexed", "字段类型化失败", fields.get(i), e);
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

    /**
     * {@link org.apache.lucene.index.IndexOptions#DOCS} and stored
     */
    public static void docIndexedAndStored(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.fieldSep, ctl.fieldNames.size());
        Field field, storedField;
        String fieldName;
        Object fieldValue;
        Document doc = new Document();
        for (int i = 0; i < fields.size(); i++) {
            try {
                fieldValue = ctl.getConcreteFieldValue(fields.get(i), (short) i);
            } catch (Exception e) {
                fieldValue = null;
                GreatLogger.error(GreatLogger.Level.plain, DocIndexBuilderUtil.class, "docIndexedAndStored", "字段类型化失败", fields.get(i), e);
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
                default: // StringField
                    field = new StringField(fieldName, (String) fieldValue, Field.Store.YES);
                    break;
            }
            doc.add(field);
            if (storedField != null) doc.add(storedField);
        }
        indexWriter.addDocument(doc);
    }

    /**
     * {@link org.apache.lucene.index.IndexOptions#DOCS_AND_FREQS} and stored
     */
    public static void dFIndexedAndStored(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.fieldSep, ctl.fieldNames.size());
        Field field, storedField;
        String fieldName;
        Object fieldValue;

        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setTokenized(false);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);

        Document doc = new Document();
        for (int i = 0; i < fields.size(); i++) {
            try {
                fieldValue = ctl.getConcreteFieldValue(fields.get(i), (short) i);
            } catch (Exception e) {
                fieldValue = null;
                GreatLogger.error(GreatLogger.Level.plain, DocIndexBuilderUtil.class, "docsFreqsIndexedAndStored", "字段类型化失败", fields.get(i), e);
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

    /**
     * {@link org.apache.lucene.index.IndexOptions#DOCS_AND_FREQS_AND_POSITIONS} and stored
     */
    public static void dFPIndexedAndStored(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.fieldSep, ctl.fieldNames.size());
        Field field, storedField;
        String fieldName;
        Object fieldValue;

        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setTokenized(false);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);

        Document doc = new Document();
        for (int i = 0; i < fields.size(); i++) {
            try {
                fieldValue = ctl.getConcreteFieldValue(fields.get(i), (short) i);
            } catch (Exception e) {
                fieldValue = null;
                GreatLogger.error(GreatLogger.Level.plain, DocIndexBuilderUtil.class, "docsFreqsPosIndexedAndStored", "字段类型化失败", fields.get(i), e);
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

    /**
     * {@link org.apache.lucene.index.IndexOptions#DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS} and stored
     */
    public static void dFPOIndexedAndStored(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
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
                GreatLogger.error(GreatLogger.Level.plain, DocIndexBuilderUtil.class, "docsFreqsPosOffsetIndexedAndStored", "字段类型化失败", fields.get(i), e);
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

    /**
     * {@link org.apache.lucene.index.IndexOptions#DOCS_AND_FREQS_AND_POSITIONS} and {@link org.apache.lucene.index.DocValues}
     */
    public static void docValueBuild(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.fieldSep, ctl.fieldNames.size());
        Field field = null, storedField;
        String fieldName;
        Object fieldValue;

        Document doc = new Document();
        for (int i = 0; i < fields.size(); i++) {
            try {
                fieldValue = ctl.getConcreteFieldValue(fields.get(i), (short) i);
            } catch (Exception e) {
                fieldValue = null;
                GreatLogger.error(GreatLogger.Level.plain, DocIndexBuilderUtil.class, "docsAndDocValues", "字段类型化失败", fields.get(i), e);
            }
            if (fieldValue == null) continue;
            fieldName = ctl.fieldNames.get(i).toLowerCase();
            storedField = null;
            switch (ctl.getFieldTypeByIndex(i).getSimpleName()) {
                case "Integer":
                case "Byte":
                case "Short":
                    field = new IntPoint(fieldName, ((Number) fieldValue).intValue());
                    storedField = new NumericDocValuesField(fieldName, ((Number) fieldValue).intValue());
                    break;
                case "Long":
                    field = new LongPoint(fieldName, ((Number) fieldValue).longValue());
                    storedField = new NumericDocValuesField(fieldName, ((Number) fieldValue).intValue());
                    break;
                case "Date":
                case "Timestamp":
                    field = new LongPoint(fieldName, ((Date) fieldValue).getTime());
                    storedField = new NumericDocValuesField(fieldName, ((Date) fieldValue).getTime());
                    break;
                case "Float":
                    field = new FloatPoint(fieldName, ((Number) fieldValue).floatValue());
                    storedField = new NumericDocValuesField(fieldName, Float.floatToRawIntBits(((Number) fieldValue).floatValue()));
                    break;
                case "Double":
                    field = new DoublePoint(fieldName, ((Number) fieldValue).doubleValue());
                    storedField = new NumericDocValuesField(fieldName, Double.doubleToRawLongBits(((Number) fieldValue).doubleValue()));
                    break;
                case "BigDecimal":
                    field = new BigIntegerPoint(fieldName, ((BigDecimal) fieldValue).toBigInteger());
                    storedField = new NumericDocValuesField(fieldName, Double.doubleToRawLongBits(((BigDecimal) fieldValue).doubleValue()));
                    break;
                default:
                    String value = (String) fieldValue;
                    value = value.trim();
                    if (!value.isEmpty()) {
                        field = new TextField(fieldName, value, Field.Store.NO);
                        storedField = new SortedSetDocValuesField(fieldName, new BytesRef(value.getBytes()));
                        //storedField = new BinaryDocValuesField(fieldName, new BytesRef(value.getBytes()));
                    }
                    break;
            }
            if (field != null) doc.add(field);
            if (storedField != null) doc.add(storedField);
        }
        indexWriter.addDocument(doc);
    }

    public static void docValueBuildByDocs(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.fieldSep, ctl.fieldNames.size());
        Field field = null, storedField;
        String fieldName;
        Object fieldValue;

        Document doc = new Document();
        for (int i = 0; i < fields.size(); i++) {
            try {
                fieldValue = ctl.getConcreteFieldValue(fields.get(i), (short) i);
            } catch (Exception e) {
                fieldValue = null;
                GreatLogger.error(GreatLogger.Level.plain, DocIndexBuilderUtil.class, "docsAndDocValues", "字段类型化失败", fields.get(i), e);
            }
            if (fieldValue == null) continue;
            fieldName = ctl.fieldNames.get(i).toLowerCase();
            storedField = null;
            switch (ctl.getFieldTypeByIndex(i).getSimpleName()) {
                case "Integer":
                case "Byte":
                case "Short":
                    field = new IntPoint(fieldName, ((Number) fieldValue).intValue());
                    storedField = new NumericDocValuesField(fieldName, ((Number) fieldValue).intValue());
                    break;
                case "Long":
                    field = new LongPoint(fieldName, ((Number) fieldValue).longValue());
                    storedField = new NumericDocValuesField(fieldName, ((Number) fieldValue).intValue());
                    break;
                case "Date":
                case "Timestamp":
                    field = new LongPoint(fieldName, ((Date) fieldValue).getTime());
                    storedField = new NumericDocValuesField(fieldName, ((Date) fieldValue).getTime());
                    break;
                case "Float":
                    field = new FloatPoint(fieldName, ((Number) fieldValue).floatValue());
                    storedField = new NumericDocValuesField(fieldName, Float.floatToRawIntBits(((Number) fieldValue).floatValue()));
                    break;
                case "Double":
                    field = new DoublePoint(fieldName, ((Number) fieldValue).doubleValue());
                    storedField = new NumericDocValuesField(fieldName, Double.doubleToRawLongBits(((Number) fieldValue).doubleValue()));
                    break;
                case "BigDecimal":
                    field = new BigIntegerPoint(fieldName, ((BigDecimal) fieldValue).toBigInteger());
                    storedField = new NumericDocValuesField(fieldName, Double.doubleToRawLongBits(((BigDecimal) fieldValue).doubleValue()));
                    break;
                default:
                    String value = (String) fieldValue;
                    value = value.trim();
                    if (!value.isEmpty()) {
                        FieldType type = new FieldType();
                        type.setOmitNorms(true); // 不写Norm
                        type.setTokenized(false);
                        type.setStored(false);
                        type.setIndexOptions(IndexOptions.DOCS);
                        type.freeze();
                        field = new Field(fieldName, value, type);
                        storedField = new SortedSetDocValuesField(fieldName, new BytesRef(value.getBytes()));
                        //storedField = new BinaryDocValuesField(fieldName, new BytesRef(value.getBytes()));
                    }
                    break;
            }
            if (field != null) doc.add(field);
            if (storedField != null) doc.add(storedField);
        }
        indexWriter.addDocument(doc);
    }

    public static void docValueBuild2(IndexWriter indexWriter, byte[] line, CtlConfig ctl) throws IOException {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.fieldSep, ctl.fieldNames.size());
        Field field = null, storedField;
        String fieldName;
        String fieldValue;

        Document doc = new Document();
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).length == 0) continue;
            fieldValue = new String(fields.get(i)).trim();
            fieldName = ctl.fieldNames.get(i).toLowerCase();
            storedField = null;
            field = null;
            if (!fieldValue.isEmpty()) {
                field = new TextField(fieldName, fieldValue, Field.Store.NO);
                // storedField = new BinaryDocValuesField(fieldName, new BytesRef(fieldValue.getBytes()));
            }
            if (field != null) doc.add(field);
            if (storedField != null) doc.add(storedField);
        }
        indexWriter.addDocument(doc);
    }

    /**
     * 目录到达指定上限后 删除当前表的索引文件
     */
    public static IndexWriter checkDiskRemainSpace(Path indexPath, IndexWriter indexWriter, ICallback<IndexWriter> newIndexWriter) {
        long size = getIndexPathSize(indexPath);
        try {
            if (size > 100l * 1024 * 1024 * 1024) { // 100g
                logger.info("索引目录超过100g, 删除索引: " + indexPath.toString());
                indexWriter.close();
                try {
                    ResourcesUtil.delete(indexPath);
                } catch (Exception e) {
                    logger.error("删除索引目录异常 => " + indexPath.toString(), e);
                }
                logger.info("索引目录超过100g, 删除索引" + indexPath.toString() + "后重建");
                return newIndexWriter.callback("", indexPath);
            }
        } catch (Exception e) {
            logger.error("删除重建索引异常,返回{@param indexWriter}", e);
        }
        return indexWriter;
    }

    public static long getIndexPathSize(Path indexPath) {
        long size = 0;
        int times = 0;
        while (times < 3) {
            try {
                size = ResourcesUtil.getPathSize(indexPath);
                break;
            } catch (IOException e) {
                times++;
            }
        }
        return size;
    }

    public static void collectStatistic(String desc) {
        Path statisticPath = Paths.get("logs/statistic.out");
        logger.info("开始搜集统计数据 => " + desc);
        try {
            desc += "\n";
            Files.write(statisticPath, desc.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            logger.error("收集统计信息遇到异常", e);
        }
    }

}
