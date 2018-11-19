package com.broadtech.learn.lucene.funresearch;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * create by 2018/1/16 9:52<br>
 *
 * @author Yuanjun Chen
 */
public class DemoMerge {

    public static void main(String[] args) throws IOException {
        String path = "indexes/demoLucene71";
        IndexWriterConfig conf = new IndexWriterConfig();
        conf.setUseCompoundFile(true);
        conf.getMergePolicy().setNoCFSRatio(1);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        Directory dir = FSDirectory.open(Paths.get(path));
        IndexWriter writer = new IndexWriter(dir, conf);

        Directory dir_1 = FSDirectory.open(Paths.get(path + "_1"));
        Directory dir_2 = FSDirectory.open(Paths.get(path + "_2"));
        writer.addIndexes(dir_1, dir_2);
        dir_1.close();
        dir_2.close();
        writer.forceMerge(1);
        writer.close();
        dir.close();

        Directory readDir = FSDirectory.open(Paths.get(path));
        IndexReader reader = DirectoryReader.open(readDir);
        System.out.println(reader.numDocs());
        reader.close();
        readDir.close();
    }
}
