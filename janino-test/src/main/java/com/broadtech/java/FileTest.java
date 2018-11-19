package com.broadtech.java;

import java.io.File;
import java.io.IOException;

public class FileTest {

    public static void main(String[] args) throws IOException {
        File f = new File("D:\\WorkSpace\\TestProject\\janino-test\\src\\main\\java\\com\\broadtech\\java\\FileTest.java");
        System.out.println("getAbsolutePath: " + f.getAbsolutePath());
        System.out.println("getCanonicalPath: " + f.getCanonicalPath());
    }
}
