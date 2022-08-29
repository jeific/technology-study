package com.snowlake.java.io;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class StringWriter {
    public static void main(String[] args) throws IOException {
        try (InputStream stream = StringWriter.class.getResourceAsStream("/assembly.xml")) {
            byte[] data = IOUtils.readFully(stream, stream.available());
            System.out.println(new String(data));
        }

    }
}
