package info.gridworld.actor;

import java.util.EventListener;
import java.util.Set;
import java.util.stream.Stream;

import info.gridworld.actor.ActorEvent.ActorInfo;

/**
 * The {@code ActorListener} interface represents the brain of an {@code Actor} that can respond to
 * a changing world.
 */
public interface ActorListener extends EventListener {
  /**
   * Responds to an event in a {@link ShellWorld}. Returning multiple final {@link Action}s is
   * undefined.
   * 
   * @param e the event in question
   * @param self information about the current actor
   * @param environment information about the detectable actors
   * @return the response to the event
   */
  Stream<Action> eventResponse(ActorEvent e, ActorInfo self, Set<ActorInfo> environment);
}
