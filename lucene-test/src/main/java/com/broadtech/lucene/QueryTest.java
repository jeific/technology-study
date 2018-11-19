package com.broadtech.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;

/**
 * create by 2018/2/7 10:56<br>
 *
 * @author Yuanjun Chen
 */
public class QueryTest {

    public static void main(String[] args) throws Exception {
        BooleanQuery.Builder queryBuild = new BooleanQuery.Builder();
        queryBuild.add(new BooleanClause(new TermQuery(new Term("k", "v")), BooleanClause.Occur.MUST));
        queryBuild.add(new BooleanClause(new TermQuery(new Term("day", "20180206")), BooleanClause.Occur.MUST));
        queryBuild.add(new BooleanClause(IntPoint.newRangeQuery("k2", 2, 6), BooleanClause.Occur.MUST_NOT));
        System.out.println(queryBuild.build());

        Analyzer analyzer = new StandardAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("k1", "127.128.66.15");
        // 提取Token流字符Term
        CharTermAttribute charTerm = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            System.out.println(charTerm.toString());
        }
        tokenStream.close();
    }
}
