package com.broadtech.qp.index;

import com.broadtech.bdp.common.util.UnitHelper;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2017/7/12.
 */
public class RuntimeStatus {
    private long start = Clock.systemUTC().millis();
    private AtomicInteger businessCount = new AtomicInteger(0);
    private AtomicLong lastStaticTIme = new AtomicLong(Clock.systemUTC().millis());
    private AtomicInteger bytes = new AtomicInteger(0);
    private AtomicInteger lines = new AtomicInteger(0);
    private AtomicInteger bytesTotal = new AtomicInteger(0);
    private AtomicInteger linesTotal = new AtomicInteger(0);

    public String printStatus() {
        long interval = (Clock.systemUTC().millis() - lastStaticTIme.getAndSet(Clock.systemUTC().millis())) / 1000;
        long totalInterval = (Clock.systemUTC().millis() - start) / 1000;
        return "最近" + interval + "s, 处理数据" + lines.getAndSet(0) + "条(" + UnitHelper.getHumanSize(bytes.getAndSet(0)) + ")记录" +
                ", 总计: 在" + totalInterval + "s内总共处理" + linesTotal.get() + "条(" + UnitHelper.getHumanSize(bytesTotal.get())
                + ")记录, IndexProcessorCount: " + businessCount.get();
    }

    public void addBusiness() {
        businessCount.incrementAndGet();
    }

    public void addLine(int lineBytes) {
        bytes.addAndGet(lineBytes);
        lines.incrementAndGet();
        bytesTotal.addAndGet(lineBytes);
        linesTotal.incrementAndGet();
    }
}
