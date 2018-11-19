package com.broadtech.learn.lucene.funresearch;

import com.broadtech.learn.lucene.SimpleIndexSearcher;
import com.broadtech.learn.lucene.SimpleIndexWriter;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * create by 2018/1/9 10:56<br>
 *
 * @author Yuanjun Chen
 */
public class DemoLucene71 {

    public static void main(String[] args) throws IOException {
        String path = "indexes/demoLucene71";

        // build index
        String nameFieldName = "0name";
        SimpleIndexWriter writer = new SimpleIndexWriter(path, IndexWriterConfig.OpenMode.CREATE);
        Document doc0 = new Document();
        doc0.add(new StringField(nameFieldName, new BytesRef("Tom swift".getBytes()), Field.Store.NO));
        doc0.add(new NumericDocValuesField("zero", 0));
        doc0.add(new NumericDocValuesField("age", 32));
        doc0.add(new DoubleDocValuesField("height", 1.75));
        writer.addDocument(doc0);
        writer.commitAndClose();

        // search index
        SimpleIndexSearcher searcher = new SimpleIndexSearcher(path);
        Query query = new TermQuery(new Term(nameFieldName, "Tom swift"));
        TopDocs rs = searcher.query(query);
        System.out.println(rs.totalHits);
    }
}
