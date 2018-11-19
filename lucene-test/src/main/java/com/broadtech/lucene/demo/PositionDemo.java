package com.broadtech.lucene.demo;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

/**
 * 自定义搜索
 */
public class PositionDemo {

    public static void main(String[] args) throws IOException {
        Directory dir = new RAMDirectory();
        IndexWriter writer = store(dir);
        IndexReader reader = DirectoryReader.open(writer);
        IndexSearcher searcher = new IndexSearcher(reader);
        researchPositionFetch(reader, searcher);
        reader.close();
        dir.close();
    }

    /**
     * 研究位置数据提取，前提位置数据必须已被存储,步骤：
     * 1. 定位field
     * 2. 定位doc
     * 3. 获取term频率
     * 4. 提取位置数据
     */
    private static void researchPositionFetch(IndexReader reader, IndexSearcher searcher) throws IOException {
        StandardAnalyzer analyzer = null;
        try {
            //PhraseQuery query = new PhraseQuery("name", "T");

            // =========================   分词器【Analyze】使用    ===============================
            analyzer = new StandardAnalyzer();
            TokenStream tokenStream = analyzer.tokenStream("name", "Tom swift 中华人民共和国");
            // 获取每个单词信息,获取词元文本属性
            CharTermAttribute charTerm = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
//        while (tokenStream.incrementToken()) {
//            System.out.println(charTerm.toString());
//        }
            // =========================   分词器【Analyze】使用 END ===============================

            Term term = new Term("name", "国");
            TermQuery query = new TermQuery(term);
            TopDocs topDocs = searcher.search(query, 10);
            System.out.println(reader.maxDoc() + "\t" + topDocs.totalHits + "\t" + Arrays.toString(topDocs.scoreDocs));

            // 提取term
            Terms terms = reader.leaves().get(0).reader().terms("name"); // 【1.】 定位field
            TermsEnum termEnum = terms.iterator(); // SegmentTermsEnum
            PostingsEnum postingsEnum = null; // Lucene50PostingsReader.BlockDocsEnum
            if (termEnum.seekExact(term.bytes())) {
                postingsEnum = termEnum.postings(postingsEnum, PostingsEnum.POSITIONS); // 读取位置信息
                // 【2. 定位doc】
                for (int doc = postingsEnum.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = postingsEnum.nextDoc()) {
                    // 【3.】根据term频率确定位置数量
                    for (int posIndex = 0; posIndex < postingsEnum.freq(); posIndex++) {
                        // 【4.】定位position
                        System.out.println("docId: " + doc + " position: " + postingsEnum.nextPosition());
                    }
                }

            }
        } finally {
            IOUtils.close(analyzer);
        }
    }

    public static IndexWriter store(Directory dir) throws IOException {
        long time = System.currentTimeMillis();
        IndexWriterConfig iwf = new IndexWriterConfig();
        iwf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwf.setRAMBufferSizeMB(128);
        iwf.setUseCompoundFile(false);
        IndexWriter writer = new IndexWriter(dir, iwf);

        int count = 10;
        while (count-- > 0) {
            writer(writer);
        }
        System.out.println("耗时: " + ((System.currentTimeMillis() - time) / 1000.0) + "s");
        return writer;
    }

    private static void writer(IndexWriter writer) throws IOException {
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
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Field nameField = new Field("name", analyzer.tokenStream("name", nameValue), nameFieldType);
        doc0.add(nameField);
        analyzer.close();

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
