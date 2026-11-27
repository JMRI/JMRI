package jmri.server.json.audio;

import static jmri.server.json.audio.JsonAudio.AUDIO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.InstanceManager;
import jmri.jmrit.audio.AudioSource;
import jmri.jmrit.audio.DefaultAudioManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link JsonAudioHttpService}.
 */
public class JsonAudioHttpServiceTest extends JsonNamedBeanHttpServiceTestBase<Audio, JsonAudioHttpService> {

    private static final String FACTORY_PROPERTY = "jmri.jmrit.audio.DefaultAudioManager.implementation";
    private String oldFactory;
    private AudioManager manager;

    @Test
    @Override
    public void testDoGet() throws Exception {
        super.testDoGet();
        AudioSource source = (AudioSource) manager.newAudio("IAS1", "test source");
        // a source that hasn't played yet
        assertEquals(Audio.STATE_INITIAL, source.getState());
        JsonNode result = service.doGet(AUDIO, "IAS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(AUDIO, result.path(JSON.TYPE).asText());
        assertEquals("IAS1", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals(JSON.AUDIO_INITIAL, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertFalse(result.path(JSON.DATA).has(JSON.AUDIO_COMMAND_PLAY_NUM_LOOPS));
        // playing
        source.play();
        result = service.doGet(AUDIO, "IAS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.AUDIO_PLAYING, result.path(JSON.DATA).path(JSON.STATE).asInt());
        assertEquals(0, result.path(JSON.DATA).path(JSON.AUDIO_COMMAND_PLAY_NUM_LOOPS).asInt());
        // stopped
        source.stop();
        result = service.doGet(AUDIO, "IAS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.AUDIO_STOPPED, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // a buffer can't be read
        manager.newAudio("IAB1", null);
        JsonException ex = assertThrows(JsonException.class, () ->
            service.doGet(AUDIO, "IAB1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(400, ex.getCode());
        assertEquals("Audio is not sub type \"Source\": IAB1", ex.getMessage());
        // a source that doesn't exist
        ex = assertThrows(JsonException.class, () ->
            service.doGet(AUDIO, "IAS2", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(404, ex.getCode());
    }

    @Test
    public void testDoPost() throws AudioException, JsonException {
        Audio source = manager.newAudio("IAS1", "test source");
        JsonNode result;
        JsonNode message;
        // play
        message = mapper.createObjectNode().put(JSON.NAME, "IAS1").put(JSON.AUDIO_COMMAND, JSON.AUDIO_COMMAND_PLAY);
        validateData(AUDIO, message, false);
        result = service.doPost(AUDIO, "IAS1", message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        validate(result);
        assertEquals(Audio.STATE_PLAYING, source.getState());
        assertEquals(JSON.AUDIO_PLAYING, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // stop
        message = mapper.createObjectNode().put(JSON.NAME, "IAS1").put(JSON.AUDIO_COMMAND, JSON.AUDIO_COMMAND_STOP);
        validateData(AUDIO, message, false);
        result = service.doPost(AUDIO, "IAS1", message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        validate(result);
        assertEquals(Audio.STATE_STOPPED, source.getState());
        assertEquals(JSON.AUDIO_STOPPED, result.path(JSON.DATA).path(JSON.STATE).asInt());
        // rename without a command
        message = mapper.createObjectNode().put(JSON.NAME, "IAS1").put(JSON.USERNAME, "new name");
        validateData(AUDIO, message, false);
        result = service.doPost(AUDIO, "IAS1", message, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        validate(result);
        assertEquals("new name", source.getUserName());
        assertEquals(Audio.STATE_STOPPED, source.getState());
        // an unknown command changes nothing
        JsonNode badCommand = mapper.createObjectNode().put(JSON.NAME, "IAS1")
                .put(JSON.USERNAME, "bad name").put(JSON.COMMENT, "bad comment").put(JSON.AUDIO_COMMAND, "Pause");
        JsonException ex = assertThrows(JsonException.class, () ->
            service.doPost(AUDIO, "IAS1", badCommand, new JsonRequest(locale, JSON.V5, JSON.POST, 42)));
        assertEquals(400, ex.getCode());
        assertEquals("Unknown command for audio: Pause", ex.getMessage());
        assertEquals("new name", source.getUserName());
        assertNull(source.getComment());
        // posting to a buffer changes nothing
        Audio buffer = manager.newAudio("IAB1", "test buffer");
        JsonNode toBuffer = mapper.createObjectNode().put(JSON.NAME, "IAB1").put(JSON.USERNAME, "bad name");
        ex = assertThrows(JsonException.class, () ->
            service.doPost(AUDIO, "IAB1", toBuffer, new JsonRequest(locale, JSON.V5, JSON.POST, 42)));
        assertEquals(400, ex.getCode());
        assertEquals("Audio is not sub type \"Source\": IAB1", ex.getMessage());
        assertEquals("test buffer", buffer.getUserName());
        // a source that doesn't exist
        JsonNode missing = mapper.createObjectNode().put(JSON.NAME, "IAS2").put(JSON.AUDIO_COMMAND, JSON.AUDIO_COMMAND_PLAY);
        ex = assertThrows(JsonException.class, () ->
            service.doPost(AUDIO, "IAS2", missing, new JsonRequest(locale, JSON.V5, JSON.POST, 42)));
        assertEquals(404, ex.getCode());
    }

    @Test
    public void testClientSchema() throws AudioException, JsonException {
        Audio source = manager.newAudio("IAS1", null);
        source.setComment("a comment");
        // null clears the comment
        ObjectNode data = mapper.createObjectNode().put(JSON.NAME, "IAS1").putNull(JSON.COMMENT);
        validateData(AUDIO, data, false);
        service.doPost(AUDIO, "IAS1", data, new JsonRequest(locale, JSON.V5, JSON.POST, 42));
        assertNull(source.getComment());
        // state can't be set, and only Play and Stop are commands
        JsonRequest request = new JsonRequest(locale, JSON.V5, JSON.GET, 42);
        JsonSchemaServiceCache schemas = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        JsonNode withState = mapper.createObjectNode().put(JSON.NAME, "IAS1").put(JSON.STATE, Audio.STATE_PLAYING);
        assertThrows(JsonException.class, () -> schemas.validateData(AUDIO, withState, false, request));
        consumeValidationWarnings();
        JsonNode withPause = mapper.createObjectNode().put(JSON.NAME, "IAS1").put(JSON.AUDIO_COMMAND, "Pause");
        assertThrows(JsonException.class, () -> schemas.validateData(AUDIO, withPause, false, request));
        consumeValidationWarnings();
    }

    private void consumeValidationWarnings() {
        assertNotNull(JUnitAppender.checkForMessageStartingWith("Errors validating"));
        while (JUnitAppender.checkForMessageStartingWith("JSON Validation Error") != null) {
            // drain
        }
    }

    @Test
    public void testDoGetList() throws AudioException, JsonException {
        JsonNode result;
        // the default listener is always there, but isn't listed
        assertNotNull(manager.getBySystemName("IAL$"));
        result = service.doGetList(AUDIO, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(0, result.size());
        // buffers aren't listed either
        manager.newAudio("IAB1", null);
        manager.newAudio("IAS1", null);
        manager.newAudio("IAS2", null);
        result = service.doGetList(AUDIO, mapper.createObjectNode(), new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals(2, result.size());
        assertEquals("IAS1", result.path(0).path(JSON.DATA).path(JSON.NAME).asText());
        assertEquals("IAS2", result.path(1).path(JSON.DATA).path(JSON.NAME).asText());
    }

    @Test
    @Override
    public void testDoDelete() throws JsonException {
        assertDoesNotThrow(() -> manager.newAudio("IAS1", null));
        service.doDelete(AUDIO, "IAS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.DELETE, 42));
        assertNull(manager.getBySystemName("IAS1"));
        JsonException ex = assertThrows(JsonException.class, () ->
            service.doDelete(AUDIO, "IAS1", NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.DELETE, 42)));
        assertEquals(404, ex.getCode());
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // use the Null sound system so tests behave the same with or without a sound card
        oldFactory = System.setProperty(FACTORY_PROPERTY, "jmri.jmrit.audio.NullAudioFactory");
        manager = new DefaultAudioManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.setDefault(AudioManager.class, manager);
        manager.init();
        service = new JsonAudioHttpService(mapper);
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        manager.cleanup();
        manager = null;
        if (oldFactory == null) {
            System.clearProperty(FACTORY_PROPERTY);
        } else {
            System.setProperty(FACTORY_PROPERTY, oldFactory);
        }
        oldFactory = null;
        super.tearDown();
    }

}
