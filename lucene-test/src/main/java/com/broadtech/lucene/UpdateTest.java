package com.broadtech.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * create by 2018/2/24 16:25<br>
 *
 * @author Yuanjun Chen<br>
 * 更新功能测试<br>
 */
public class UpdateTest {

    public static void main(String[] args) throws Exception {
        String path = "indexes/update_index";
        IndexWriterConfig iwf = new IndexWriterConfig();
        iwf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        // iwf.setCodec(new SimpleTextCodec());
        try (
                Directory dir = FSDirectory.open(Paths.get(path));
                IndexWriter writer = new IndexWriter(dir, iwf)
        ) {
            Document doc = new Document();
            doc.add(new StringField("name", "Tom", Field.Store.YES));
            //doc.add(new IntPoint("name_cnt", 1)); // int索引
            doc.add(new StringField("name_cnt", "1", Field.Store.YES));
            writer.addDocument(doc);
            nrtSearch(writer, "name", "Tom", null);

            // update
            doc = new Document();
            doc.add(new StringField("name", "Tom", Field.Store.YES));
            doc.add(new StringField("name_cnt", "2", Field.Store.YES));
            writer.updateDocument(new Term("name", "Tom"), doc); // update支持对指定DocValue进行更新
            nrtSearch(writer, "name", "Tom", null);

            // delete
            writer.deleteDocuments(new Term("name", "Tom"));
            nrtSearch(writer, "name", "Tom", null);

            doc = new Document();
            doc.add(new StringField("nice", "ok", Field.Store.YES));
            doc.add(new SortedDocValuesField("desc", new BytesRef("This is apple".getBytes())));
            writer.addDocument(doc);

            // update docValue
            doc = new Document();
            doc.add(new StringField("nice", "ok2", Field.Store.YES));
            doc.add(new SortedDocValuesField("desc", new BytesRef("This is apple2".getBytes())));
            writer.updateDocument(new Term("nice", "ok3"), doc); // Term存在则更新，否则添加
            nrtSearch(writer, "nice", "ok", "desc");
            nrtSearch(writer, "nice", "ok2", "desc");

            // 以下用法错误, can only update NUMERIC or BINARY
            // writer.updateDocValues(new Term("nice", "ok"), new SortedDocValuesField("desc", new BytesRef("This is apple2".getBytes())))
        }
    }

    /**
     * NRT：需要重新关联Writer快照
     */
    private static void nrtSearch(IndexWriter writer, String field, String value, String docField) throws IOException {
        try (IndexReader reader = DirectoryReader.open(writer)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(new TermQuery(new Term(field, value)), 10);
            StringBuilder result = new StringBuilder();
            for (ScoreDoc topDoc : topDocs.scoreDocs) {
                Document doc = reader.document(topDoc.doc);
                result.append(doc.toString());
                if (docField != null) {
                    LeafReader leafReader = reader.leaves().get(0).reader();
                    SortedDocValues docValues = leafReader.getSortedDocValues(docField);
                    docValues.advance(topDoc.doc);
                    result.append("DV: " + docField + ",id=" + topDoc.doc + " value=" + docValues.binaryValue().utf8ToString());
                }
            }
            System.out.println("writer => field:" + field + ",value:" + value + " >> " + topDocs.totalHits + " Result: " + result.toString());
        }
    }
}
