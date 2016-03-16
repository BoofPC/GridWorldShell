package info.gridworld.actor;

/**
 * An {@code Action} is a data structure that represents an action taken by a {@link Shell} in a
 * {@link ShellWorld}.
 */
public interface Action {
  /**
   * A final action is not meant to be followed by another in the same step.
   * 
   * @return the action's finality
   */
  boolean isFinal();
}
