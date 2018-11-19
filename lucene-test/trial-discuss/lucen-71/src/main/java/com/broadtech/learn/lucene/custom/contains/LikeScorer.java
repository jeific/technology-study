package com.broadtech.learn.lucene.custom.contains;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.io.IOException;

final class LikeScorer extends Scorer {
    private final LikeDocIdSetIterator docIdSetIterator;

    protected LikeScorer(Weight weight, LikeDocIdSetIterator likeDocIdSetIterator) {
        super(weight);
        this.docIdSetIterator = likeDocIdSetIterator;
    }

    @Override
    public int docID() {
        return docIdSetIterator.docID();
    }

    @Override
    public float score() throws IOException {
        return 0;
    }

    @Override
    public int freq() throws IOException {
        return docIdSetIterator.freq();
    }

    @Override
    public DocIdSetIterator iterator() {
        return docIdSetIterator;
    }
}
