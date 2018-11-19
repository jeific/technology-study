package com.broadtech.qp.custom;


import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.PriorityQueue;

import java.io.IOException;

/**
 * Created by jeifi on 2017/8/3.
 */
public class CustomCollector implements Collector {
    PriorityQueue<ScoreDoc> pq;
    int totalHits = 0;

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
        pq = new CustomHitQueue(Math.min(context.reader().maxDoc(), 10));
        return new CustomLeafCollector(context, pq) {
            @Override
            public void collect(int doc) throws IOException {
                totalHits++;
                pqTop.doc = doc + docBase;
                pqTop.score = 0;   // 不计算分数
                pqTop = pq.updateTop();
            }
        };
    }

    @Override
    public boolean needsScores() {
        return true;
    }

    public TopDocs topDocs() {
        int start = totalHits < pq.size() ? totalHits : pq.size();
        for (; start < pq.size(); start++) {
            pq.pop(); // 删除多余的预初始项
        }
        ScoreDoc[] results = new ScoreDoc[totalHits];
        for (int i = 0; i < totalHits; i++) {
            results[i] = pq.pop();
        }
        return new TopDocs(totalHits, results, Float.NaN);
    }
}
