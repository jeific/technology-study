package com.broadtech.qp.trial;

import com.broadtech.bdp.common.util.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Chen Yuanjun on 2017/7/13.
 */
public class IndexAndSearchTrial {
    private static final Logger logger = Logger.getLogger(IndexAndSearchTrial.class);

    public static void main(String[] args) throws IOException, ParseException {
        Path path = Paths.get("indexes/doc_cf_false");
        logger.info("");
        logger.info(path.toAbsolutePath().toString());

        String[] fields = {"name", "age", "contacts", "enjoy", "sport", "noToken", "DocValue"};
        String[][] docs = {
                {"joch", "23", "2", "I like read book.", "true", "www.baidu.con", "34"}
                , {"Tom", "25", "3", "non", "false", "", "47"}
                , {"joch jeific", "32", "1", "books,music,play,read; Read is pleasant", "true", "", "12"}
                , {"tianzuo", "0", "1", "anythings", "true", "games.qq.com/?pgv_ref=aio2015", "96"}
                , {"NullValue", null, "1", null, "true", "games.qq.com/?pgv_ref=aio2015", "96"}};

        index(path, fields, docs);
        emptySearch(path);
        //search(path, "name", "joch");
//        search(path, "enjoy", "read");
//        search(path, "age", "25");
//        search(path, "contacts", "1");
//        search(path, "age", "26"); // expect : not found
    }

    private static void index(Path path, String[] fields, String[][] docs) throws IOException {
        Directory dir = FSDirectory.open(path);
        IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
        iwc.setUseCompoundFile(false);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        FieldType typeAll = new FieldType();
        typeAll.setTokenized(true);
        typeAll.setStored(true);
        typeAll.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);

        FieldType type = new FieldType();
        type.setTokenized(true);
        type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);

        FieldType notokenType = new FieldType();
        notokenType.setTokenized(false);
        notokenType.setIndexOptions(IndexOptions.DOCS);
        notokenType.setDocValuesType(DocValuesType.BINARY);
        notokenType.freeze();

        FieldType classicType = new FieldType();
        classicType.setOmitNorms(true); // 不写Norm
        classicType.setTokenized(false);
        classicType.setStored(false);
        classicType.setIndexOptions(IndexOptions.DOCS);
        classicType.freeze();

        for (String[] _doc : docs) {
            Document doc = new Document();
            //doc.add(new StringField(fields[0], _doc[0], Field.Store.YES));
            doc.add(new Field(fields[0], _doc[0].getBytes(), classicType));
            doc.add(new SortedDocValuesField(fields[0], new BytesRef(_doc[0].getBytes(StandardCharsets.UTF_8))));
            if (_doc[1] != null)
                doc.add(new IntPoint(fields[1], Integer.valueOf(_doc[1])));
            if (_doc[2] != null)
                doc.add(new IntPoint(fields[2], Integer.valueOf(_doc[2]))); // provided N-dimensional int point
            //doc.add(new StoredField(fields[2], Integer.valueOf(_doc[2]))); // stored-only field
            if (_doc[3] != null)
                doc.add(new Field(fields[3], _doc[3], classicType)); // Lucene不能处理null值
            doc.add(new Field(fields[4], _doc[4], classicType));
            doc.add(new Field(fields[5], _doc[5], classicType));
            doc.add(new DoubleDocValuesField(fields[6], Double.valueOf(_doc[6])));
            indexWriter.addDocument(doc);
        }
        indexWriter.close();
    }

    public static void search(Path path, String field, String queryStr) throws IOException, ParseException {
        Directory dir = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(dir); // index载入
        IndexSearcher indexSearch = new IndexSearcher(reader);// index search

        QueryParser parser = new QueryParser(field, new StandardAnalyzer()); // Query parser
        logger.info("reader.maxDoc: " + reader.maxDoc() + "\tRefCount: " + reader.getRefCount()
                + "\tdocCount: " + reader.getDocCount(field) + " 查询条件: " + field + "=" + queryStr);

        logger.info("========= search ============");
        Query query = parser.parse(queryStr); // parse query value
        TopDocs results = indexSearch.search(query, 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = indexSearch.doc(scoreDoc.doc);
            logger.info("docInfo: " + scoreDoc.toString() + " @@ shardIndex:" + scoreDoc.shardIndex + " docId: " + scoreDoc.doc
                    + " >> " + doc + "\tGet field contacts: " + doc.get("contacts"));
        }
        if (hits.length > 0) {
            logger.info("========= searchAfter ============");
            TopDocs results2 = indexSearch.searchAfter(hits[0], query, 10);
            ScoreDoc[] hits2 = results2.scoreDocs;
            int numTotalHits2 = results2.totalHits;
            logger.info(numTotalHits2 + " total matching documents");
            for (ScoreDoc scoreDoc : hits2) {
                Document doc = indexSearch.doc(scoreDoc.doc);
                logger.info("docInfo: " + scoreDoc.toString() + " @@ shardIndex:" + scoreDoc.shardIndex + " docId: " + scoreDoc.doc
                        + " >> " + doc + "\tGet field contacts: " + doc.get("contacts"));
            }
        }
    }

    public static void emptySearch(Path path) throws IOException, ParseException {
        Directory dir = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(dir); // index载入
        IndexSearcher indexSearch = new IndexSearcher(reader);// index search

        logger.info("索引信息 RefCount: " + reader.getRefCount() + " maxDoc: " + reader.maxDoc());
        for (LeafReaderContext leaf : reader.getContext().leaves()) {
            logger.info("\tdocBase: " + leaf.docBase + " ord: " + leaf.ord
                    + " " + leaf.toString());
        }

        logger.info("========= search ============");
        TopDocs results = indexSearch.search(new BooleanQuery.Builder().build(), 10);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            Document doc = indexSearch.doc(scoreDoc.doc);
            logger.info("docInfo: " + scoreDoc.toString() + " @@ shardIndex:" + scoreDoc.shardIndex + " docId: " + scoreDoc.doc
                    + " >> " + doc + "\tGet field contacts: " + doc.get("contacts"));
        }
    }
}
