package com.broadtech.learn.lucene.custom.contains;

import org.apache.lucene.index.*;
import org.apache.lucene.search.DocIdSetIterator;

import java.io.IOException;

/**
 * 核心实现
 */
final class LikeDocIdSetIterator extends DocIdSetIterator {
    private final PostingsEnum[] postingsEnums;
    private int currDocId;
    private long docFreq = NO_MORE_DOCS;

    public LikeDocIdSetIterator(LeafReaderContext context, Term[] terms) throws IOException {
        PostingsEnum[] peSeq = new PostingsEnum[terms.length];
        LeafReader leafReader = context.reader();
        boolean findNotMatchTerm = false; // 记录是否存在没有匹配的Term
        if (terms.length == 1) {
            PostingsEnum pe = getPostingEnum(leafReader, terms[0], PostingsEnum.NONE);
            if (pe != null) {
                peSeq[0] = pe;
            } else {
                findNotMatchTerm = true;
            }
        } else {
            for (int i = 0; i < terms.length; i++) {
                PostingsEnum pe = getPostingEnum(leafReader, terms[i], PostingsEnum.POSITIONS);
                if (pe != null) {
                    peSeq[i] = pe;
                } else {
                    findNotMatchTerm = true;
                    break;
                }
            }
        }
        postingsEnums = findNotMatchTerm ? null : peSeq;
        currDocId = findNotMatchTerm ? NO_MORE_DOCS : -1;
        if (!findNotMatchTerm) {
            for (PostingsEnum pe : postingsEnums) {
                docFreq = Math.min(docFreq, pe.cost());
            }
        }
    }

    private PostingsEnum getPostingEnum(LeafReader leafReader, Term term, int flags) throws IOException {
        Terms _terms = leafReader.terms(term.field());
        if(_terms == null) return null;
        TermsEnum termEnum = _terms.iterator();
        if (termEnum.seekExact(term.bytes())) {
            return termEnum.postings(null, flags);
        } else {
            return null;
        }
    }

    public int freq() {
        return 0;
    }

    @Override
    public int docID() {
        return currDocId;
    }

    @Override
    public long cost() {
        return docFreq;
    }

    @Override
    public int advance(int target) throws IOException {
        if (postingsEnums.length == 1) {
            currDocId = postingsEnums[0].advance(target);
            return currDocId;
        }
        currDocId = positionMatch(target);
        return currDocId;
    }

    @Override
    public int nextDoc() throws IOException {
        if (postingsEnums.length == 1) {
            currDocId = postingsEnums[0].nextDoc();
            return currDocId;
        }
        currDocId = positionMatch(-1);
        return currDocId;
    }

    private int internalNextDoc(PostingsEnum pe, int target) throws IOException {
        return target == -1 ? pe.nextDoc() : pe.advance(target);
    }

    /**
     * 根据位置匹配计算合适的docId [kernel]
     *
     * @param target -1 表明是nextDoc()操作
     */
    private int positionMatch(int target) throws IOException {
        // 找到Terms里面当前位置的最大的docId (id是有序的且升序)
        int docIds[] = new int[postingsEnums.length];
        for (int i = 0; i < postingsEnums.length; i++) {
            docIds[i] = internalNextDoc(postingsEnums[i], target);
            if (docIds[i] == NO_MORE_DOCS) return NO_MORE_DOCS;
            currDocId = Math.max(currDocId, docIds[i]);
        }
        // 匹配位置
        if (docIds[0] != currDocId) {
            docIds[0] = postingsEnums[0].advance(currDocId);
            if (docIds[0] == NO_MORE_DOCS) return NO_MORE_DOCS;
            // 当前确立的docId在目前的Posting里面没有 则计算下一个docId
            if (docIds[0] != currDocId) return positionMatch(target);
        }
        int advanceRightPosition = postingsEnums[0].nextPosition();
        for (int i = 1; i < postingsEnums.length; i++) {
            if (docIds[i] != currDocId) {
                docIds[i] = postingsEnums[i].advance(currDocId);
                if (docIds[i] == NO_MORE_DOCS) return NO_MORE_DOCS;
                // 当前确立的docId在目前的Posting里面没有 则计算下一个docId
                if (docIds[i] != currDocId) return positionMatch(target);
            }
            advanceRightPosition = positionMatch(advanceRightPosition, postingsEnums[i]);
            if (advanceRightPosition == -1) {
                return NO_MORE_DOCS;
            }
        }
        return currDocId;
    }

    /**
     * 要求left至少存在一个位置 < right的位置
     *
     * @return 找到的最临近左边的位置
     */
    private int positionMatch(int leftPos, PostingsEnum right) throws IOException {
        int advanceRightPosition;
        for (int i = 0; i < right.freq(); i++) {
            advanceRightPosition = right.nextPosition();
            if (leftPos < advanceRightPosition) return advanceRightPosition;
        }
        return -1;
    }
}
