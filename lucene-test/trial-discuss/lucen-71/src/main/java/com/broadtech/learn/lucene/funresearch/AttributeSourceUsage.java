package com.broadtech.learn.lucene.funresearch;

import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeSource;

import java.util.Iterator;

public class AttributeSourceUsage {

    public static void main(String[] args) {
        AttributeSource as = new AttributeSource();
        as.addAttribute(PositionIncrementAttribute.class);
        //as.addAttributeImpl(new PackedTokenAttributeImpl());
        //AttributeSource.State state = as.captureState();
        //as.restoreState(state);

        Iterator<AttributeImpl> itr = as.getAttributeImplsIterator();
        while (itr.hasNext()) {
            System.out.println("itr => " + itr.next().reflectAsString(true));
        }
        System.out.println(as.reflectAsString(true));
    }
}
