package com.mrxu.stucomplarear2.utils;

public class IdWorker {

    private final long workerId;
    private final long datacenterId;

    public IdWorker(long workerId, long datacenterId) {
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public long nextId() {
        return com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
    }
}
