package com.broadtech.learn.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class SimpleIndexWriter {
    private IndexWriter indexWriter;

    public SimpleIndexWriter(String path) throws IOException {
        this(path, IndexWriterConfig.OpenMode.CREATE);
    }

    public SimpleIndexWriter(String path, IndexWriterConfig.OpenMode openMode) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(path));
        IndexWriterConfig iwf = new IndexWriterConfig();
        iwf.setOpenMode(openMode);
        iwf.setUseCompoundFile(false);
        indexWriter = new IndexWriter(dir, iwf);
    }

    public SimpleIndexWriter(String path, IndexWriterConfig iwf) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(path));
        indexWriter = new IndexWriter(dir, iwf);
    }

    public void addDocument(Document doc) throws IOException {
        indexWriter.addDocument(doc);
    }

    public void commitAndClose() throws IOException {
        indexWriter.close();
    }
}
