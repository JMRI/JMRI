package jmri.server.json.audio;

import static jmri.server.json.audio.JsonAudio.AUDIO;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.audio.DefaultAudioManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link JsonAudioSocketService}.
 */
public class JsonAudioSocketServiceTest {

    private static final String FACTORY_PROPERTY = "jmri.jmrit.audio.DefaultAudioManager.implementation";
    private final Locale locale = Locale.ENGLISH;
    private String oldFactory;
    private AudioManager manager;

    @Test
    public void testAudioChange() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonAudioSocketService service = new JsonAudioSocketService(connection);
        AudioSource source = (AudioSource) manager.newAudio("IAS1", null);
        JsonNode message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "IAS1");
        service.onMessage(AUDIO, message, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        // a source that hasn't played yet
        message = connection.getMessage();
        assertNotNull(message, "Message is not null");
        assertEquals(JSON.AUDIO_INITIAL, message.path(JSON.DATA).path(JSON.STATE).asInt(-1));
        // playing is pushed to the subscriber
        source.play();
        JUnitUtil.waitFor(() -> JSON.AUDIO_PLAYING == connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt(-1),
                "playing pushed");
        // and so is stopping
        source.stop();
        JUnitUtil.waitFor(() -> JSON.AUDIO_STOPPED == connection.getMessage().path(JSON.DATA).path(JSON.STATE).asInt(-1),
                "stopped pushed");
        service.onClose();
    }

    @Test
    public void testOnList() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonAudioSocketService service = new JsonAudioSocketService(connection);
        manager.newAudio("IAS1", null);
        service.onList(AUDIO, connection.getObjectMapper().createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        JsonNode message = connection.getMessage();
        assertNotNull(message, "Message is not null");
        assertTrue(message.isArray(), "List is an array");
        assertEquals(1, message.size());
        // adding a buffer pushes the list again, still only sources and not an error
        int count = connection.getMessages().size();
        manager.newAudio("IAB1", null);
        JUnitUtil.waitFor(() -> connection.getMessages().size() > count, "list pushed");
        message = connection.getMessage();
        assertTrue(message.isArray(), "List is an array, not an error");
        assertEquals(1, message.size());
        assertEquals("IAS1", message.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        // adding a source pushes a longer list
        manager.newAudio("IAS2", null);
        JUnitUtil.waitFor(() -> connection.getMessage().size() == 2, "list with two sources pushed");
        service.onClose();
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        // use the Null sound system so tests behave the same with or without a sound card
        oldFactory = System.setProperty(FACTORY_PROPERTY, "jmri.jmrit.audio.NullAudioFactory");
        manager = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.setDefault(AudioManager.class, manager);
        manager.init();
    }

    @AfterEach
    public void tearDown() throws Exception {
        manager.cleanup();
        manager = null;
        if (oldFactory == null) {
            System.clearProperty(FACTORY_PROPERTY);
        } else {
            System.setProperty(FACTORY_PROPERTY, oldFactory);
        }
        oldFactory = null;
        JUnitUtil.tearDown();
    }

}
