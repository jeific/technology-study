package com.broadtech.lucene.research;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class SimpleIndexSearcher {
    protected IndexReader reader;
    protected IndexSearcher searcher;

    public SimpleIndexSearcher(String path) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(path));
        reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
    }

    public TopDocs query(Query query) throws IOException {
        return searcher.search(query, 100);
    }

    public IndexReader getReader() {
        return reader;
    }

    public void close() throws IOException {
        reader.close();
        searcher = null;
        reader = null;
    }
}
