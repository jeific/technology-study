package com.broadtech.qp.index.status;

import com.broadtech.bdp.common.ctl.CtlConfig;
import com.broadtech.bdp.common.util.Logger;
import com.broadtech.bdp.common.util.UnitHelper;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2017/7/12.
 */
public class RuntimeStatus {
    private ConcurrentHashMap<CtlConfig, RuntimeStatus> classicStatusMap = new ConcurrentHashMap<>();
    private long start = Clock.systemUTC().millis();
    private AtomicInteger businessCount = new AtomicInteger(0);
    private AtomicLong lastStaticTIme = new AtomicLong(Clock.systemUTC().millis());
    private AtomicLong bytes = new AtomicLong(0);
    private AtomicInteger lines = new AtomicInteger(0);
    private AtomicLong bytesTotal = new AtomicLong(0);
    private AtomicInteger linesTotal = new AtomicInteger(0);
    private String lashStatus;

    public void printStatus(Logger logger) {
        logger.info("=========================================");
        if (!classicStatusMap.isEmpty()) {
            classicStatusMap.forEach((k, v) -> printByStatus(logger, v, k.toString()));
        }
        printByStatus(logger, this, "总计 => ");
    }

    public String getLashStatus() {
        String status = lashStatus;
        lashStatus = "";
        return status;
    }

    private void printByStatus(Logger logger, RuntimeStatus status, String flag) {
        if (status.lines.get() == 0) return;
        long interval = (Clock.systemUTC().millis() - status.lastStaticTIme.getAndSet(Clock.systemUTC().millis())) / 1000;
        long totalInterval = (Clock.systemUTC().millis() - status.start) / 1000;
        lashStatus = flag + "最近" + interval + "s, 处理数据" + status.lines.getAndSet(0) + "条(" + UnitHelper.getHumanSize(status.bytes.getAndSet(0)) + ")记录" +
                ", 总计: 在" + totalInterval + "s内总共处理" + status.linesTotal.get() + "条(" + UnitHelper.getHumanSize(status.bytesTotal.get())
                + ")记录, IndexProcessorCount: " + status.businessCount.get();
        logger.info(lashStatus);
    }

    public void incBusiness() {
        businessCount.incrementAndGet();
    }

    private void decBusiness() {
        businessCount.decrementAndGet();
    }

    public void incBusiness(CtlConfig ctl) {
        RuntimeStatus classicStatus = this.classicStatusMap.get(ctl);
        if (classicStatus == null) {
            this.classicStatusMap.putIfAbsent(ctl, new RuntimeStatus());
            classicStatus = this.classicStatusMap.get(ctl);
        }
        classicStatus.incBusiness();
        incBusiness(); // 总计
    }

    public void decBusiness(CtlConfig ctl) {
        RuntimeStatus classicStatus = this.classicStatusMap.get(ctl);
        if (classicStatus != null) {
            classicStatus.decBusiness();
        }
        decBusiness(); // 总计
    }


    public void addLine(int lineBytes, int lineNum) {
        bytes.addAndGet(lineBytes);
        lines.addAndGet(lineNum);
        bytesTotal.addAndGet(lineBytes);
        linesTotal.addAndGet(lineNum);
    }

    public void addLine(CtlConfig ctl, int lineBytes, int lineNum) {
        RuntimeStatus classicStatus = this.classicStatusMap.get(ctl);
        if (classicStatus == null) {
            this.classicStatusMap.putIfAbsent(ctl, new RuntimeStatus());
            classicStatus = this.classicStatusMap.get(ctl);
        }
        addLine(lineBytes, lineNum);        // 总计
        classicStatus.addLine(lineBytes, lineNum); // 分类统计
    }
}
