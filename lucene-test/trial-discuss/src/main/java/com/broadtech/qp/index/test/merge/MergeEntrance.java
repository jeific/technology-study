package com.broadtech.qp.index.test.merge;

import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.TimeCounter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

/**
 * Created by jeifi on 2017/8/17.
 */
public class MergeEntrance {
    private static Logger logger = Logger.getLogger(MergeEntrance.class);

    public static void main(String[] args) {
        if (args.length != 2 && args.length != 3) {
            logger.error("用法错误 USAGE: <mergeToPath> <maxNumSegments> [dirs]");
            return;
        }
        try {
            Directory mergeToPath = FSDirectory.open(Paths.get(args[0]));
            int maxNumSegments = Integer.parseInt(args[1]);
            Directory[] dirPaths = null;
            if (args.length == 3) {
                String[] dirs = args[2].split(",");
                dirPaths = new Directory[dirs.length];
                for (int i = 0; i < dirs.length; i++) {
                    dirPaths[i] = FSDirectory.open(Paths.get(dirs[i]));
                }
            }
            IndexWriterConfig iwc = new IndexWriterConfig();
            iwc.setRAMBufferSizeMB(512);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            iwc.setUseCompoundFile(false);

            TimeCounter timeCounter = new TimeCounter();
            IndexWriter indexWriter = new IndexWriter(mergeToPath, iwc);
            logger.info("打开IndexWriter => " + mergeToPath.toString() + " 耗时" + timeCounter.humanCost());
            if (dirPaths != null) {
                timeCounter.reset();
                logger.info("indexWriter.addIndexes ...");
                indexWriter.addIndexes(dirPaths);
                logger.info("indexWriter.addIndexes => " + args[2] + " 耗时" + timeCounter.humanCost());
            }
            if (maxNumSegments > 1) {
                timeCounter.reset();
                logger.info("indexWriter.forceMerge(" + maxNumSegments + ") ...");
                indexWriter.forceMerge(maxNumSegments);
                logger.info("indexWriter.forceMerge(" + maxNumSegments + ") 耗时" + timeCounter.humanCost());
            }

            timeCounter.reset();
            logger.info("indexWriter.commit() ...");
            indexWriter.commit();
            logger.info("indexWriter.commit() 耗时" + timeCounter.humanCost());

            timeCounter.reset();
            logger.info("indexWriter.close() ...");
            indexWriter.close();
            logger.info("indexWriter.close() 耗时" + timeCounter.humanCost());
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }
}
