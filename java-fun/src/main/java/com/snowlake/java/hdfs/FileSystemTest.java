package com.snowlake.java.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

/**
 * classpath下面，是否存在core-site.xml,hdfs-site.xml
 * 存在时候FileSystem=org.apache.hadoop.fs.LocalFileSystem
 * 存在则为FileSystem=org.apache.hadoop.hdfs.DistributedFileSystem
 */
public class FileSystemTest {

    public static void main(String[] args) throws IOException {
        FileSystem fs = FileSystem.get(new Configuration());
        System.out.println(fs.getClass());
    }
}
