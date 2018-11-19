package com.broadtech.qp.query;

import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.TimeCounter;
import com.broadtech.qp.query.custom.CustomQuery;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Chen Yuanjun on 2017/7/25.
 */
public class SimpleQuery {
    private static final Logger logger = Logger.getLogger(SimpleQuery.class);

    /**
     * 使用QueryParser辅助通过term分析器可以构建相似查询<br>
     * 不使用term分析器则为精确查询
     */
    public static void main(String[] args) throws Exception {
        Path indexPath = Paths.get("indexes/doc_cf_false");
        logger.info("");
        logger.info("*******************" + SimpleQuery.class + "*******************");
        Directory dir = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        TimeCounter timeCounter = new TimeCounter();
        termQuery("enjoy", "book", searcher);
//        termQuery("enjoy", "music", searcher);
//        termQuery("enjoy", "book", searcher);
//        queryParserSearch("enjoy", "is read book", searcher);
//        termQuery("enjoy", "books", searcher);
//        statistic("enjoy", "read", searcher, reader);
//        statistic("name", "read", searcher, reader);
//        termRangeQuery(searcher);
//        pointRangeQuery(searcher);
//        multiPhraseQuery("enjoy", "read", searcher);
//        phraseQuery("enjoy", "read", searcher);
//        booleanQuery(searcher);
        fuzzyQuery(searcher);

        System.out.println("cost time : " + timeCounter.cost() + " ms");
    }

    private static void termQuery(String field, String value, IndexSearcher searcher) throws IOException {
        logger.info("termQuery查询条件: field: " + field + " value: " + value);
        Term term = new Term(field, value);
        TermQuery termQuery = new TermQuery(term);
        TopDocs results = searcher.search(termQuery, 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    private static void phraseQuery(String field, String value, IndexSearcher searcher) throws IOException {
        logger.info("phraseQuery查询条件: field: " + field + " value: " + value);
        PhraseQuery phraseQuery = new PhraseQuery(0, field, value.split(",")); // slop 间隔的term数
        TopDocs results = searcher.search(phraseQuery, 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    private static void multiPhraseQuery(String field, String value, IndexSearcher searcher) throws IOException {
        logger.info("multiPhraseQuery查询条件: field: " + field + " value: " + value);
        MultiPhraseQuery.Builder builder = new MultiPhraseQuery.Builder();
        builder.setSlop(1); // 斜率
        for (String t : value.split(" "))
            builder.add(new Term(field, t));
        MultiPhraseQuery multiPhraseQuery = builder.build();
        TopDocs results = searcher.search(multiPhraseQuery, 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    /**
     * 使用分词器可以实现相似查询
     */
    private static void queryParserSearch(String field, String value, IndexSearcher searcher) throws Exception {
        logger.info("queryParserSearch查询条件: field: " + field + " value: " + value);
        QueryParser parser = new QueryParser(field, new StandardAnalyzer());
        TopDocs results = searcher.search(parser.parse(value), 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    /**
     * This query matches the documents looking for terms that fall into the supplied range according to BytesRef.compareTo(BytesRef).
     * 通过compareTo方法判断是否命中
     */
    private static void termRangeQuery(IndexSearcher searcher) throws Exception {
        String field = "enjoy";
        logger.info("TermRangeQuery查询条件: field: " + field + " range: like - music");
        TermRangeQuery termRangeQuery = new TermRangeQuery(field, new BytesRef("like".getBytes())
                , new BytesRef("music".getBytes()), true, true);
        TopDocs results = searcher.search(termRangeQuery, 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    private static void pointRangeQuery(IndexSearcher searcher) throws Exception {
        String field = "age";
        logger.info("pointRangeQuery查询条件: field: " + field + " range: 30 - 50");
        Query pointRangeQuery = IntPoint.newRangeQuery(field, 30, 50);
        TopDocs results = searcher.search(pointRangeQuery, 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    /**
     * BooleanQuery查询示例
     */
    private static void booleanQuery(IndexSearcher searcher) throws ParseException, IOException {
        logger.info("booleanQuery查询");
        // Query intPointQuery = IntPoint.newExactQuery("age",25);
        Query intPointQuery = IntPoint.newRangeQuery("age", 25, 30);
        Query termQuery = new TermQuery(new Term("name", "jeific"));
        Query parseQuery = new QueryParser("enjoy", new StandardAnalyzer()).parse("read book");
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        Query booleanQuery = builder
                .add(intPointQuery, BooleanClause.Occur.MUST_NOT)
                .add(termQuery, BooleanClause.Occur.SHOULD)
                .add(parseQuery, BooleanClause.Occur.SHOULD)
                .build();
        TopDocs results = searcher.search(booleanQuery, 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    /**
     * FuzzyQuery 查询示例
     */
    private static void fuzzyQuery(IndexSearcher searcher) throws ParseException, IOException {
        logger.info("FuzzyQuery 查询");
        String storedRaw = "games.qq.com/?pgv_ref=aio2015";
        Term term = new Term("noToken", "games.qq.com/?pgv_ref=aio2015");
//        FuzzyQuery query = new FuzzyQuery(term, 0, storedRaw.length(), 10, false);
//        TermQuery query = new TermQuery(term);
//        PrefixQuery query = new PrefixQuery(term,) // WildcardQuery RegexpQuery 不合适，传递参数是前缀
        CustomQuery query = new CustomQuery(term);

        TopDocs results = searcher.search(query, 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    private static void statistic(String field, String value, IndexSearcher searcher, IndexReader reader) throws IOException {
        Term term = new Term(field, value);
        // 获取term的词频,文档频率
        TermStatistics termStatistics = searcher.termStatistics(term, TermContext.build(reader.getContext(), term));
        System.out.println("Term: " + termStatistics.term().toString() + "=" + new String(termStatistics.term().bytes)
                + " docFreq: " + termStatistics.docFreq()
                + " totalTermFreq: " + termStatistics.totalTermFreq());
        System.out.println("Field: " + field
                + " DocCount: " + reader.getDocCount(field)
                + " RefCount: " + reader.getRefCount()
                + " DocFreq: " + reader.getSumDocFreq(field)
                + " SumTotalTermFreq: " + reader.getSumTotalTermFreq(field));
        Terms terms = reader.getTermVector(1, field);
        System.out.println(terms + "\n" + reader.getTermVectors(1));
    }
}
