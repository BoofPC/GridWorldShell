package info.gridworld.actor;

import java.util.EventListener;
import java.util.Set;
import java.util.stream.Stream;

import info.gridworld.actor.ActorEvent.ActorInfo;

public interface ActorListener extends EventListener {
  Stream<Action> eventResponse(ActorEvent e, ActorInfo self,
    Set<ActorInfo> environment);
}
