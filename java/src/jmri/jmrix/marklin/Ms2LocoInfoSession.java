package jmri.jmrix.marklin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import jmri.jmrix.marklin.mcan.McanFrame;
import jmri.jmrix.marklin.mcan.cs2.Cs2Data;
import jmri.jmrix.marklin.mcan.cs2.Cs2Frame;
import jmri.jmrix.marklin.mcan.cs2.Cs2Header;
import jmri.jmrix.marklin.mcan.cs2.Cs2GuiCommands;

/**
 * Sequential MS2 list/detail reader. Call from one thread.
 * The provider is selected externally.
 *
 * A sessioon must be targeted at a specific MS2 provider.
 *
 * The typical flow is to first get the index of available entries (lokliste) and then pull the individual loco entries (lokinfo)
 */
public final class Ms2LocoInfoSession {
    public static final long SESSION_MS = 120_000;
    public static final long REPLY_MS = 5_000;
    private static final int MAX_BYTES = 1_048_576;

    public interface Listener {
        default void requesting(String description, int attempt) {}
        default void listReceived(String text) {}
        default void locoReceived(String name, String text) {}
        default void error(String reason) {}
        default void finished(int records, int failures) {}
    }

    private final int providerHash;
    private final int requesterHash;
    private final long paddingMs;
    private final Listener listener;
    private final Deque<int[]> outgoing = new ArrayDeque<>();
    private final Deque<String> names = new ArrayDeque<>();
    private String request = "lokliste";
    private boolean listRequest = true;
    private int attempts;
    private boolean finished;
    private int records;
    private int failures;
    private long readyAt;
    private long nextSendAt;
    private long replyDeadline;
    private byte[] stream;
    private int streamHash;
    private int streamIndex;
    private int streamCrc;

    public Ms2LocoInfoSession(int providerHash, int requesterHash, long paddingMs, Listener listener) {
        this.providerHash = providerHash;
        this.requesterHash = requesterHash;
        this.paddingMs = Math.max(1, paddingMs);
        this.listener = java.util.Objects.requireNonNull(listener);
    }

    public boolean isFinished() { return finished; }
    public int getRecords() { return records; }
    public int getFailures() { return failures; }

    /** Elapsed milliseconds since this acquisition began. */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
            justification = "null means no frame is due yet, distinct from an empty frame")
    @javax.annotation.CheckForNull
    public int[] nextFrame(long now) {
        if (finished) {
            return null;
        }
        if (now >= SESSION_MS) {
            cancel("Roster acquisition exceeded two minutes");
            return null;
        }
        if (replyDeadline != 0 && now >= replyDeadline) {
            failed("Timed out waiting for " + description(), now);
        }
        if (finished || now < readyAt) {
            return null;
        }
        if (outgoing.isEmpty() && replyDeadline == 0) {
            attempts++;
            if (listRequest) {
                outgoing.add(Cs2GuiCommands.buildRequestConfigData("lokliste", requesterHash));
            } else {
                outgoing.addAll(Cs2GuiCommands.buildRequestLocoInfo(request, requesterHash));
            }
            listener.requesting(description(), attempts);
        }
        if (outgoing.isEmpty() || now < nextSendAt) {
            return null;
        }
        int[] frame = outgoing.removeFirst();
        nextSendAt = now + paddingMs;
        if (outgoing.isEmpty()) {
            replyDeadline = now + REPLY_MS;
        }
        return frame;
    }

    public void accept(McanFrame frame, long now) {
        if (finished || now >= SESSION_MS || !(frame instanceof Cs2Frame) || frame.raw().byteAt(4) > 8) {
            return;
        }
        Cs2Header header = ((Cs2Frame) frame).header();
        if (frame instanceof Cs2GuiCommands.RequestConfigDataRequestFrame && header.hash() != requesterHash) {
            if (attempts > 0) {
                failed("Competing configuration request; cannot safely correlate " + description(), now);
            } else {
                readyAt = now + REPLY_MS;
            }
            return;
        }
        if (replyDeadline == 0 || header.commandCode() != Cs2GuiCommands.COMMAND_CONFIG_DATA_STREAM
                || (header.hash() != requesterHash && header.hash() != providerHash)) {
            return;
        }
        if (frame instanceof Cs2GuiCommands.ConfigDataStreamHeaderFrame) {
            Cs2GuiCommands.ConfigDataStreamHeaderFrame streamHeader = (Cs2GuiCommands.ConfigDataStreamHeaderFrame) frame;
            long length = streamHeader.fileLength();
            if (length == 0 || length > MAX_BYTES || stream != null) {
                failed("Invalid or overlapping stream header for " + description(), now);
                return;
            }
            stream = new byte[(int) length];
            streamHash = header.hash();
            streamIndex = 0;
            streamCrc = streamHeader.crc();
            replyDeadline = now + REPLY_MS;
        } else if (stream != null && header.hash() == streamHash) {
            if (!(frame instanceof Cs2GuiCommands.ConfigDataStreamPacketFrame)) {
                failed("Invalid stream data DLC for " + description(), now);
                return;
            }
            Cs2Data data = ((Cs2Frame) frame).data();
            int count = Math.min(8, stream.length - streamIndex);
            for (int i = 0; i < count; i++) {
                stream[streamIndex++] = (byte) data.byteAt(i);
            }
            replyDeadline = now + REPLY_MS;
            if (streamIndex == stream.length) {
                complete(now);
            }
        }
    }

    public void cancel(String reason) {
        if (!finished) {
            listener.error(reason);
            failures++;
            finish();
        }
    }

    private String description() {
        return listRequest ? "lokliste" : "lokinfo " + request;
    }

    private void failed(String reason, long now) {
        listener.error(reason);
        outgoing.clear();
        stream = null;
        replyDeadline = 0;
        readyAt = now + 1_000;
        if (attempts >= 2) {
            failures++;
            advance(now);
        }
    }

    private void advance(long now) {
        request = names.pollFirst();
        listRequest = false;
        attempts = 0;
        stream = null;
        replyDeadline = 0;
        readyAt = now + 250;
        if (request == null) {
            finish();
        }
    }

    private void finish() {
        finished = true;
        outgoing.clear();
        stream = null;
        listener.finished(records, failures);
    }

    private void complete(long now) {
        if (crc16(stream) != streamCrc) {
            failed("CRC mismatch for " + description(), now);
            return;
        }
        String text;
        try {
            byte[] payload = stream;
            // Captured MS2 DLC-7 lists can be plain text, despite the reference's zlib suggestion.
            if ((payload[0] & 0xFF) == 0x78) {
                payload = inflate(payload);
            }
            text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(payload))
                    .toString().replace("\0", "").strip();
        } catch (IOException e) {
            failed("Cannot decode " + description() + ": " + e.getMessage(), now);
            return;
        }
        Set<String> found = new LinkedHashSet<>();
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.strip();
            if (trimmed.startsWith(".name=")) {
                found.add(trimmed.substring(6));
            }
        }
        if (listRequest) {
            if (!text.startsWith("[lokliste]")) {
                failed("Unexpected document instead of lokliste", now);
                return;
            }
            for (String name : found) {
                try {
                    Cs2GuiCommands.buildRequestLocoInfo(name, requesterHash);
                    names.addLast(name);
                } catch (IllegalArgumentException e) {
                    failures++;
                    listener.error("Cannot request lokinfo: " + e.getMessage());
                }
            }
            listener.listReceived(text);
        } else if (!text.startsWith("[lokomotive]") || found.size() != 1 || !found.contains(request)) {
            failed("Received record does not match lokinfo name " + request, now);
            return;
        } else {
            records++;
            listener.locoReceived(request, text);
        }
        advance(now);
    }

    public static int crc16(byte[] data) {
        int crc = 0xFFFF;
        for (byte value : data) {
            crc ^= (value & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                crc = ((crc << 1) ^ ((crc & 0x8000) == 0 ? 0 : 0x1021)) & 0xFFFF;
            }
        }
        return crc;
    }

    private static byte[] inflate(byte[] data) throws IOException {
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(data);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0 || result.size() + count > MAX_BYTES) {
                    throw new IOException("Incomplete, oversized or dictionary-dependent zlib stream");
                }
                result.write(buffer, 0, count);
            }
            return result.toByteArray();
        } catch (DataFormatException e) {
            throw new IOException("Invalid zlib stream", e);
        } finally {
            inflater.end();
        }
    }
}
