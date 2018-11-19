package com.broadtech.learn.lucene.custom;

import com.broadtech.learn.lucene.SimpleIndexWriter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.StringReader;

/**
 * 产生大量数据索引
 */
public class GenBigdata {

    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        String path = "indexes/bigdata";
        IndexWriterConfig iwf = new IndexWriterConfig();
        iwf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwf.setRAMBufferSizeMB(128);
        iwf.setUseCompoundFile(false);
        SimpleIndexWriter writer = new SimpleIndexWriter(path, iwf);
        int count = 10;
        while (count-- > 0) {
            writer(writer);
        }
        writer.commitAndClose();
        System.out.println("耗时: " + ((System.currentTimeMillis() - time) / 1000.0) + "s");
    }

    private static void writer(SimpleIndexWriter writer) throws IOException {
        Document doc0 = new Document();
        String nameValue = "Tom swift k 中华人民共和国 " + ((char) ((int) (Math.random() * 100 % 26) + 97));
        System.out.println(nameValue);
        doc0.add(new SortedDocValuesField("name", new BytesRef(nameValue)));
        doc0.add(new NumericDocValuesField("zero", 0));
        long ageValue = (long) (Math.random() * 1000);
        doc0.add(new NumericDocValuesField("age", ageValue));
        double heightValue = Math.random() * 10000;
        doc0.add(new DoubleDocValuesField("height", heightValue));

        //indexed
        FieldType nameFieldType = new FieldType();
        nameFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        nameFieldType.setTokenized(true);
        nameFieldType.setOmitNorms(true);
        // Field nameField = new Field("name", nameValue, nameFieldType);
        //Field nameField = new Field("name", new StringReader(nameValue), nameFieldType);
        Field nameField = new Field("name", new StandardAnalyzer()
                .tokenStream("name", nameValue), nameFieldType);
        doc0.add(nameField);

        doc0.add(new LongPoint("age", ageValue));
        doc0.add(new DoublePoint("height", heightValue));
        writer.addDocument(doc0);
    }

    private static void testStringReader() throws IOException {
        String s = "Hello world! 中华人民共和国";
        StringReader reader = new StringReader(s);
        int ch;
        int i = 0;
        do {
            ch = reader.read();
            System.out.println(++i + ": " + ch);
        } while (ch != -1);
        System.out.println(s.length());
    }
}
