package com.broadtech.learn.lucene.funresearch;

import com.broadtech.learn.lucene.SimpleIndexWriter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 测试多线程使用同一IndexWriter写出的segment情况<br>
 * 结论：每个线程产生一个Segment
 */
public class ParallelWriter {

    public static void main(String[] args) throws IOException, InterruptedException {
        String path = "indexes";
        File file = new File(path);
        System.out.println(file.getAbsolutePath() +
                "\nTotalSpac:" + (file.getTotalSpace() / 1024 / 1024 / 1024)
                + "G\nUsableSpace:" + (file.getUsableSpace() / 1024 / 1024 / 1024)
                + "G\nFreeSpac:" + (file.getFreeSpace() / 1024 / 1024 / 1024)
                + "G");

        long time = System.currentTimeMillis();
        IndexWriterConfig iwf = new IndexWriterConfig();
        iwf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        iwf.setRAMBufferSizeMB(128);
        iwf.setUseCompoundFile(true);
        SimpleIndexWriter writer = new SimpleIndexWriter(path, iwf);
        CountDownLatch cdl = new CountDownLatch(5);
        startService(writer, 1000 * 100, 5, cdl);
        cdl.await();
        writer.commitAndClose();
        System.out.println(Thread.currentThread().getName() + " >> 耗时: " + ((System.currentTimeMillis() - time) / 1000.0) + "s");
    }

    private static void insert(SimpleIndexWriter writer, int records) {
        while (records-- > 0) {
            try {
                Document doc0 = new Document();
                String nameValue = "Tom swift k 中华人民共和国 " + ((char) ((int) (Math.random() * 100 % 26) + 97));
                doc0.add(new SortedDocValuesField("name", new BytesRef(nameValue)));
                doc0.add(new NumericDocValuesField("zero", 0));
                long ageValue = (long) (Math.random() * 1000);
                doc0.add(new NumericDocValuesField("age", ageValue));
                double heightValue = Math.random() * 10000;
                doc0.add(new DoubleDocValuesField("height", heightValue));
                writer.addDocument(doc0);

                System.out.println(Thread.currentThread().getName() + " >> " + nameValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startService(SimpleIndexWriter writer, int records, int num, CountDownLatch cdl) {
        for (int i = 0; i < num; i++) {
            new Thread(() -> {
                insert(writer, records);
                cdl.countDown();
            }, "Insert_service_" + i).start();
        }
    }
}
