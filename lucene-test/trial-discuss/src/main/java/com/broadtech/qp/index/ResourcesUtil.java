package com.broadtech.qp.index;

import com.broadtech.bdp.common.ctl.*;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.qp.index.status.RuntimeStatus;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Created on 2017/7/11.
 */
public class ResourcesUtil {
    private static final Logger logger = Logger.getLogger(ResourcesUtil.class);
    private final TableNameMapping tableNameMapping;
    private final RuntimeStatus status;
    private final List<RichCtlConfig> ctlList;

    public ResourcesUtil(RuntimeStatus status) throws Exception {
        logger.info("开始载入全局资源");
        this.status = status;
        String driverClass = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://master/jobAdmin";
        String root = "root";
        String pwd = "broadtech";
        CtlParser ctlParser = new CtlParser(driverClass, url, root, pwd);
        List<CtlConfig> _ctlList = ctlParser.load("lucene");
        ctlList = new ArrayList<>(_ctlList.size());
        for (CtlConfig ctl : _ctlList) {
            ctl.buildFieldTypeParsers();
            ctlList.add(new RichCtlConfig(ctl, new CtlValidate.Param()));
        }
        tableNameMapping = new TableNameMapping(new TableCodeMapping(driverClass, url, root, pwd), ctlList);
        logger.info("全局资源载入完成 => 载入可用数据表: " + _ctlList);
    }

    public List<RichCtlConfig> getCtlList() {
        return ctlList;
    }

    public RuntimeStatus getStatus() {
        return status;
    }

    public RichCtlConfig getCtl(Path path) {
        return tableNameMapping.getCtlByFileName(path.getFileName().toString());
    }

    public RichCtlConfig getCtlByCtlId(String ctlId) {
        for (RichCtlConfig ctl : ctlList) {
            if (ctl.getCtlConfig().ctlId.equals(ctlId)) return ctl;
        }
        return null;
    }

    public String getCtlId(Path path) {
        return tableNameMapping.getCtlByFileName(path.getFileName().toString()).getCtlConfig().ctlId;
    }

    public static byte[] transform(String hexStr) {
        if (hexStr.startsWith("0x")) {
            hexStr = hexStr.substring(2);
        }
        if (hexStr.length() % 2 != 0) {
            throw new IllegalArgumentException("参数不是标准的十六进制数据");
        }
        byte[] b = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length(); i++) {
            b[i / 2] = Byte.parseByte(hexStr.substring(i, ++i + 1), 16);
        }
        return b;
    }

    /**
     * 计算路径的大小
     *
     * @throws IOException
     */
    public static long getPathSize(Path resourceDir) throws IOException {
        AtomicLong count = new AtomicLong(0);
        Files.walkFileTree(resourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isDirectory(file)) {
                    count.addAndGet(Files.size(file));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return count.get();
    }

    /**
     * 删除目录|文件
     */
    public static void delete(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!Files.isDirectory(file)) Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
            Stream<Path> stream = Files.walk(path);
            Object[] paths = stream.toArray();
            for (int i = paths.length - 1; i >= 0; i--) {
                Files.deleteIfExists((Path) paths[i]);
            }
            stream.close();
        } else {
            Files.deleteIfExists(path);
        }
    }
}
