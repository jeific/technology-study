package com.broadtech.lucene;

import com.broadtech.common.util.Logger;
import com.broadtech.common.util.ThreadAssistant;
import com.broadtech.common.util.TimeCounter;
import com.broadtech.common.util.UnitHelper;
import com.broadtech.swiftim.common.context.SwiftimKeys;
import com.broadtech.swiftim.common.util.SwiftimCommonUtil;
import com.broadtech.swiftim.store.io.BufferedHdfsDirectory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.solr.store.hdfs.HdfsLockFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * create by 2018/2/1 10:03<br>
 *
 * @author Yuanjun Chen
 */
public class OnlyOpen {
    private static final Logger logger = Logger.getLogger(OnlyOpen.class);

    public static void main(String[] args) throws IOException {
        String path = args[0];
        FileSystem fs = SwiftimCommonUtil.getNewFileSystem();
        Path indexPath = new Path(path);
        logger.info(path + " => " + UnitHelper.getHumanSize(SwiftimCommonUtil.du(fs, indexPath)));
        OnlyOpen onlyOpen = new OnlyOpen();

        for (int i = 1; i <= 20; i++) {
            int buffer = 1024 * i;
            logger.info("\n\n===============  buffer=" + i + "KB =================");
            onlyOpen.inputStream(fs, indexPath, buffer);
            onlyOpen.swiftimInput(fs.getConf(), indexPath, buffer);
            onlyOpen.solrInput(fs.getConf(), indexPath, buffer);
            ThreadAssistant.sleep(5, TimeUnit.SECONDS);
        }

        fs.close();
    }


    private void inputStream(FileSystem fs, Path path, int buffer) {
        TimeCounter counter = new TimeCounter();
        try {
            FileStatus[] list = fs.listStatus(path);
            for (FileStatus status : list) {
                try (InputStream ins = fs.open(status.getPath(), buffer)) {
                    logger.info("FileSystem.open(Path, int) cost:" + counter.humanCost() + " => " + status.getPath().toUri().toString());
                } catch (IOException e) {
                    logger.error(e.toString(), e);
                }
            }
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    private void swiftimInput(Configuration conf, Path path, int buffer) {
        try {
            System.setProperty(SwiftimKeys.OPTION_LUCENE_READER_BUFFER_BYTES, String.valueOf(buffer));
            TimeCounter counter = new TimeCounter();
            BufferedHdfsDirectory directory = new BufferedHdfsDirectory(path, conf);
            IndexReader reader = DirectoryReader.open(directory);
            logger.info("open swiftim index reader cost:" + counter.humanCost());
            reader.close();
            directory.close();
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }

    private void solrInput(Configuration conf, Path path, int buffer) {
        try {
            TimeCounter counter = new TimeCounter();
            org.apache.solr.store.hdfs.HdfsDirectory hdfsDirectory =
                    new org.apache.solr.store.hdfs.HdfsDirectory(path
                            , HdfsLockFactory.INSTANCE, conf, buffer);
            IndexReader reader = DirectoryReader.open(hdfsDirectory);
            logger.info("open solr index reader cost:" + counter.humanCost());
            IndexSearcher searcher = new IndexSearcher(reader);
            logger.info(String.valueOf(searcher.getIndexReader().maxDoc()));
            reader.close();
            hdfsDirectory.close();
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
    }
}
