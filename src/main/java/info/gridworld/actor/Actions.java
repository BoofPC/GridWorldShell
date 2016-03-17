package info.gridworld.actor;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import info.gridworld.actor.ShellWorld.Watchman;
import info.gridworld.actor.Util.Either;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import javafx.util.Pair;
import lombok.Data;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Actions {
  /**
   * The {@code MessageAction} class represents the act of sending a message to another actor in the
   * world.
   */
  @Data
  public class MessageAction implements Action {
    /**
     * If shouting, a left of the range. If directed to a specific actor, then a right of either: a
     * left polar offset, or an id.
     * 
     * @return how to find the recipient(s)
     */
    private final Either<Double, Either<Integer, Pair<Double, Double>>> recipient;
    /**
     * The message to send.
     * 
     * @return the message
     */
    private final Serializable message;

    @Override
    public boolean isFinal() {
      return false;
    }

    public static BiConsumer<Shell, Action> impl(final double maxDist) {
      return (final Shell that, final Action a) -> {
        final Either<Double, Either<Integer, Pair<Double, Double>>> scope =
          ((MessageAction) a).getRecipient();
        final Serializable messageIn = ((MessageAction) a).getMessage();
        Serializable message_ = null;
        Pipe pipe_ = null;
        try {
          pipe_ = Pipe.open();
        } catch (IOException e) {
          e.printStackTrace();
        }
        final Pipe pipe = pipe_;
        try {
          new ObjectOutputStream(Channels.newOutputStream(pipe_.sink()))
            .writeObject(messageIn);
          try {
            message_ = (Serializable) new ObjectInputStream(
              Channels.newInputStream(pipe.source())).readObject();
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        final Serializable message = message_;
        final Watchman watchman = that.getWatchman();
        final Function<Integer, ReportEvents.MessageReportEvent> report =
          id -> new ReportEvents.MessageReportEvent(that, that.getId(), id,
            message);
        if (scope.isRight()) {
          final Either<Integer, Pair<Double, Double>> recipient =
            scope.getRightValue();
          final Grid<Actor> grid = that.getGrid();
          if (recipient.isRight()) {
            // check and report MessageReportEvent at offset
            final Pair<Double, Double> offsetPolar = recipient.getRightValue();
            final double distance = Math.min(offsetPolar.getKey(), maxDist);
            final double direction = offsetPolar.getValue();
            final Location loc = that.getLocation();
            final Pair<Double, Double> offsets = Util.polarToRect(distance,
              Util.polarRight(Math.toRadians(that.getDirection() + direction)));
            final Location targetLoc =
              new Location((int) (loc.getRow() + offsets.getKey()),
                (int) (loc.getCol() + offsets.getValue()));
            final Actor target_ = grid.get(targetLoc);
            if ((target_ == null) || !(target_ instanceof Shell)) {
              return;
            }
            final Shell target = (Shell) target_;
            watchman.report(report.apply(target.getId()));
          } else {
            final int recipientId = recipient.getLeftValue();
            final Shell recipientShell =
              (Shell) watchman.getWorld().getShells().get(recipientId);
            final Location loc = that.getLocation();
            final Pair<Double, Double> locRect = Util.locToRect(loc);
            final Location recipientLoc = recipientShell.getLocation();
            final Pair<Double, Double> recipientLocRect =
              Util.locToRect(recipientLoc);
            final Pair<Double, Double> offsetRect =
              Util.Pairs.thread(locRect, recipientLocRect, (x, y) -> x - y);
            final double distance = Util.Pairs.apply(offsetRect, Math::hypot);
            if (distance > maxDist) {
              return;
            }
            watchman.report(report.apply(recipient.getLeftValue()));
          }
        } else {
          final double shoutRange = Math.min(scope.getLeftValue(), maxDist);
          final Stream<Actor> listeners = Util.actorsInRadius(that, shoutRange);
          listeners.filter(act -> act instanceof Shell).map(s -> (Shell) s)
            .map(Shell::getId).map(report).forEach(watchman::report);
        }
      };
    }
  }
  /**
   * The {@code MessageAction} class represents the act of moving forward.
   */
  @Data
  public class MoveAction implements Action {
    /**
     * The distance to travel.
     */
    private final int distance;

    @Override
    public boolean isFinal() {
      return true;
    }

    public static BiConsumer<Shell, Action> impl(final int maxDist) {
      return (final Shell that, final Action a) -> {
        final int distance = Math.min(((MoveAction) a).getDistance(), maxDist);
        final int direction = that.getDirection();
        final Grid<Actor> grid = that.getGrid();
        Location dest = that.getLocation();
        for (int i = distance; i > 0; i--) {
          dest = dest.getAdjacentLocation(direction);
        }
        dest = Util.sanitize(dest, grid);
        final Actor destActor = grid.get(dest);
        if (destActor != null) {
          that.getWatchman().report(new ReportEvents.CollisionReportEvent(that,
            that, destActor, direction));
          return;
        }
        that.moveTo(dest);
      };
    }
  }
  /**
   * The {@code MessageAction} class represents the act of turning.
   */
  @Value
  public class TurnAction implements Action {
    /**
     * The angle to turn, in eighths of a turn.
     */
    private final int angle;

    @Override
    public boolean isFinal() {
      return false;
    }

    public static BiConsumer<Shell, Action> impl() {
      return (final Shell that, final Action a) -> {
        final int angle = ((TurnAction) a).getAngle();
        final int direction = that.getDirection();
        that.setDirection((direction + angle * 45) % 360);
      };
    }
  }
  /**
   * The {@code MessageAction} class represents the act of chaning color.
   */
  @Data
  public class ColorAction implements Action {
    /**
     * The color to change to.
     */
    private final Color color;

    @Override
    public boolean isFinal() {
      return false;
    }

    public static BiConsumer<Shell, Action> impl() {
      return (final Shell that, final Action a) -> {
        final Color color = ((ColorAction) a).getColor();
        that.setColor(color);
      };
    }
  }
}
