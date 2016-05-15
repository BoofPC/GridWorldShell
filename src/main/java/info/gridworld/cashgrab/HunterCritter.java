package info.gridworld.cashgrab;

import java.awt.Color;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.StepEvent;
import info.gridworld.actor.ActorListener;
import info.gridworld.actor.Util;
import info.gridworld.actor.Util.Pairs;
import info.gridworld.cashgrab.Actions.ConsumeAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HunterCritter implements ActorListener {
  public static final int BABY_WAIT = 20;

  @Getter
  private final boolean female;

  private boolean firstRun = true;
  private AtomicInteger lastBaby = new AtomicInteger(0);

  @Override
  public Stream<Action> eventResponse(ActorEvent e, ActorInfo self, Set<ActorInfo> environment) {
    if (e instanceof StepEvent)
      return stepResponse(e, self, environment);
    return null;
  }

  public Stream<Action> stepResponse(ActorEvent e, ActorInfo self, Set<ActorInfo> environment) {
    final Stream.Builder<Action> actions = Stream.builder();
    final Consumer<Stream<Action>> addActs = s -> s.forEach(actions::add);
    if (!(firstRun = !firstRun)) {
      actions.add(new ColorAction(Color.ORANGE));
    }
    dispatch: {
      if (Util.applyNullable(HunterCritter.mate(lastBaby), addActs))
        break dispatch;
      if (Util.applyNullable(HunterCritter.hunt(self.getId(), environment), addActs))
        break dispatch;
      if (Util.applyNullable(HunterCritter.coinCamp(self.getId(), environment), addActs))
        break dispatch;
      Util.applyNullable(HunterCritter.wander(), addActs);
    }
    lastBaby.incrementAndGet();
    return actions.build();
  }

  public static Stream<Action> mate(final AtomicInteger lastBaby) {
    if (BABY_WAIT > lastBaby.get())
      return null;
    return null; //Stream.of();
  }

  public static Stream<Action> hunt(final int id, final Set<ActorInfo> env) {
    final ActorInfo prey = env.stream().filter(a -> {
      final String type = Util.coalesce(a.getType(), "");
      return !(type.equals(HunterCritter.class.getName()) || type.equals(Coin.class.getName()));
    }).sorted((a1, a2) -> Double.compare(Util.coalesce(a1.getDistance(), Double.MAX_VALUE),
        Util.coalesce(a2.getDistance(), Double.MAX_VALUE))).findFirst().orElse(null);
    if (prey == null) {
      return null;
    }
    System.out.println(id + " going after " + prey);
    final Stream.Builder<Action> actions = Stream.builder();
    if (Math.round(prey.getDistance()) <= 1) {
      actions.add(Pairs.applyNullable(Pairs.liftNull(prey.getDistance(), prey.getDirection()),
          ConsumeAction::new));
    } else {
      actions.add(new TurnAction((int) Math.round(prey.getDirection() / 45.0)));
      actions.add(new MoveAction(1));
    }
    return actions.build();
  }

  public static Stream<Action> coinCamp(final int id, final Set<ActorInfo> env) {
    final ActorInfo coin =
        env.stream().filter(a -> (Util.coalesce(a.getType(), "").equals(Coin.class.getName())))
            .sorted((a1, a2) -> Double.compare(Util.coalesce(a1.getDistance(), Double.MAX_VALUE),
                Util.coalesce(a2.getDistance(), Double.MAX_VALUE)))
            .findFirst().orElse(null);
    if (coin == null) {
      return null;
    }
    final Stream.Builder<Action> actions = Stream.builder();
    final double dist = coin.getDistance();
    final double dir = coin.getDirection();
    final int dirOffset = (int) Math.round(dir / 45.0);
    if (Math.round(dist) > 1) {
      // approach
      actions.add(new TurnAction(dirOffset));
    } else {
      System.out.println(id + " is juking out coins");
      // circle
      final Optional<ActorInfo> lastBlocker =
          env.stream().filter(a -> Math.abs(a.getDistance()) < Math.sqrt(2) + 0.1)
              .sorted((a, b) -> -Double.compare(a.getDirection() % 360, b.getDirection() % 360))
              .findFirst();
      actions.add(new TurnAction(
          (int) Math.round(lastBlocker.map(ActorInfo::getDirection).orElse(dir) / 45.0) - 1));
    }
    actions.add(new MoveAction(1));
    return actions.build();
  }

  public static Stream<Action> wander() {
    final int turn = new Random().nextInt(3) - 1;
    final Stream.Builder<Action> actions = Stream.builder();
    if (turn != 0) {
      actions.add(new TurnAction(turn));
    }
    actions.add(new MoveAction(1));
    return actions.build();
  }
}
