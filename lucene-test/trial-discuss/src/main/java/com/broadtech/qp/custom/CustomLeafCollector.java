package com.broadtech.qp.custom;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.PriorityQueue;

import java.io.IOException;

/**
 * Created by jeifi on 2017/8/3.
 */
public abstract class CustomLeafCollector implements LeafCollector {
    protected final int docBase;
    protected ScoreDoc pqTop;

    public CustomLeafCollector(LeafReaderContext ctx, PriorityQueue<ScoreDoc> pq) {
        docBase = ctx.docBase;
        pqTop = pq.top();
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {

    }
}
