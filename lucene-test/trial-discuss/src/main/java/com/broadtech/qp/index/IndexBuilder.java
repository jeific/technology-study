package com.broadtech.qp.index;

import com.broadtech.bdp.common.ctl.RichCtlConfig;
import com.broadtech.bdp.common.util.*;
import com.broadtech.qp.index.status.RuntimeStatus;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/7/10.
 */
public class IndexBuilder implements Runnable {
    private final Logger logger = Logger.getLogger(this.getClass());
    private final byte[] cache = new byte[64 * 1024];
    private final RichCtlConfig ctl;
    private final TimeCounter timeCounter = new TimeCounter();
    private final RuntimeStatus status;
    private final ConcurrentLinkedQueue<Path> pathQueue = new ConcurrentLinkedQueue<>();
    private boolean stopped = false;

    public IndexBuilder(RichCtlConfig ctl, RuntimeStatus status) throws Exception {
        this.ctl = ctl;
        this.status = status;
    }

    public void stop() {
        stopped = true;
    }

    public void build(Path path) {
        if (!pathQueue.contains(path)) {
            pathQueue.offer(path);
        }
    }

    /**
     * 一行记录构建document
     *
     * @throws Exception
     */
    private void indexDoc(IndexWriter indexWriter, byte[] line, RichCtlConfig ctl) throws Exception {
        List<byte[]> fields = TokenUtils.tokensFromLine(line, ctl.getCtlConfig().fieldSep, ctl.getCtlConfig().fieldNames.size());
        Field field;
        String fieldName;
        Object fieldValue;
        Document doc = new Document();
        for (int i = 0; i < fields.size(); i++) {
            try {
                fieldValue = ctl.getConcreteFieldValue(fields.get(i), (short) i);
            } catch (Exception e) {
                fieldValue = null;
                GreatLogger.error(GreatLogger.Level.plain, IndexBuilder.class, "indexDoc", "字段类型化失败", fields.get(i), e);
            }
            if (fieldValue == null) continue;
            fieldName = ctl.getCtlConfig().fieldNames.get(i).toLowerCase();
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
        status.addLine(line.length, 1);
    }

    @Override
    public void run() {
        long lastTime = Clock.systemUTC().millis();
        IndexWriter indexWriter;
        try {
            indexWriter = createIndexWriter();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return;
        }
        Path indexFilePath;
        while (!stopped) {
            indexFilePath = pathQueue.poll();
            if (indexFilePath == null) {
                ThreadAssistant.sleep(10, TimeUnit.MILLISECONDS);
                continue;
            }
            try {
                timeCounter.reset();
                int offset = 0, count;
                List<byte[]> lines = new ArrayList<>();
                try (InputStream ins = new FileInputStream(indexFilePath.toFile())) {
                    while ((count = ins.read(cache, offset, cache.length - offset)) != -1) {
                        lines.clear();
                        offset = TokenUtils.tokens(cache, count, ctl.getCtlConfig().lineSep, lines);
                        for (byte[] line : lines) {
                            indexDoc(indexWriter, line, ctl);
                        }
                    }
                }
                logger.info("build " + indexFilePath.toString() + " " + UnitHelper.getHumanSize(indexFilePath.toFile().length()) + " index, cost time: " + timeCounter.humanCost());
            } catch (Throwable e) {
                logger.error("build " + indexFilePath.toString() + " index failure", e);
            } finally {
                if (Clock.systemUTC().millis() - lastTime > 60 * 1000) {
                    lastTime = Clock.systemUTC().millis();
                    logger.info("checkDiskRemainSpace start => " + ctl.getCtlConfig().tableName);
                    indexWriter = checkDiskRemainSpace(indexWriter);
                    logger.info("checkDiskRemainSpace end => " + ctl.getCtlConfig().tableName);
                }
            }
        }
        try {
            System.out.println("======= 开始关闭 ========");
            logger.info(indexWriter.getDirectory().toString() + " IndexDoc处理线程即将关闭");
            indexWriter.close();
            System.out.println("======= 已关闭 ========");
        } catch (Throwable e) {
            System.out.println("======= 关闭异常 ========" + e.toString());
            logger.error(e.getMessage(), e);
        }
    }

    private IndexWriter createIndexWriter() throws IOException {
        Directory dir = FSDirectory.open(Paths.get("indexes", ctl.getCtlConfig().tableName));
        IndexWriterConfig iwc = new IndexWriterConfig();
        return new IndexWriter(dir, iwc);
    }

    /**
     * 目录到达指定上限后 删除当前表的索引文件
     */
    private IndexWriter checkDiskRemainSpace(IndexWriter indexWriter) {
        try {
            long size = ResourcesUtil.getPathSize(Paths.get("indexes"));
            if (size > 100l * 1024 * 1024 * 1024) { // 100g
                Path indexes = Paths.get("indexes", ctl.getCtlConfig().tableName);
                logger.info("索引目录超过100g, 删除索引: " + indexes.toString());
                indexWriter.close();
                try {
                    ResourcesUtil.delete(indexes);
                } catch (Exception e) {
                    logger.error("删除索引目录异常 => " + indexes.toString(), e);
                }
                logger.info("索引目录超过100g, 删除索引" + indexes.toString() + "后重建");
                return createIndexWriter();
            }
        } catch (IOException e) {
            logger.error("删除重建索引异常,返回{@param indexWriter}", e);
        }
        return indexWriter;
    }
}