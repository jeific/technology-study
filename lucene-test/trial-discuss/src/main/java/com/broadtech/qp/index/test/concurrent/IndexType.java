package com.broadtech.qp.index.test.concurrent;

/**
 * Created on 2017/7/25.
 */
public enum IndexType {
    DOCS(0), DOCS_STORED(1),
    DOCS_AND_FREQS_STORE(2),
    DOCS_AND_FREQS_AND_POSITIONS_STORE(3),
    DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS_STORE(4),
    DOCS_AND_FREQS_AND_POSITIONS_DOCVALUES(5),
    DOCS_DOCVALUES(6);

    int value;

    IndexType(int i) {
        this.value = i;
    }

    public static IndexType parse(int type) {
        for (IndexType element : IndexType.values()) {
            if (element.value == type) return element;
        }
        throw new IllegalArgumentException("unsupported type: " + type);
    }
}
