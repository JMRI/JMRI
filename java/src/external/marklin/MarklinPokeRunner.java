package external.marklin;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TCP client for marklin-poke: connects to McanTcpBridge, sends CAN frames,
 * collects replies and prints everything to stdout.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class MarklinPokeRunner {

    private static final int FRAME_SIZE = 13;
    private static final int CONNECT_TIMEOUT_MS = 5000;

    private final MarklinPoke.Args args;
    private final PrintStream out;
    private final PrintStream err;
    Socket socket;

    public MarklinPokeRunner(MarklinPoke.Args args) {
        this(args, System.out, System.err);
    }

    public MarklinPokeRunner(MarklinPoke.Args args, PrintStream out, PrintStream err) {
        this.args = args;
        this.out = out;
        this.err = err;
    }

    /**
     * Attempt to connect to the bridge. Returns false (and prints an error) if
     * the connection cannot be established.
     */
    public boolean connect() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(args.host(), args.port()), CONNECT_TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            err.println("Cannot connect to " + args.host() + ":" + args.port() + ": " + e.getMessage());
            try { socket.close(); } catch (IOException ignored) {}
            return false;
        }
    }

    /**
     * Connect, send frames, watch, print results.
     * Calls System.exit(1) if the bridge is not reachable.
     */
    @SuppressFBWarnings(value = "DM_EXIT", justification = "standalone CLI tool aborts when the bridge is unreachable")
    public void run() {
        if (!connect()) {
            System.exit(1);
            return;
        }

        CopyOnWriteArrayList<ReceivedFrame> received = new CopyOnWriteArrayList<>();
        long[] t0 = {-1L};

        // Start receiver thread
        Thread receiver = new Thread(() -> {
            try {
                InputStream in = socket.getInputStream();
                byte[] buf = new byte[FRAME_SIZE];
                int pos = 0;
                int b;
                while (!Thread.currentThread().isInterrupted() && (b = in.read()) != -1) {
                    buf[pos++] = (byte) b;
                    if (pos == FRAME_SIZE) {
                        long now = System.nanoTime();
                        long base = t0[0] < 0 ? now : t0[0];
                        long offsetMs = (now - base) / 1_000_000L;
                        received.add(new ReceivedFrame(offsetMs, buf.clone()));
                        pos = 0;
                    }
                }
            } catch (IOException e) {
            }
        });
        receiver.setDaemon(true);
        receiver.start();

        try {
            OutputStream os = socket.getOutputStream();
            List<byte[]> frames = args.frames();
            for (int i = 0; i < frames.size(); i++) {
                if (t0[0] < 0) {
                    t0[0] = System.nanoTime();
                }
                long offsetMs = (System.nanoTime() - t0[0]) / 1_000_000L;
                byte[] frame = frames.get(i);
                os.write(frame);
                os.flush();
                out.printf("> +%04dms %s%n", offsetMs, toHex(frame));

                if (i < frames.size() - 1 && args.paddingMs() > 0) {
                    Thread.sleep(args.paddingMs());
                }
            }

            if (args.watchSecs() > 0) {
                Thread.sleep(args.watchSecs() * 1000L);
            }
        } catch (IOException | InterruptedException e) {
            err.println("Error during send/watch: " + e);
            if (e instanceof InterruptedException) {
                // Restore the interrupt status so callers up the stack can observe it.
                Thread.currentThread().interrupt();
            }
        }

        receiver.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            err.println("Error closing socket: " + e);
        }
        try { receiver.join(500); } catch (InterruptedException ignored) {}

        for (ReceivedFrame rf : received) {
            out.printf("< +%04dms %s%n", rf.offsetMs, toHex(rf.data));
        }
    }

    /** Format bytes as space-separated uppercase two-digit hex. */
    static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(String.format("%02X", data[i] & 0xFF));
        }
        return sb.toString();
    }

    /** A received CAN frame with its arrival offset. */
    static final class ReceivedFrame {
        final long offsetMs;
        final byte[] data;

        ReceivedFrame(long offsetMs, byte[] data) {
            this.offsetMs = offsetMs;
            this.data = data;
        }
    }
}
