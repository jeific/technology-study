package com.broadtech.lucene.research;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.index.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

public class NullMultiValueTest {
    public static void main(String[] args) throws IOException {
        String path = "indexes/null_multiValue";
        //writer(path);
        search(path);
    }

    private static void writer(String path) throws IOException {
        SimpleIndexWriter writer = new SimpleIndexWriter(path);

        Document doc1 = new Document();
        doc1.add(new SortedDocValuesField("name", new BytesRef("Tom swift".getBytes())));
        doc1.add(new SortedNumericDocValuesField("zero", 0));
        doc1.add(new SortedNumericDocValuesField("age", 32));
        doc1.add(new SortedNumericDocValuesField("height", Double.doubleToRawLongBits(1.75)));
        writer.addDocument(doc1);

        Document doc2 = new Document(); // name=null
        doc2.add(new SortedNumericDocValuesField("zero", 0));
        doc2.add(new SortedNumericDocValuesField("age", 33));
        doc2.add(new SortedNumericDocValuesField("height", Double.doubleToRawLongBits(1.85)));
        writer.addDocument(doc2);

        Document doc3 = new Document(); // age=null
        doc3.add(new SortedDocValuesField("name", new BytesRef("Josh swift".getBytes())));
        doc3.add(new SortedNumericDocValuesField("zero", 0));
        doc3.add(new SortedNumericDocValuesField("height", Double.doubleToRawLongBits(1.65)));
        writer.addDocument(doc3);

        Document doc4 = new Document(); // height=null
        doc4.add(new SortedDocValuesField("name", new BytesRef("Mike swift".getBytes())));
        doc4.add(new SortedNumericDocValuesField("zero", 0));
        doc4.add(new SortedNumericDocValuesField("age", 30));
        writer.addDocument(doc4);
        writer.commitAndClose();
    }

    private static void search(String path) throws IOException {
        SimpleIndexSearcher searcher = new SimpleIndexSearcher(path);
        IndexReader reader = searcher.getReader();
        int maxDoc = reader.maxDoc();
        int[] docBases = new int[reader.leaves().size()];
        for (int i = 0; i < docBases.length; i++) {
            docBases[i] = reader.leaves().get(i).docBase;
        }
        for (int docId = 0; docId < maxDoc; docId++) {
            int leaf = ReaderUtil.subIndex(docId, docBases);
            int doc = docId - docBases[leaf];
            LeafReader leafReader = reader.leaves().get(leaf).reader();

            SortedDocValues nameDV = leafReader.getSortedDocValues("name");
            System.out.println("name\t>> docId=" + docId + ",leafDocId=" + doc + ",Ord=" + nameDV.getOrd(doc) + ",bytes=" + nameDV.get(doc).utf8ToString());
            SortedNumericDocValues zeroDV = leafReader.getSortedNumericDocValues("zero");
            zeroDV.setDocument(doc);
            System.out.println("zero\t>> docId=" + docId + ",leafDocId=" + doc + ",object=" + zeroDV.getClass() + ",value=" + zeroDV.valueAt(0) + ",count=" + zeroDV.count());
            SortedNumericDocValues ageDV = leafReader.getSortedNumericDocValues("age");
            ageDV.setDocument(doc);
            System.out.println("age \t>> docId=" + docId + ",leafDocId=" + doc + ",object=" + ageDV.getClass() + ",value=" + ageDV.valueAt(0) + ",count=" + ageDV.count());
            SortedNumericDocValues heightDV = leafReader.getSortedNumericDocValues("height");
            heightDV.setDocument(doc);
            System.out.println("height\t>> docId=" + docId + ",leafDocId=" + doc + ",object=" + heightDV.getClass() + ",value=" + heightDV.valueAt(0) + ",count=" + heightDV.count());
            System.out.println();
        }

        searcher.close();
    }
}
