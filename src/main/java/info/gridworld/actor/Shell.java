package info.gridworld.actor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ShellWorld.Watchman;
import info.gridworld.grid.Location;
import javafx.util.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class Shell extends Actor {
  @Getter
  @RequiredArgsConstructor
  public enum Tags implements info.gridworld.actor.Tag {
    PUSHABLE("Shell.Pushable");
    private final String tag;
  }

  @Getter
  private final int id;
  @Getter
  private final @NonNull ActorListener brain;
  @Getter
  private final @NonNull Watchman watchman;
  private Stream<Action> nextActions;
  private final @NonNull Map<Class<? extends Action>, BiConsumer<Shell, Action>> actionImpls =
      new HashMap<>();
  @Getter
  private final @NonNull Map<String, Object> tags = new HashMap<>();

  public Shell(final int id, final @NonNull ActorListener brain, final @NonNull Watchman watchman) {
    this.id = id;
    this.brain = brain;
    this.watchman = watchman;
    this.actionImpls.put(MoveAction.class, MoveAction.impl(1));
    this.actionImpls.put(TurnAction.class, TurnAction.impl());
    this.actionImpls.put(ColorAction.class, ColorAction.impl());
  }

  public Shell addImpl(final Class<? extends Action> clazz, final BiConsumer<Shell, Action> impl) {
    this.actionImpls.put(clazz, impl);
    return this;
  }

  public Shell addAllImpls(final Map<Class<? extends Action>, BiConsumer<Shell, Action>> impls) {
    this.actionImpls.putAll(impls);
    return this;
  }

  public Shell tag(final String tag) {
    this.tag(tag, null);
    return this;
  }

  public Shell tag(final String tag, final Object value) {
    this.tags.put(tag, value);
    return this;
  }

  public Shell tag(final Tag tag) {
    return this.tag(tag.getTag());
  }

  public Shell tag(final Tag tag, final Object value) {
    return this.tag(tag.getTag(), value);
  }

  public Object getTag(final String tag) {
    return this.tags.get(tag);
  }

  public Object getTagOrDefault(final String tag, final Object defaultValue) {
    final Object value = this.getTag(tag);
    return value == null ? defaultValue : value;
  }

  public Object getTag(final Tag tag) {
    return this.tags.get(tag.getTag());
  }

  public Object getTagOrDefault(final Tag tag, final Object defaultValue) {
    final Object value = this.getTag(tag);
    return value == null ? defaultValue : value;
  }

  public void respond(final ActorEvent event) {
    final ActorInfo that =
        ActorInfo.builder().id(this.id).distance(0.0).direction(0.0).color(this.getColor()).build();
    final Set<ActorInfo> environment = new HashSet<>();
    final double myDirection = this.getDirection();
    final Location myLoc = this.getLocation();
    final Pair<Double, Double> myLocRect = Util.locToRect(myLoc);
    final double sightRadius = 3;
    Util.actorsInRadius(this, sightRadius).forEach(actor -> {
      Class<?> actorType_;
      if (actor instanceof Shell) {
        actorType_ = ((Shell) actor).getBrain().getClass();
      } else {
        actorType_ = actor.getClass();
      }
      final Class<?> actorType = actorType_;
      final ActorInfo.ActorInfoBuilder actorInfo = ActorInfo.builder().type(actorType.getName());
      if (actor instanceof Shell) {
        actorInfo.id(((Shell) actor).getId());
      }
      final Location actorLoc = actor.getLocation();
      final Pair<Double, Double> actorLocRect = Util.locToRect(actorLoc);
      final Pair<Double, Double> offset = Util.rectOffset(myLocRect, actorLocRect);
      final Pair<Double, Double> offsetPolar = Util.rectToPolar(offset);
      final double offsetDirection = Math.toDegrees(Util.polarUp(offsetPolar.getValue()));
      actorInfo.distance(offsetPolar.getKey())
          .direction(Util.normalizeDegrees(offsetDirection - myDirection));
      actorInfo.color(actor.getColor());
      environment.add(actorInfo.build());
    });
    this.nextActions = this.brain.eventResponse(event, that, environment);
  }

  @Override
  public void act() {
    if (this.nextActions == null) {
      return;
    }
    for (final Action a : (Iterable<Action>) this.nextActions::iterator) {
      if (a == null) {
        continue;
      }
      Class<?> clazz = a.getClass();
      BiConsumer<Shell, Action> impl = null;
      while (clazz != null && (impl = this.actionImpls.get(clazz)) == null) {
        clazz = clazz.getSuperclass();
      }
      if (impl != null) {
        impl.accept(this, a);
      }
      if (a.isFinal()) {
        break;
      }
    }
    this.nextActions = null;
  }
}
