package com.snowlake.java.io;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.GroupFactory;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetReader.Builder;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.api.DelegatingReadSupport;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.apache.parquet.schema.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ParquetWriterDemo {

    public static void main(String[] args) throws Exception {
        parquetReader("/tmp/root/applos");
    }

    static void parquetReaderV2(String inPath) throws Exception {
        GroupReadSupport readSupport = new GroupReadSupport();
        Builder<Group> reader = ParquetReader.builder(readSupport, new Path(inPath));
        ParquetReader<Group> build = reader.build();
        Group line = null;
        while ((line = build.read()) != null) {
            Group time = line.getGroup("time", 0);
            //通过下标和字段名称都可以获取
            /*System.out.println(line.getString(0, 0)+"\t"+
            line.getString(1, 0)+"\t"+
            time.getInteger(0, 0)+"\t"+
            time.getString(1, 0)+"\t");*/
            System.out.println(line.getString("city", 0) + "\t" +
                    line.getString("ip", 0) + "\t" +
                    time.getInteger("ttl", 0) + "\t" +
                    time.getString("ttl2", 0) + "\t");
            //System.out.println(line.toString());
        }
        System.out.println("读取结束");
    }

    //新版本中new ParquetReader()所有构造方法好像都弃用了,用上面的builder去构造对象
    static void parquetReader(String inPath) throws Exception {
        FileSystem fs = FileSystem.get(new Configuration());
        ParquetMetadata readFooter = ParquetFileReader.readFooter(fs.getConf(), new Path(inPath), ParquetMetadataConverter.NO_FILTER);
        MessageType schemaType = readFooter.getFileMetaData().getSchema();
        List<Type> colTypes = schemaType.getFields();
        System.out.println(colTypes);


        GroupReadSupport readSupport = new GroupReadSupport();
        String schema = "message row {optional binary clientIp (UTF8);}";
        MessageType messageType = MessageTypeParser.parseMessageType(schema);
        readSupport.init(new Configuration(), null, messageType);

        DelegatingReadSupport delegatingReadSupport = new DelegatingReadSupport(readSupport);
        ParquetReader<Group> reader = new ParquetReader<>(new Path(inPath), delegatingReadSupport);
        ParquetMetadata footer = ParquetFileReader.readFooter(new Configuration()
                , new Path(inPath), ParquetMetadataConverter.NO_FILTER);
        System.out.println(footer);

//        ParquetReader<Group> reader = ParquetReader.builder(new GroupReadSupport(), new Path(inPath))
//                .withConf(fs.getConf()).build();

        Group line;
        int i = 0;
        while ((line = reader.read()) != null) {
            if (i++ > 1996) {
                System.out.println(i);
            }
            System.out.println(line.toString());
        }
        System.out.println("读取结束");
        fs.close();
    }

    /**
     * @param outPath 　　输出Parquet格式
     * @param inPath  输入普通文本文件
     * @throws IOException
     */
    static void parquetWriter(String outPath, String inPath) throws IOException {
        MessageType schema = MessageTypeParser.parseMessageType("message Pair {\n" +
                " required binary city (UTF8);\n" +
                " required binary ip (UTF8);\n" +
                " repeated group time {\n" +
                " required int32 ttl;\n" +
                " required binary ttl2;\n" +
                "}\n" +
                "}");
        GroupFactory factory = new SimpleGroupFactory(schema);
        Path path = new Path(outPath);
        Configuration configuration = new Configuration();
        GroupWriteSupport writeSupport = new GroupWriteSupport();
        writeSupport.setSchema(schema, configuration);
        ParquetWriter<Group> writer = new ParquetWriter<Group>(path, configuration, writeSupport);
//把本地文件读取进去，用来生成parquet格式文件
        BufferedReader br = new BufferedReader(new FileReader(new File(inPath)));
        String line = "";
        Random r = new Random();
        while ((line = br.readLine()) != null) {
            String[] strs = line.split("\\s+");
            if (strs.length == 2) {
                Group group = factory.newGroup()
                        .append("city", strs[0])
                        .append("ip", strs[1]);
                Group tmpG = group.addGroup("time");
                tmpG.append("ttl", r.nextInt(9) + 1);
                tmpG.append("ttl2", r.nextInt(9) + "_a");
                writer.write(group);
            }
        }
        System.out.println("write end");
        writer.close();
    }
}
