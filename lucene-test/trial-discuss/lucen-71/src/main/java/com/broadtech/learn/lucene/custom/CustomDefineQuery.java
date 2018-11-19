package com.broadtech.learn.lucene.custom;

import com.broadtech.learn.lucene.custom.contains.LikeQuery;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class CustomDefineQuery {

    public static void main(String[] args) throws IOException {
        String path = "indexes/bigdata";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(path)));
        IndexSearcher searcher = new IndexSearcher(reader);
        likeQuery(reader, searcher);
        reader.close();
    }

    private static void likeQuery(IndexReader reader, IndexSearcher searcher) throws IOException {
        Query query = new LikeQuery(new StandardAnalyzer(), "age", "tom","中华","u");
        Query query2 = new TermQuery(new Term("name"));
        TopDocs topDocs = searcher.search(query, 10);
        System.out.println(reader.maxDoc() + "\t" + topDocs.totalHits + "\t" + Arrays.toString(topDocs.scoreDocs));
    }
}
