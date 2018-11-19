package com.broadtech.google.protobuf;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProtoBufTest {

    public static void main(String[] args) throws IOException {
        HelloWorldOuterClass.HelloWorld.Builder builder = HelloWorldOuterClass.HelloWorld.newBuilder();
        builder.setId(12);
        builder.setName("Test Tom");
        builder.setEmail("jeific@163.com");
        builder.addFriends("SnowLake");
        builder.addFriends("jeific");

        HelloWorldOuterClass.HelloWorld person = builder.build();
        // 将数据写到输出流，如网络输出流，这里就用ByteArrayOutputStream来代替
        ByteOutputStream outs = new ByteOutputStream();
        person.writeDelimitedTo(outs);

        // 接收到流并读取，如网络输入流，这里用ByteArrayInputStream来代替
        ByteArrayInputStream input = new ByteArrayInputStream(outs.getBytes());
        HelloWorldOuterClass.HelloWorld readerParse =
                HelloWorldOuterClass.HelloWorld.parseDelimitedFrom(input);

        System.out.println(readerParse.toString() + "\nObject Size: " + outs.getCount());
        objSerialize();
    }

    public static void objSerialize() throws IOException {
        _HelloWorld hw = new _HelloWorld();
        hw.id = 12;
        hw.name = "Test Tom";
        hw.email = "jeific@163.com";
        hw.addFriend("SnowLake");
        hw.addFriend("jeific");

        ByteOutputStream byteOutputStream = new ByteOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(byteOutputStream);
        output.writeObject(hw);
        System.out.println("HW count: " + byteOutputStream.getCount());
    }


    static class _HelloWorld implements Serializable {
        int id;
        String name;
        String email;
        List<String> friends = new ArrayList<>();

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public List<String> getFriends() {
            return friends;
        }

        public void addFriend(String friend) {
            this.friends.add(friend);
        }
    }
}
