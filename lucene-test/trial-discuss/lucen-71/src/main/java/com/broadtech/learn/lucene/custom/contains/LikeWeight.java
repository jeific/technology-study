package com.broadtech.learn.lucene.custom.contains;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.Set;

final class LikeWeight extends Weight {
    private final Term[] terms;

    protected LikeWeight(Query query, Term[] terms) {
        super(query);
        this.terms = terms;
    }

    @Override
    public void extractTerms(Set<Term> terms) {

    }

    @Override
    public Explanation explain(LeafReaderContext context, int doc) throws IOException {
        return null; // 涉及计分时候需要实现
    }

    @Override
    public Scorer scorer(LeafReaderContext context) throws IOException {
        return new LikeScorer(this, new LikeDocIdSetIterator(context,terms)); // 将DocId迭代器传递到scorer
    }
}
