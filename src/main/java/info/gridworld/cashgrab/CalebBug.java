package info.gridworld.cashgrab;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.StepEvent;
import info.gridworld.actor.ActorListener;
import info.gridworld.cashgrab.Actions.CollectCoinAction;

public class CalebBug implements ActorListener {
  @Override
  public Stream<Action> eventResponse(final ActorEvent e, final ActorInfo self,
      final Set<ActorInfo> environment) {
    // As we need to stream our actions at the end, a List works to store them for now.
    final List<Action> actions = new ArrayList<>();

    // CalebBug doesn't care about anything but looking around and acting.
    // The stepEvent label serves a similar purpose to an early return,
    //   but for control statements & blocks. (JLS 14.5)
    stepEvent: if (e instanceof StepEvent) {
      // Keep track of the first coin we find, or lack thereof.
      final ActorInfo coin = environment.stream()
          .filter(a -> a.getType().equals(Coin.class.getName())).findFirst().orElse(null);
      if (coin != null) {
        final Double distance = coin.getDistance();
        final Double direction = coin.getDirection();
        if (direction != null && distance != null && distance <= 2) {
          actions.add(new CollectCoinAction(distance, direction));
          // CollectCoinAction is a final event, so we're done here.
          break stepEvent;
        }
      }

      // Wandering around in search of a coin.
      if (Math.random() < 0.5) {
        actions.add(new TurnAction(-1));
      } else {
        actions.add(new TurnAction(1));
      }
      actions.add(new MoveAction(1));
    }
    return actions.stream();
  }
}
