package jmri.jmrix.marklin;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Ms2LocoInfoSessionTest {
    @Test
    void overallDeadlineAndCancellationNeverLeaveQueuedFramesActive() {
        List<String> errors = new ArrayList<>();
        Ms2LocoInfoSession session = new Ms2LocoInfoSession(0xFF37, 0x4712, 50,
                new Ms2LocoInfoSession.Listener() {
                    @Override public void error(String reason) { errors.add(reason); }
                });
        assertNotNull(session.nextFrame(0));
        assertNull(session.nextFrame(120_000));
        assertTrue(session.isFinished());
        assertEquals(1, session.getFailures());
        assertEquals(List.of("Roster acquisition exceeded two minutes"), errors);
        assertNull(session.nextFrame(121_000));
        session.cancel("Already finished");
        assertEquals(1, errors.size());
    }
}
