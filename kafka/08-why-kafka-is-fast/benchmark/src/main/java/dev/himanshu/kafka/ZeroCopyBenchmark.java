package dev.himanshu.kafka;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Traditional read/write I/O vs FileChannel.transferTo (zero-copy),
 * copying a 10 MB file. This is the mechanism Kafka uses to move bytes
 * between a socket channel and a log segment without touching the heap.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@Threads(5)
@State(Scope.Thread)
public class ZeroCopyBenchmark {

    private static final int FILE_SIZE_BYTES = 10 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8 * 1024;

    private Path inputFile;
    private Path outputFile;

    @Setup(Level.Trial)
    public void createInputFile() throws IOException {
        inputFile = Files.createTempFile("zero-copy-in-", ".bin");
        outputFile = Files.createTempFile("zero-copy-out-", ".bin");

        byte[] payload = new byte[FILE_SIZE_BYTES];
        new Random(42).nextBytes(payload);
        Files.write(inputFile, payload);
    }

    @TearDown(Level.Trial)
    public void cleanUp() throws IOException {
        Files.deleteIfExists(inputFile);
        Files.deleteIfExists(outputFile);
    }

    @Benchmark
    public long traditionalIo() throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(inputFile.toFile(), "r");
             RandomAccessFile out = new RandomAccessFile(outputFile.toFile(), "rw");
             FileChannel source = in.getChannel();
             FileChannel destination = out.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            long copied = 0;
            while (source.read(buffer) > 0) {
                buffer.flip();
                copied += destination.write(buffer);
                buffer.clear();
            }
            return copied;
        }
    }

    @Benchmark
    public long zeroCopyIo() throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(inputFile.toFile(), "r");
             RandomAccessFile out = new RandomAccessFile(outputFile.toFile(), "rw");
             FileChannel source = in.getChannel();
             FileChannel destination = out.getChannel()) {

            long size = source.size();
            long copied = 0;
            // The kernel moves the bytes - they never enter the JVM heap.
            while (copied < size) {
                copied += source.transferTo(copied, size - copied, destination);
            }
            return copied;
        }
    }
}
