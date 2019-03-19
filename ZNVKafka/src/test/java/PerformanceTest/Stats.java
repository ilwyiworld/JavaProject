package PerformanceTest;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ct on 2016-07-13.
 */
public class Stats {
    private long start;
    private long windowStart;
    private int[] latencies;
    private int sampling;
    private int iteration;
    private int index;
    private long count;
    private long bytes;
    private int maxLatency;
    private long totalLatency;
    private long windowCount;
    private int windowMaxLatency;
    private long windowTotalLatency;
    private long windowBytes;
    private long reportingInterval;
    private AtomicLong totalMessagesSent;

    public Stats(long numRecords, int reportingInterval, AtomicLong totalMessagesSent) {
        this.start = System.currentTimeMillis();
        this.windowStart = System.currentTimeMillis();
        this.index = 0;
        this.iteration = 0;
        this.sampling = (int) (numRecords / Math.min(numRecords, 500000));
        this.latencies = new int[(int) (numRecords / this.sampling) + 1];
        this.index = 0;
        this.maxLatency = 0;
        this.totalLatency = 0;
        this.windowCount = 0;
        this.windowMaxLatency = 0;
        this.windowTotalLatency = 0;
        this.windowBytes = 0;
        this.totalLatency = 0;
        this.reportingInterval = reportingInterval;
        this.totalMessagesSent = totalMessagesSent;
    }

    public void record(int iter, int latency, int bytes, long time) {
        this.count++;
        this.bytes += bytes;
        this.totalLatency += latency;
        this.maxLatency = Math.max(this.maxLatency, latency);
        this.windowCount++;
        this.windowBytes += bytes;
        this.windowTotalLatency += latency;
        this.windowMaxLatency = Math.max(windowMaxLatency, latency);
        if (iter % this.sampling == 0) {
            this.latencies[index] = latency;
            this.index++;
        }
        /* maybe report the recent perf */
        if (time - windowStart >= reportingInterval) {
            printWindow();
            newWindow();
        }
    }

    public Callback nextCompletion(long start, int bytes, Stats stats) {
        Callback cb = new PerfCallback(this.iteration, start, bytes, stats, totalMessagesSent);
        this.iteration++;
        return cb;
    }

    public void printWindow() {
        long ellapsed = System.currentTimeMillis() - windowStart;
        double recsPerSec = 1000.0 * windowCount / (double) ellapsed;
        double mbPerSec = 1000.0 * this.windowBytes / (double) ellapsed / (1024.0 * 1024.0);
        System.out.printf(
            "%s %d records sent,use time%d(s) %.1f records/sec (%.2f MB/sec), %.1f ms avg latency, %.1f max latency.\n",
            Thread.currentThread().getName(), windowCount, ellapsed / 1000, recsPerSec, mbPerSec,
            windowTotalLatency / (double) windowCount, (double) windowMaxLatency);
    }

    public void newWindow() {
        this.windowStart = System.currentTimeMillis();
        this.windowCount = 0;
        this.windowMaxLatency = 0;
        this.windowTotalLatency = 0;
        this.windowBytes = 0;
    }

    public void printTotal() {
        long elapsed = System.currentTimeMillis() - start;
        double recsPerSec = 1000.0 * count / (double) elapsed;
        double mbPerSec = 1000.0 * this.bytes / (double) elapsed / (1024.0 * 1024.0);
        int[] percs = percentiles(this.latencies, index, 0.5, 0.95, 0.99, 0.999);
        System.out.printf(
            "%s %d records sent, %f records/sec (%.2f MB/sec), %.2f ms avg latency, %.2f ms max latency, %d ms 50th, %d ms 95th, %d ms 99th, %d ms 99.9th.\n",
            Thread.currentThread().getName(), count, recsPerSec, mbPerSec, totalLatency / (double) count,
            (double) maxLatency, percs[0], percs[1], percs[2], percs[3]);
    }

    private static int[] percentiles(int[] latencies, int count, double... percentiles) {
        int size = Math.min(count, latencies.length);
        Arrays.sort(latencies, 0, size);
        int[] values = new int[percentiles.length];
        for (int i = 0; i < percentiles.length; i++) {
            int index = (int) (percentiles[i] * size);
            values[i] = latencies[index];
        }
        return values;
    }

    private static final class PerfCallback implements Callback {
        private final long start;
        private final int iteration;
        private final int bytes;
        private final Stats stats;
        private AtomicLong totalMessagesSent;

        public PerfCallback(int iter, long start, int bytes, Stats stats, AtomicLong totalMessagesSent) {
            this.start = start;
            this.stats = stats;
            this.iteration = iter;
            this.bytes = bytes;
            this.totalMessagesSent = totalMessagesSent;
        }

        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (metadata != null) {
                long now = System.currentTimeMillis();
                int latency = (int) (now - start);
                this.stats.record(iteration, latency, bytes, now);
                this.totalMessagesSent.addAndGet(1);
            } else {
                System.out.println("Exception occurred during message send");
            }
        }
    }
}
