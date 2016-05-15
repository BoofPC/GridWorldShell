package info.gridworld.cashgrab;

import java.awt.Color;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MessageAction;
import info.gridworld.actor.ActorEvent;
import info.gridworld.actor.ActorEvent.ActorInfo;
import info.gridworld.actor.ActorEvents.MessageEvent;
import info.gridworld.actor.ActorEvents.StepEvent;
import info.gridworld.actor.ActorListener;
import info.gridworld.actor.Util;
import info.gridworld.actor.Util.Either;
import info.gridworld.actor.Util.Pairs;
import info.gridworld.cashgrab.Actions.ConsumeAction;
import javafx.util.Pair;
import lombok.Data;
import lombok.Value;

@Data
public class HunterCritterOld implements ActorListener {
  public static final int BABY_TIME = 30;

  @Value
  public static class MatingCall implements Serializable {
    @Value
    public static class Info implements Serializable {
      private static final long serialVersionUID = 1L;
      UUID uuid;
      Integer id;
      Double mateDirection;
      Double mateDistance;
      Double mateId;
    }

    private static final long serialVersionUID = 1L;
    Info info;
    boolean isFemale;
    int pass;
  }

  private final boolean isFemale;
  private boolean firstRun = true;
  private int time = 0;
  private int lastBaby = 0;
  private MatingCall.Info mate = null;
  private final UUID uuid = UUID.randomUUID();

  @Override
  public Stream<Action> eventResponse(final ActorEvent e, final ActorInfo self,
      final Set<ActorInfo> environment) {
    final Stream.Builder<Action> actions = Stream.builder();
    if (e instanceof MessageEvent) {
      final Serializable message_ = ((MessageEvent) e).getMessage();
      matingCall: if (message_ instanceof MatingCall) {
        final MatingCall message = (MatingCall) message_;
        if (this.time >= HunterCritterOld.BABY_TIME) {
          if (this.mate != null) {
            if (!this.mate.getUuid().equals(message.getInfo().getUuid())) {
              break matingCall;
            }
          }
        }
      }
    } else if (e instanceof StepEvent) {
      finalAction: {
        if (this.firstRun) {
          actions.add(new ColorAction(Color.ORANGE));
        }
        this.time++;
        this.lastBaby++;
        if (this.lastBaby > HunterCritterOld.BABY_TIME) {
          if (this.mate != null) {
            break finalAction;
          }
          final Pair<Action, ActorInfo> courtAction = this.court(self, environment);
          if (courtAction != null) {
            //mate = Optional.of(courtAction.get());
            actions.add(courtAction.getKey());
          }
        }
        final Action eatAction = this.eatPrey(self, environment);
        if (eatAction != null) {
          actions.add(eatAction);
          break finalAction;
        }
        /*if (Math.random() < 0.5) {
          actions.add(new TurnAction(-1));
        } else {
          actions.add(new TurnAction(1));
        }
        actions.add(new MoveAction(1));*/
        break finalAction;
      }
    }
    return actions.build();
  }

  private Pair<Action, ActorInfo> court(final ActorInfo self, final Set<ActorInfo> environment) {
    final String name = this.getClass().getName();
    final ActorInfo lover = environment.stream()
        .filter(a -> Util.coalesce(a.getType(), "").equals(name)).findAny().orElse(null);
    if (lover == null) {
      return null;
    }
    final Pair<Double, Double> loverLocation =
        Pairs.liftNull(lover.getDistance(), lover.getDirection());
    final Function<Pair<Double, Double>, MessageAction> messageFun =
        p -> new MessageAction(Either.right(Either.right(p)), "hey baby");
    return Pairs.liftNull(Util.applyNullable(loverLocation, messageFun), lover);
  }

  private static Action eatPrey(final ActorInfo self, final Set<ActorInfo> environment) {
    final ActorInfo prey = environment.stream()
        .filter(a -> !Util.coalesce(a.getType(), "").equals(HunterCritterOld.class.getName()))
        .sorted((a, b) -> Double.compare(Util.coalesce(a.getDistance(), Double.MAX_VALUE),
            Util.coalesce(b.getDistance(), Double.MAX_VALUE)))
        .findFirst().orElse(null);
    if (prey == null) {
      return null;
    }
    System.out.println(self.getId() + " going after " + prey);
    return Pairs.applyNullable(Pairs.liftNull(prey.getDistance(), prey.getDirection()),
        ConsumeAction::new);
  }
}
