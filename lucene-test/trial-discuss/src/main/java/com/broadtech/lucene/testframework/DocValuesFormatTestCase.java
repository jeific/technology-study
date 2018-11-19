package com.broadtech.lucene.testframework;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.index.BaseDocValuesFormatTestCase;

import java.io.IOException;

/**
 * Created by jeifi on 2017/8/17.
 */
public class DocValuesFormatTestCase extends BaseDocValuesFormatTestCase {
    @Override
    protected Codec getCodec() {
        return Codec.getDefault();
    }

    public void testEntrance() throws IOException {
        testDocValuesSimple();
    }
}
