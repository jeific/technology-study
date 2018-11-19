package com.broadtech.qp.custom;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.PriorityQueue;

/**
 * Created by jeifi on 2017/8/3.
 */
final class CustomHitQueue extends PriorityQueue<ScoreDoc> {
    public CustomHitQueue(int maxSize) {
        super(maxSize, true);
    }

    @Override
    protected ScoreDoc getSentinelObject() {
        return new ScoreDoc(Integer.MAX_VALUE, Float.NEGATIVE_INFINITY);
    }

    @Override
    protected boolean lessThan(ScoreDoc hitA, ScoreDoc hitB) {
        if (hitA.score == hitB.score)
            return hitA.doc > hitB.doc;
        else
            return hitA.score < hitB.score;
    }
}
