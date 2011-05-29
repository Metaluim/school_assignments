package cardlib.core;

/**
 * A mediator is the entity which evaluates plays.
 * For example, if a play is invalid, this is the entity
 * which makes that decision.
 */
public interface Mediator
{
    /**
     * Evaluates a given play, returning a status
     * inferred from the evaluation.
     *
     * @param play a given play
     * @return a status for the evaluation
     */
    public Play.PlayStatus evalPlay(Play play);
}
