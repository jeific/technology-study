package com.broadtech.lucene.demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * create by 2018/1/9 10:56<br>
 *
 * @author Yuanjun Chen
 */
public class DemoLucene {

    public static void main(String[] args) throws Exception {
        String path = "indexes/demoLucene71";

        // build index
        IndexWriterConfig iwf = new IndexWriterConfig();
        iwf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        write(path, iwf);
        //write(path, iwf); // iwf必须独占使用，即使writer已经关闭也不可

        // search index
        search2(path);

        // clean
        //new File(path).delete();

        Queue<Integer> intQueue = new LinkedList<>();
        Assert.assertTrue(intQueue.poll() == null);
    }

    private static void search(String path) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(path));
        IndexReader reader = null;
        try {
            reader = DirectoryReader.open(dir);
            LeafReader leafReader = reader.leaves().get(0).reader();
            NumericDocValues values = leafReader.getNumericDocValues("age");
            int docId = 0;
            values.advance(docId);
            Long age1 = values.longValue();
            values.advance(docId);
            long age2 = values.longValue();
            System.out.println(reader.numDocs() + "\t" + age1 + "\t" + age2);
        } finally {
            if (reader != null) reader.close();
            dir.close();
        }
    }

    private static void write(String path, IndexWriterConfig conf) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(path));
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(dir, conf);
            String nameFieldName = "name";
            Document doc0 = new Document();
            //doc0.add(new StringField(nameFieldName, new BytesRef("Tom swift".getBytes()), Field.Store.NO));
            doc0.add(getAnalyzerField(nameFieldName, "Tom swift"));
            doc0.add(new NumericDocValuesField("zero", 0));
            doc0.add(new NumericDocValuesField("age", 32));
            doc0.add(new DoubleDocValuesField("height", 1.75));
            writer.addDocument(doc0);

            Document doc1 = new Document();
            //doc0.add(new StringField(nameFieldName, new BytesRef("Tom swift".getBytes()), Field.Store.NO));
            doc1.add(getAnalyzerField(nameFieldName, "Gif"));
            writer.addDocument(doc1);

            writer.close();
        } catch (Exception e) {
            Assert.fail(e.toString());
        } finally {
            if (writer != null) writer.close();
            dir.close();
        }
    }

    /**
     * Reader被多线程使用 【允许reader实例被多线程使用】
     */
    private static void search2(String path) throws IOException, ParseException {
        Directory dir = FSDirectory.open(Paths.get(path));
        IndexReader reader = DirectoryReader.open(dir);
        LeafReader leafReader = reader.leaves().get(0).reader();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int i1 = 0; i1 < 1000; i1++) {
                    try {
                        NumericDocValues values = leafReader.getNumericDocValues("age");
                        int docId = 0;
                        values.advance(docId);
                        Long age1 = values.longValue();
                        values.advance(docId);
                        long age2 = values.longValue();
                        System.out.println(i1 + "\t" + reader.numDocs() + "\t" + age1 + "\t" + age2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });//.start();
        }

        IndexSearcher searcher = new IndexSearcher(reader);
        QueryParser queryParser = new QueryParser("name", new CharAnalyzer());
        /**
         * 转换为 or 运算
         */
        Query query = queryParser.parse("fg");
        TopDocs hits = searcher.search(query, 1);
        // totalHits: 记录所有的命中总数
        System.out.println(hits.totalHits + "\t" + Arrays.toString(hits.scoreDocs));
    }

    private static Field getAnalyzerField(String fieldName, String value) {
        FieldType fieldType = new FieldType();
        fieldType.setStored(false); // 设置是否行存储
        fieldType.setOmitNorms(true);
        fieldType.setTokenized(true);
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        Analyzer analyzer = new CharAnalyzer();
        return new Field(fieldName, analyzer.tokenStream(fieldName, value), fieldType);
    }
}
