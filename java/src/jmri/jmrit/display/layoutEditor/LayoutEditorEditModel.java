package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.jmrit.display.EditState;
import jmri.jmrit.display.Editor;

/**
 * A Layout Editor panel as its {@link jmri.jmrit.display.EditHistory} sees
 * it: the tracks (keyed by their views), the shapes and the icons.
 * <p>
 * Putting a step back makes the panel hold the same track objects as before,
 * writes back their properties, and then sets the use counts of the Blocks
 * involved from which tracks use them: each track taken off the panel or
 * changed gives up its Blocks, and each track on the panel afterwards takes
 * its Blocks again. Connectivity is then recomputed as after any edit.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
final class LayoutEditorEditModel extends Editor.PanelEditModel {

    private final LayoutEditor layoutEditor;

    LayoutEditorEditModel(@Nonnull LayoutEditor layoutEditor) {
        super(layoutEditor);
        this.layoutEditor = layoutEditor;
    }

    @Override
    public List<EditState<?>> capture() {
        List<EditState<?>> list = new ArrayList<>();
        for (LayoutTrackView v : layoutEditor.getLayoutTrackViews()) {
            list.add(LayoutEditorEditStates.of(v));
        }
        for (LayoutShape s : layoutEditor.getLayoutShapes()) {
            list.add(LayoutEditorEditStates.of(s));
        }
        list.addAll(super.capture());
        return list;
    }

    @Override
    public void restore(List<Object> items, List<EditState<?>> states) {
        List<LayoutTrackView> views = new ArrayList<>();
        List<LayoutShape> shapes = new ArrayList<>();
        for (Object o : items) {
            if (o instanceof LayoutTrackView) {
                views.add((LayoutTrackView) o);
            } else if (o instanceof LayoutShape) {
                shapes.add((LayoutShape) o);
            }
        }
        Set<LayoutTrackView> before = identitySet(layoutEditor.getLayoutTrackViews());
        Set<LayoutTrackView> after = identitySet(views);

        // the tracks whose Blocks may change: those written back, added or removed
        Set<LayoutTrackView> changed = Collections.newSetFromMap(new IdentityHashMap<>());
        for (EditState<?> s : states) {
            if (s.getItem() instanceof LayoutTrackView) {
                changed.add((LayoutTrackView) s.getItem());
            }
        }
        for (LayoutTrackView v : before) {
            if (!after.contains(v)) {
                changed.add(v);
            }
        }
        for (LayoutTrackView v : after) {
            if (!before.contains(v)) {
                changed.add(v);
            }
        }

        // Block use counts: start from today's, less the uses of the changed tracks
        Map<LayoutBlock, Integer> counts = new IdentityHashMap<>();
        for (LayoutBlock b : InstanceManager.getDefault(LayoutBlockManager.class).getNamedBeanSet()) {
            counts.put(b, b.getUseCount());
        }
        Set<LayoutBlock> affected = Collections.newSetFromMap(new IdentityHashMap<>());
        for (LayoutTrackView v : changed) {
            if (before.contains(v)) {
                for (LayoutBlock b : blocksOf(v)) {
                    counts.merge(b, -1, Integer::sum);
                    affected.add(b);
                }
            }
        }

        // the same objects back on the panel
        for (LayoutTrackView v : before) {
            if (!after.contains(v) && v instanceof LayoutTurnoutView) {
                ((LayoutTurnoutView) v).getLayoutTurnout().setOnPanel(false);
            }
        }
        layoutEditor.setLayoutTracksForUndo(views);
        for (LayoutTrackView v : after) {
            if (!before.contains(v) && v instanceof LayoutTurnoutView) {
                ((LayoutTurnoutView) v).getLayoutTurnout().setOnPanel(true);
            }
        }
        layoutEditor.setLayoutShapesForUndo(shapes);
        restoreIcons(items);

        for (EditState<?> s : states) {
            s.restore();
        }

        // and the uses of the changed tracks now on the panel
        for (LayoutTrackView v : changed) {
            if (after.contains(v)) {
                for (LayoutBlock b : blocksOf(v)) {
                    counts.merge(b, 1, Integer::sum);
                    affected.add(b);
                }
            }
        }
        for (Map.Entry<LayoutBlock, Integer> e : counts.entrySet()) {
            setUseCount(e.getKey(), e.getValue());
        }

        layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
        for (LayoutBlock b : affected) {
            b.updatePaths();
        }
        layoutEditor.clearSelectionGroups();
        layoutEditor.resizePanelBounds(true);
        layoutEditor.redrawPanel();
    }

    @Override
    public void setDirty() {
        layoutEditor.setDirty();
    }

    /**
     * The Blocks a track uses, each once.
     *
     * @param v the track's view
     * @return its Blocks
     */
    @Nonnull
    static Set<LayoutBlock> blocksOf(@Nonnull LayoutTrackView v) {
        Set<LayoutBlock> set = Collections.newSetFromMap(new IdentityHashMap<>());
        if (v instanceof TrackSegmentView) {
            set.add(((TrackSegmentView) v).getTrackSegment().getLayoutBlock());
        } else if (v instanceof LayoutTurnoutView) {
            LayoutTurnout t = ((LayoutTurnoutView) v).getLayoutTurnout();
            set.add(t.getLayoutBlock());
            set.add(t.getLayoutBlockB());
            set.add(t.getLayoutBlockC());
            set.add(t.getLayoutBlockD());
        } else if (v instanceof LevelXingView) {
            LevelXing x = ((LevelXingView) v).getLevelXing();
            set.add(x.getLayoutBlockAC());
            set.add(x.getLayoutBlockBD());
        } else if (v instanceof LayoutTurntableView) {
            set.add(((LayoutTurntableView) v).getTurntable().getLayoutBlock());
        } else if (v instanceof LayoutTraverserView) {
            set.add(((LayoutTraverserView) v).getTraverser().getLayoutBlock());
        }
        set.remove(null);
        return set;
    }

    private static void setUseCount(LayoutBlock b, int count) {
        int target = Math.max(0, count);
        while (b.getUseCount() < target) {
            b.incrementUse();
        }
        while (b.getUseCount() > target) {
            b.decrementUse();
        }
    }

    private static <T> Set<T> identitySet(List<T> list) {
        Set<T> set = Collections.newSetFromMap(new IdentityHashMap<>());
        set.addAll(list);
        return set;
    }
}
