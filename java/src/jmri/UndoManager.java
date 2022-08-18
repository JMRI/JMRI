package jmri;

import java.util.*;

import jmri.jmrit.display.layoutEditor.PositionablePointView;

public class UndoManager implements InstanceManagerAutoDefault {

    Queue<UndoObject> undoQueue = Collections.asLifoQueue(new ArrayDeque<>());

    public UndoManager() {
    }

    public void addUndoEvent(Object source, String key, Object data) {
        undoQueue.add(new UndoObject(source, key, data));
        log.info("++++ add {} :: {} :: {}", source, key, data);
    }

    public void undoEvent() {
        if (undoQueue.size() == 0) {
            log.info("Undo Queue is empty");
            return;
        }

        UndoObject undo = undoQueue.remove();
        log.info("---- undo = {} :: {} :: {}", undo.getSource(), undo.getKey(), undo.getData());
        if (undo.getSource() instanceof PositionablePointView) {
            ((PositionablePointView)undo.getSource()).undoChange(undo.getKey(), undo.getData());
        }
    }

    private static class UndoObject {
        Object _source;    // The 'this' value from the caller
        String _key;
        Object _data;

        public UndoObject(Object source, String key, Object data) {
            _source = source;
            _key = key;
            _data = data;
        }

        Object getSource() {
            return _source;
        }

        String getKey() {
            return _key;
        }

        Object getData() {
            return _data;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UndoManager.class);
}

