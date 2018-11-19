package com.broadtech.lucene;

import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * create by 2018/2/24 13:35<br>
 *
 * @author Yuanjun Chen
 * NRT: near real time<br>
 * 测试结论: <br>
 * 1. 多个线程使用同一个IndexWriter将产生多个segment<br>
 * 2. DirectoryReader.openIfChanged(IndexReader)实现低成本的NRT效果,在返回非null，需要自己关闭old reader
 */
public class NRTTest {

    public static void main(String[] args) throws Exception {
        String path = "indexes/nrt_singleton";
        IndexWriterConfig iwf = new IndexWriterConfig();
        iwf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwf.setCodec(new SimpleTextCodec());

        singleton(path, iwf);
        //simulateConcurrent(path, iwf);
    }

    private static void singleton(String path, IndexWriterConfig iwf) throws IOException {
        try (Directory dir = FSDirectory.open(new File(path).toPath());
             IndexWriter writer = new IndexWriter(dir, iwf);
        ) {
            Document doc = new Document();
            doc.add(new StringField("name", "Tom", Field.Store.YES));
            writer.addDocument(doc);

            doc = new Document();
            doc.add(new StringField("name", "Tom", Field.Store.YES));
            writer.addDocument(doc);

            doc = new Document();
            doc.add(new StringField("nice", "ok", Field.Store.YES));
            writer.addDocument(doc);

            doc = new Document();
            doc.add(new StringField("age", "10", Field.Store.YES));
            writer.addDocument(doc);
        }
    }

    private static void simulateConcurrent(String path, IndexWriterConfig iwf) throws IOException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(4);
        try (Directory dir = FSDirectory.open(new File(path).toPath());
             IndexWriter writer = new IndexWriter(dir, iwf);
        ) {
            nameField(cdl, writer);
            nameField(cdl, writer);
            new Thread(() -> {
                try {
                    Document doc = new Document();
                    doc.add(new StringField("nice", "ok", Field.Store.YES));
                    writer.addDocument(doc);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cdl.countDown();
                }
            }).start();
            new Thread(() -> {
                try {
                    Document doc = new Document();
                    doc.add(new StringField("age", "10", Field.Store.YES));
                    writer.addDocument(doc);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cdl.countDown();
                }
            }).start();
            cdl.await();
            writer.forceMerge(1);
        }
    }

    private static void nameField(CountDownLatch cdl, IndexWriter writer) {
        new Thread(() -> {
            try (IndexReader reader = DirectoryReader.open(writer)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                Document doc = new Document();
                doc.add(new StringField("name", "Tom", Field.Store.YES));
                writer.addDocument(doc);
                search(searcher, "name", "Tom");
                nrtSearch(reader, "name", "Tom");
                nrtSearch(writer, "name", "Tom");
//                    writer.commit();
//                    search(searcher, "name", "Tom");
//                    nrtSearch(reader, "name", "Tom");
//                    nrtSearch(writer, "name", "Tom");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cdl.countDown();
            }
        }).start();
    }

    private static void search(IndexSearcher searcher, String field, String value) throws IOException {
        TopDocs topDocs = searcher.search(new TermQuery(new Term(field, value)), 10);
        System.out.println("searcher => field:" + field + ",value:" + value + " >> " + topDocs.totalHits);
    }

    private static void nrtSearch(IndexReader reader, String field, String value) throws IOException {
        try (IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader) reader)) {
            TopDocs topDocs = new IndexSearcher(newReader).search(new TermQuery(new Term(field, value)), 10);
            System.out.println("reader => field:" + field + ",value:" + value + " >> " + topDocs.totalHits);
        }
    }

    /**
     * NRT：需要重新关联Writer快照
     */
    private static void nrtSearch(IndexWriter writer, String field, String value) throws IOException {
        try (IndexReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(new TermQuery(new Term(field, value)), 10);
            System.out.println("writer => field:" + field + ",value:" + value + " >> " + topDocs.totalHits);
        }
    }
}
