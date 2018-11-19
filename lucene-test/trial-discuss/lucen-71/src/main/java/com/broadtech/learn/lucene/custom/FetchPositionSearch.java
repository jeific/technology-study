package com.broadtech.learn.lucene.custom;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * 自定义搜索
 */
public class FetchPositionSearch {

    public static void main(String[] args) throws IOException {
        String path = "indexes/bigdata";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(path)));
        IndexSearcher searcher = new IndexSearcher(reader);
        researchPositionFetch(reader, searcher);
        reader.close();
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

            Term term = new Term("name", "k");
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
}
