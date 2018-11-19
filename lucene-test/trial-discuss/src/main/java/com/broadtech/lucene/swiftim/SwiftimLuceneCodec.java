package com.broadtech.lucene.swiftim;

import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.lucene62.Lucene62Codec;

/**
 * * Implements the Lucene 6.2 index format, with configurable per-field postings
 * and docvalues formats.
 * <p>
 * If you want to reuse functionality of this codec in another codec, extend
 * {@link FilterCodec}.
 * 基于{@link Lucene62Codec}扩展
 *
 * @see org.apache.lucene.codecs.lucene60 package documentation for file format details.
 */
public class SwiftimLuceneCodec extends Lucene62Codec {
    private final DocValuesFormat defaultDVFormat = DocValuesFormat.forName("SwiftimLucene54");

    /**
     * 替换默认实现
     */
    @Override
    public DocValuesFormat getDocValuesFormatForField(String field) {
        return defaultDVFormat;
    }
}