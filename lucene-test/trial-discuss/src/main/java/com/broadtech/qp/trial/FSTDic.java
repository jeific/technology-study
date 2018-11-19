package com.broadtech.qp.trial;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.fst.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class FSTDic {
    FST<Long> fst;
    FST.BytesReader fstReader;

    public FSTDic() throws IOException {
        File file = new File("fst");
        if (file.exists()) {
//            fst = load(file);
        } else {
            String[] words = {"中国", "中国人", "中国人民", "中国人民解放军"};
            long[] output = {5, 3, 8, 9};
            fst = build(words, output);
        }
        fstReader = fst.getBytesReader();
    }

    public void save() throws IOException {
        fst.save(Paths.get("fst"));
    }

    public FST<BytesRef> load(File file) throws IOException {
        return new FST<>(new InputStreamDataInput(new FileInputStream(file)), ByteSequenceOutputs.getSingleton());
    }

    private FST<Long> build(String[] keys, long[] values) throws IOException {
//        ByteSequenceOutputs outputs = ByteSequenceOutputs.getSingleton();
//        Builder<BytesRef> builder = new Builder<>(FST.INPUT_TYPE.BYTE4, outputs);
//        final IntsRefBuilder scratchIntsRef = new IntsRefBuilder();
//        BytesRef output = new BytesRef(4);
//
//        for (String word : words) {
//            NumericUtils.intToSortableBytes(word.length(), output.bytes, 0);
//            builder.add(Util.toUTF32(word, scratchIntsRef), BytesRef.deepCopyOf(output));
//        }
//        return builder.finish();

        PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
        Builder<Long> builder = new Builder<>(FST.INPUT_TYPE.BYTE1, outputs);
        BytesRef scratchBytes;
        IntsRefBuilder scratchInts = new IntsRefBuilder();
        for (int i = 0; i < keys.length; i++) {
            scratchBytes = new BytesRef(keys[i]);
            builder.add(Util.toIntsRef(scratchBytes, scratchInts), values[i]);
        }
        return builder.finish();
    }

//    public boolean contains(String word) throws IOException {
//        FST.Arc<BytesRef> scratchArc = new FST.Arc<>();
//        FST.Arc<BytesRef> target = new FST.Arc<>();
//        int bufUpto = 0, buflen = word.length();
//        BytesRef pendingOutput = fst.outputs.getNoOutput();
//        BytesRef matchOutput;
//        fst.getFirstArc(scratchArc);
//        while (bufUpto < buflen) {
//            int codePoint = Character.codePointAt(word, bufUpto);
//            if ((target = fst.findTargetArc(codePoint, scratchArc, scratchArc, fstReader)) != null) {
//                pendingOutput = fst.outputs.add(pendingOutput, target.output);
//            } else {
//                break;
//            }
//            bufUpto += Character.charCount(codePoint);
//        }
//        if (scratchArc.isFinal()) {
//            matchOutput = fst.outputs.add(pendingOutput, scratchArc.nextFinalOutput);
//            if (matchOutput.length > 0) {
//                int len = NumericUtils.sortableBytesToInt(matchOutput.bytes, 0);
//                System.out.println(len);
//                return true;
//            }
//        }
//        return false;
//    }

    public static void main(String[] args) throws IOException {
        FSTDic dic = new FSTDic();
        //  dic.save();
        String[] words = {"中国", "中国人", "中国人民", "中国人民解放军"};
        long[] output = {5, 3, 8, 9};
        
    }
}
