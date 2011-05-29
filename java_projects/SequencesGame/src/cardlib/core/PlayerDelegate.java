package cardlib.core;

import java.io.Serializable;

/**
 * Encapsulates funcionality which every player
 * that can be represented in the logic of the game,
 * must implement.
 */
public interface PlayerDelegate extends Serializable, Comparable<PlayerDelegate>
{
    /**
     * Contains the logic of a play associated with
     * entity that is playable.
     *
     * @return the play data
     */
    public Play doPlay();
}
