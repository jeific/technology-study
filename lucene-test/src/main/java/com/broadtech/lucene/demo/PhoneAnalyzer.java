package com.broadtech.lucene.demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * create by 2018/2/9 17:43<br>
 *
 * @author Yuanjun Chen
 */
public class PhoneAnalyzer extends Analyzer {

    public static void main(String[] args) throws Exception {
        List<String> analyseTerms = new ArrayList<>();
        Analyzer analyzer = new PhoneAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("k", "13512567896");
        // 提取Token流字符Term
        CharTermAttribute charTerm = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            analyseTerms.add(charTerm.toString());
        }
        tokenStream.close();
        analyzer.close();
        System.out.println(analyseTerms.size() + "\t" + analyseTerms);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer src = new Tokenizer() {
            private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
            private int posMark = 0;

            @Override
            public boolean incrementToken() throws IOException {
                clearAttributes();
                boolean next = false;
                if (posMark == 0) {
                    next = true;
                    toPart(0, 3);
                    posMark = 1;
                } else if (posMark == 1) {
                    next = true;
                    toPart(3, 7);
                    posMark = 2;
                } else if (posMark == 2) {
                    next = true;
                    toPart(7, 20);
                    posMark = 3;
                }

                return next;
            }

            private void toPart(int start, int end) throws IOException {
                char[] chs = new char[end - start];
                int len = input.read(chs, 0, chs.length);
                for (int i = 0; i < len; i++) {
                    Character.toChars(Character.toLowerCase(chs[i]), termAtt.buffer(), i);
                }
                termAtt.setLength(len);
            }
        };
        TokenStream tok = new StandardFilter(src);
        return new TokenStreamComponents(src, tok);
    }
}
