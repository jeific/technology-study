package org.apache.lucene.codecs.lucene54;

import org.apache.lucene.codecs.DocValuesConsumer;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.index.SegmentWriteState;

import java.io.IOException;

/**
 * 重写{@link Lucene54DocValuesFormat {@link #fieldsProducer(SegmentReadState)}}接口
 */
public class SwiftimLucene54DocValuesFormat extends DocValuesFormat {

    /**
     * Sole Constructor
     */
    public SwiftimLucene54DocValuesFormat() {
        super("SwiftimLucene54");
    }

    @Override
    public DocValuesConsumer fieldsConsumer(SegmentWriteState state) throws IOException {
        return new Lucene54DocValuesConsumer(state, Lucene54DocValuesFormat.DATA_CODEC, Lucene54DocValuesFormat.DATA_EXTENSION, Lucene54DocValuesFormat.META_CODEC, Lucene54DocValuesFormat.META_EXTENSION);
    }

    @Override
    public DocValuesProducer fieldsProducer(SegmentReadState state) throws IOException {
        return new SwiftimLucene54DocValuesProducer(state, Lucene54DocValuesFormat.DATA_CODEC, Lucene54DocValuesFormat.DATA_EXTENSION, Lucene54DocValuesFormat.META_CODEC, Lucene54DocValuesFormat.META_EXTENSION);
    }
}
