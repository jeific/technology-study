package com.broadtech.learn.lucene.funresearch;

import com.broadtech.learn.lucene.SimpleIndexSearcher;
import com.broadtech.learn.lucene.SimpleIndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Arrays;

/**
 * 基于lucene 7.1.0
 */
public class DVNullSolution {
    public static void main(String[] args) throws IOException {
        String path = "indexes/null_value2";
        SimpleIndexWriter writer = new SimpleIndexWriter(path);
        writer(writer);
        writer.commitAndClose();
        search(path);
    }

    private static void search(String path) throws IOException {
        SimpleIndexSearcher searcher = new SimpleIndexSearcher(path);
        IndexReader reader = searcher.getReader();
        int maxDoc = reader.maxDoc();
        int[] docBases = new int[reader.leaves().size()];
        for (int i = 0; i < docBases.length; i++) {
            docBases[i] = reader.leaves().get(i).docBase;
        }
        // lucene 7.1 迭代方式实现
        for (int docId = 0; docId < maxDoc; docId++) {
            int leaf = ReaderUtil.subIndex(docId, docBases);
            int doc = docId - docBases[leaf];
            LeafReader leafReader = reader.leaves().get(leaf).reader();
            try {
                System.out.println("====================== " + doc + " =========================");
                SortedDocValues nameDV = leafReader.getSortedDocValues("name");
                System.out.println("name\t>> docId=" + docId + ",leafDocId=" + doc + ",object=" + nameDV
                        + ",bytes=" + (nameDV.advance(doc) == doc ? nameDV.binaryValue().utf8ToString() : "null"));
                NumericDocValues zeroDV = leafReader.getNumericDocValues("zero");
                System.out.println("zero\t>> docId=" + docId + ",leafDocId=" + doc + ",object=" + zeroDV + "\tadvance=" + zeroDV.advanceExact(doc)
                        + ",value=" + (zeroDV.advanceExact(doc) ? zeroDV.longValue() : "null"));
                NumericDocValues ageDV = leafReader.getNumericDocValues("age");
                System.out.println("age \t>> docId=" + docId + ",leafDocId=" + doc + ",object=" + ageDV + "\tadvance=" + ageDV.advanceExact(doc)
                        + ",value=" + (ageDV.advanceExact(doc) ? ageDV.longValue() : "null"));
                NumericDocValues heightDV = leafReader.getNumericDocValues("height");
                System.out.println("height\t>> docId=" + docId + ",leafDocId=" + doc + ",object=" + heightDV + "\tadvance=" + heightDV.advanceExact(doc)
                        + ",value=" + (heightDV.advanceExact(doc) ? Double.longBitsToDouble(heightDV.longValue()) : "null"));
                System.out.println();
            } catch (Exception e) {
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }

        searcher.close();
    }

    public static void writer(SimpleIndexWriter writer) throws IOException {
        Document doc0 = new Document();
        doc0.add(new SortedDocValuesField("name", new BytesRef("Tom swift".getBytes())));
        doc0.add(new NumericDocValuesField("zero", 0));
        doc0.add(new NumericDocValuesField("age", 32));
        doc0.add(new DoubleDocValuesField("height", 1.75));
        writer.addDocument(doc0);

        Document doc1 = new Document(); // name=null
        doc1.add(new NumericDocValuesField("zero", 0));
        doc1.add(new NumericDocValuesField("age", 33));
        doc1.add(new DoubleDocValuesField("height", 1.85));
        writer.addDocument(doc1);

        Document doc2 = new Document(); // age=null
        doc2.add(new SortedDocValuesField("name", new BytesRef("Josh swift".getBytes())));
        doc2.add(new NumericDocValuesField("zero", 0));
        doc2.add(new DoubleDocValuesField("height", 1.65));
        writer.addDocument(doc2);

        Document doc3 = new Document(); // height=null
        doc3.add(new SortedDocValuesField("name", new BytesRef("Mike swift".getBytes())));
        doc3.add(new NumericDocValuesField("zero", 0));
        doc3.add(new NumericDocValuesField("age", 30));
        writer.addDocument(doc3);
    }
}
