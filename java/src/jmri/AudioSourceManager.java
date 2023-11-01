package jmri;

/**
 * An audio manager that only returns audio sources.
 * This manager uses the ordinary AudioManager, but only returns sources.
 *
 * @author Daniel Bergvist (C) 2023
 */
public interface AudioSourceManager extends AudioManager {
}
