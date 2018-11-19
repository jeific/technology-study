package com.broadtech.qp.trial;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jeifi on 2017/7/30.
 */
public class IndexWriterTrial {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("indexes/doc_cf_false");
        Directory dir = FSDirectory.open(path);
        IndexWriter i1 = new IndexWriter(dir, new IndexWriterConfig());
        IndexWriter i2 = new IndexWriter(dir, new IndexWriterConfig());
    }
}
