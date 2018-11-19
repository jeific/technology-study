package com.broadtech.lucene.demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

/**
 * create by 2018/2/9 17:43<br>
 *
 * @author Yuanjun Chen
 */
public class CharAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer src = new Tokenizer() {
            private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

            @Override
            public boolean incrementToken() throws IOException {
                clearAttributes();
                boolean next = false;
                while (true) {
                    int ch = input.read();
                    if (ch <= 0) break;
                    if (Character.isLetter(ch) || Character.isDigit(ch)) {
                        Character.toChars(Character.toLowerCase(ch), termAtt.buffer(), 0);
                        termAtt.setLength(1);
                        next = true;
                        break;
                    }
                }

                return next;
            }
        };
        TokenStream tok = new StandardFilter(src);
        return new TokenStreamComponents(src, tok);
    }
}
