package com.snowlake.java.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class JavaChild extends JavaBase {

    public static void main(String[] args) throws IOException {
        JavaChild javaChild = new JavaChild();
        javaChild.m1();
        javaChild.m2();

        JavaBase javaBase = new JavaChild();
        javaBase.m();

        // 测试截取地址
        Path path = Paths.get(new File("").getAbsolutePath());
        Path prefix = Paths.get("F:\\workspace");
        System.out.println(path + "\n" + prefix);
        System.out.println(getRelativePath(prefix, path));
        Files.walkFileTree(Paths.get("F:\\workspace\\git_repo\\technology-study\\java-fun\\src\\main\\java\\com\\snowlake\\java\\util"),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println(file + "\t" + attrs);
                        return super.visitFile(file, attrs);
                    }
                });
    }

    public void m1() {
        this.total = 10;
    }

    public void m2() {
        System.out.println(this.total);
    }

    @Override
    public void m() {
        m1();
        m2();
    }

    /**
     * 从path下排除prefixPath部分，获得相对路径
     */
    public static String getRelativePath(Path prefixPath, Path path) {
        return path.toString().replace(prefixPath.toString() + File.separator, "");
    }
}
