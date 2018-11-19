package com.broadtech.qp.trial;

import com.broadtech.bdp.common.util.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Chen Yuanjun on 2017/7/13.
 */
public class DocValuesTrial {
    private static final Logger logger = Logger.getLogger(DocValuesTrial.class);

    public static void main(String[] args) throws IOException, ParseException {
        Path path = Paths.get("indexes/doc_cf_false");
        logger.info("");
        logger.info(path.toAbsolutePath().toString());

        String[] fields = {"name", "age", "contacts", "enjoy", "sport", "noToken", "DocValue"};
        String[][] docs = {
                {"joch", "23", "2", "I like read book.", "true", "www.baidu.con", "34"}
                , {"Tom", "25", "3", "non", "false", "", "47"}
                , {"joch jeific", "32", "1", "books,music,play,read; Read is pleasant", "true", "", "12"}
                , {"tianzuo", "0", "1", "anythings", "true", "games.qq.com/?pgv_ref=aio2015", "96"}};

//        index(path, fields, docs);
        search(path, "name", "joch");
//        search(path, "enjoy", "read");
//        search(path, "age", "25");
//        search(path, "contacts", "1");
//        search(path, "age", "26"); // expect : not found
    }

    private static void index(Path path, String[] fields, String[][] docs) throws IOException {
        Directory dir = FSDirectory.open(path);
        IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
        iwc.setUseCompoundFile(false);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
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
        notokenType.setStored(true);
        notokenType.setIndexOptions(IndexOptions.DOCS);
        notokenType.freeze();

        for (String[] _doc : docs) {
            Document doc = new Document();
            doc.add(new StringField(fields[0], _doc[0], Field.Store.YES));
            doc.add(new IntPoint(fields[1], Integer.valueOf(_doc[1])));
            doc.add(new IntPoint(fields[2], Integer.valueOf(_doc[2]))); // provided N-dimensional int point
            doc.add(new StoredField(fields[2], Integer.valueOf(_doc[2]))); // stored-only field
            doc.add(new Field(fields[3], _doc[3], typeAll));
            doc.add(new Field(fields[4], _doc[4], type));

            doc.add(new Field(fields[5], _doc[5].getBytes(), notokenType));
            doc.add(new SortedDocValuesField(fields[5], new BytesRef(_doc[5].getBytes())));

            doc.add(new DoubleDocValuesField(fields[6], Double.valueOf(_doc[6])));
            doc.add(new StoredField(fields[6], Double.valueOf(_doc[6])));
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

        Query query = parser.parse(queryStr); // parse query value
        TopDocs results = indexSearch.search(query, 100);
        ScoreDoc[] hits = results.scoreDocs;
        int numTotalHits = results.totalHits;
        logger.info(numTotalHits + " total matching documents");
        for (ScoreDoc scoreDoc : hits) {
            logger.info(scoreDoc.toString());
            Document doc = indexSearch.doc(scoreDoc.doc);
            logger.info(scoreDoc.shardIndex + " " + doc + "\tGet field contacts: " + doc.get("contacts"));
            for (LeafReaderContext leaf : reader.leaves()) {
                logger.info(">> " + scoreDoc.doc + " noToken: " + new String(DocValues.getSorted(leaf.reader(), "noToken").get(scoreDoc.doc).bytes) + "\t" + doc.get("noToken"));
                logger.info(">> " + scoreDoc.doc + " DocValue: " + Double.longBitsToDouble(DocValues.getNumeric(leaf.reader(), "DocValue").get(scoreDoc.doc)) + "\t" + doc.get("DocValue"));
            }
        }
        logger.info(reader.maxDoc() + "");

        // searchAfter
//        TopDocs results2 = indexSearch.searchAfter(hits[0], query, 10);
//        ScoreDoc[] hits2 = results2.scoreDocs;
//        int numTotalHits2 = results2.totalHits;
//        logger.info(numTotalHits2 + " total matching documents");
//        for (ScoreDoc scoreDoc : hits2) {
//            logger.info(scoreDoc.toString());
//            Document doc = indexSearch.doc(scoreDoc.doc);
//            logger.info(scoreDoc.shardIndex + " " + doc + "\tGet field contacts: " + doc.get("contacts"));
//        }
    }
}
