package io.kimconsulting.maxbots.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class BotInstanceLock implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    private BotInstanceLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    public static BotInstanceLock acquire(Path path) {
        try {
            FileChannel channel = FileChannel.open(
                path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
            FileLock lock = channel.tryLock();
            if (lock == null) {
                channel.close();
                throw new IllegalStateException("Another echo bot instance is already running. Stop it before starting a new one.");
            }

            String content = "pid=" + ProcessHandle.current().pid() + System.lineSeparator()
                + "started_at=" + Instant.now() + System.lineSeparator();
            channel.write(ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)));
            channel.force(true);

            return new BotInstanceLock(channel, lock);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to acquire bot instance lock: " + path, ex);
        }
    }

    @Override
    public void close() {
        try {
            if (lock != null && lock.isValid()) {
                lock.release();
            }
        } catch (IOException ignored) {
        }

        try {
            channel.close();
        } catch (IOException ignored) {
        }
    }
}
