package jmri.jmrit.trainrecord;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.Sensor;
import jmri.TrainRecord;
import jmri.TrainRecord.Confidence;
import jmri.TrainRecord.Reason;

/**
 * Puts train records' reasons and states into words, for the Block dialog,
 * the Block Table and panels. Reasons are kept as references to Blocks,
 * sensors and times, and are only put into words here, when someone looks.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class TrainRecordReasons {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private TrainRecordReasons() {
    }

    /**
     * @param reason a reason
     * @return it in words, one sentence
     */
    @Nonnull
    public static String describe(@Nonnull Reason reason) {
        Block block = reason.getBlock();
        Block from = reason.getFrom();
        Sensor sensor = reason.getSensor();
        List<String> through = new ArrayList<>();
        for (Block b : reason.getThrough()) {
            through.add(b.getDisplayName());
        }
        return Bundle.getMessage("Reason" + reason.getRule().name(),
                block == null ? Bundle.getMessage("ReasonNoBlock") : block.getDisplayName(),
                from == null ? Bundle.getMessage("ReasonNoBlock") : from.getDisplayName(),
                time(reason.getTime()),
                sensor == null ? Bundle.getMessage("ReasonNoSensor") : sensor.getDisplayName(),
                String.join(", ", through));
    }

    /**
     * @param time a time
     * @return it as the time of day, to the second, in this computer's time
     *         zone
     */
    @Nonnull
    public static String time(@Nonnull Instant time) {
        return LocalTime.ofInstant(time, ZoneId.systemDefault()).format(TIME);
    }

    /**
     * @param confidence a confidence, or null
     * @return it in words, or an empty string for null
     */
    @Nonnull
    public static String describe(@CheckForNull Confidence confidence) {
        return confidence == null ? "" : Bundle.getMessage("Confidence" + confidence.name());
    }

    /**
     * A train's name as panels and the Block Table show it: with a marking
     * only when the operator needs to check it (ambiguous, stalled or lost).
     *
     * @param record a train record
     * @return its name, marked if needed
     */
    @Nonnull
    public static String displayName(@Nonnull TrainRecord record) {
        Confidence c = record.getConfidence();
        if (c != null && (c == Confidence.AMBIGUOUS || c == Confidence.STALLED || c == Confidence.LOST)) {
            return Bundle.getMessage("TrainName" + c.name(), record.getName());
        }
        return record.getName();
    }

    /**
     * Why a train is in a Block, in words: the reason it was put there, and
     * then, if JMRI is less than sure of it, why.
     *
     * @param record a train record
     * @param block  one of its Blocks
     * @return the explanation, or an empty string if the train isn't there
     */
    @Nonnull
    public static String why(@Nonnull TrainRecord record, @Nonnull Block block) {
        Reason in = record.getReason(block);
        if (in == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(describe(in));
        Reason c = record.getConfidenceReason();
        if (c != null && c != in && record.getConfidence() != Confidence.CONFIRMED) {
            sb.append(' ').append(describe(c));
        }
        return sb.toString();
    }
}
