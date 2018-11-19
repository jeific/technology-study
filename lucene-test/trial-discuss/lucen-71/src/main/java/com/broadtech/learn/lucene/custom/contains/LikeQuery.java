package com.broadtech.learn.lucene.custom.contains;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final public class LikeQuery extends Query {
    private Term[] terms;

    public LikeQuery(Analyzer analyzer, String field, String... terms) throws IOException {
        List<String> analyseTerms = new ArrayList<>();
        for (String term : terms) {
            TokenStream tokenStream = analyzer.tokenStream(field, term);
            CharTermAttribute charTerm = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                analyseTerms.add(charTerm.toString());
            }
            tokenStream.close();
        }
        this.terms = new Term[analyseTerms.size()];
        for (int i = 0; i < analyseTerms.size(); i++) {
            this.terms[i] = new Term(field, analyseTerms.get(i));
        }
    }

    @Override
    public String toString(String field) {
        StringBuilder builder = new StringBuilder();
        for (Term term : terms) {
            if (term.field().equals(field)) {
                builder.append(term.bytes().utf8ToString()).append("%");
            }
        }
        if (builder.length() > 0)
            builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LikeQuery && Arrays.equals(terms, ((LikeQuery) obj).terms);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        return new LikeWeight(this, terms);
    }
}

