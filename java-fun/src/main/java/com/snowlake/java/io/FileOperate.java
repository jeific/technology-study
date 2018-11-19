package com.snowlake.java.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class FileOperate {

    public static void main(String[] args) throws IOException, InterruptedException {
        File f = new File("file_operate_file.dat");
        OutputStream outs = new FileOutputStream(f);
        TimeUnit.SECONDS.sleep(5);
        int count = 1;
        while (count-- > 0) {
            outs.write("IOException\n".getBytes(StandardCharsets.UTF_8));
        }
        outs.close();

        Path path = f.toPath();
        BasicFileAttributeView attrView = Files.getFileAttributeView(path, BasicFileAttributeView.class,
                LinkOption.NOFOLLOW_LINKS);
        BasicFileAttributes attr = attrView.readAttributes();
        DateFormat df = new SimpleDateFormat("yyyyMMdd/HH:mm:ss");
        File file2 = new File(f.getAbsolutePath());
        System.out.println("create:" + attr.creationTime().toMillis() + " " + df.format(attr.creationTime().toMillis()));
        System.out.println("lastAccess:" + attr.lastAccessTime().toMillis() + " " + df.format(attr.lastAccessTime().toMillis()));
        System.out.println("lastModified:" + attr.lastModifiedTime().toMillis() + " " + df.format(attr.lastModifiedTime().toMillis()));
        file2.delete();
    }

}
