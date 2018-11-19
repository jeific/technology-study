package com.broadtech.qp.custom;

import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.TimeCounter;
import com.broadtech.qp.query.SimpleQuery;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Created by jeifi on 2017/8/3.
 * 不使用{@link org.apache.lucene.search.TopScoreDocCollector}收集数据
 */
public class SimpleCollectQuery {
    private static final Logger logger = Logger.getLogger(SimpleQuery.class);

    public static void main(String[] args) throws Exception {
        Path indexPath = Paths.get("indexes/doc_cf_false");
        logger.info("");
        logger.info("*******************" + SimpleQuery.class + "*******************");
        Directory dir = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        TimeCounter timeCounter = new TimeCounter();

//        booleanQuery2(searcher);
        termQuery("enjoy", "read", searcher);

        System.out.println("cost time : " + timeCounter.cost() + " ms");
    }

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

        TopDocs results = searcher.search(booleanQuery, getCollectorManager());

        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    private static void booleanQuery2(IndexSearcher searcher) throws ParseException, IOException {
        logger.info("booleanQuery查询");
        // Query intPointQuery = IntPoint.newExactQuery("age",25);
        Query termQuery = new TermQuery(new Term("name", "Tom"));  // joch 可以查出
        Query parseQuery = new QueryParser("enjoy", new StandardAnalyzer()).parse("read book");
        BooleanQuery.Builder builder = new BooleanQuery.Builder();

        Query booleanQuery = builder
                .add(termQuery, BooleanClause.Occur.MUST)
                .add(parseQuery, BooleanClause.Occur.MUST)
                .build();

        TopDocs results = searcher.search(booleanQuery,10);

        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    private static void termQuery(String field, String value, IndexSearcher searcher) throws IOException {
        logger.info("termQuery查询条件: field: " + field + " value: " + value);
        Term term = new Term(field, value);
        TermQuery termQuery = new TermQuery(term);
        TopDocs results = searcher.search(termQuery, getCollectorManager());
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = searcher.doc(scoreDoc.doc);
            logger.info(scoreDoc.toString());
            doc.getFields().forEach(f -> logger.info(f.getClass() + "\t" + f.toString()));
        }
    }

    private static CollectorManager<CustomCollector, TopDocs> getCollectorManager() {
        return new CollectorManager<CustomCollector, TopDocs>() {

            @Override
            public CustomCollector newCollector() throws IOException {
                return new CustomCollector();
            }

            @Override
            public TopDocs reduce(Collection<CustomCollector> collectors) throws IOException {
                final TopDocs[] topDocs = new TopDocs[collectors.size()];
                int i = 0;
                for (CustomCollector collector : collectors) {
                    topDocs[i++] = collector.topDocs();
                }
                return TopDocs.merge(0, 10, topDocs, true);
            }
        };
    }
}
