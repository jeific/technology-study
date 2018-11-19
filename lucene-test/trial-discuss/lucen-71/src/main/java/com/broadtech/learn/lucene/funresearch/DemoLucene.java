package com.broadtech.learn.lucene.funresearch;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

/**
 * create by 2018/1/9 10:56<br>
 *
 * @author Yuanjun Chen
 */
public class DemoLucene {

    public static void main(String[] args) throws IOException {
        String path = "indexes/demoLucene71";

        // build index
        IndexWriterConfig iwf = new IndexWriterConfig();
        iwf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        write(path, iwf);
        //write(path, iwf); // iwf必须独占使用，即使writer已经关闭也不可

        // search index
        search(path);

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
            String nameFieldName = "0name";
            Document doc0 = new Document();
            doc0.add(new StringField(nameFieldName, new BytesRef("Tom swift".getBytes()), Field.Store.NO));
            doc0.add(new NumericDocValuesField("zero", 0));
            doc0.add(new NumericDocValuesField("age", 32));
            doc0.add(new DoubleDocValuesField("height", 1.75));
            writer.addDocument(doc0);
            writer.close();
        } catch (Exception e) {
            Assert.fail(e.toString());
        } finally {
            if (writer != null) writer.close();
            dir.close();
        }
    }
}
