package info.gridworld.actor;

import java.io.Serializable;
import java.util.function.BiConsumer;

import info.gridworld.actor.Shell.Tags;
import info.gridworld.actor.ShellWorld.Watchman;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReportEvents {
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class CollisionReportEvent extends ReportEvent {
    private static final long serialVersionUID = 1L;
    private final @NonNull Actor collider;
    private final @NonNull Actor collidedWith;
    private final int direction;

    public CollisionReportEvent(final Object source,
      final @NonNull Actor collider, final @NonNull Actor collidedWith,
      final int direction) {
      super(source);
      this.collider = collider;
      this.collidedWith = collidedWith;
      this.direction = direction;
    }

    public static BiConsumer<Watchman, ReportEvent> impl() {
      return (final Watchman that, final ReportEvent r_) -> {
        final CollisionReportEvent r = (CollisionReportEvent) r_;
        final Actor collidedWith_ = r.getCollidedWith();
        if (!(collidedWith_ instanceof Shell)) {
          return;
        }
        final Shell collidedWith = (Shell) collidedWith_;
        if (!(boolean) collidedWith.getTagOrDefault(Tags.PUSHABLE, false)) {
          return;
        }
        final int direction = r.getDirection();
        final Actor collider = r.getCollider();
        final Grid<Actor> grid = collider.getGrid();
        final Location destLoc = collidedWith.getLocation();
        final Location pushLoc =
          Util.sanitize(destLoc.getAdjacentLocation(direction), grid);
        if (destLoc.equals(pushLoc)) {
          return;
        }
        final Actor displaced = grid.get(pushLoc);
        if (displaced != null) {
          that.report(
            new CollisionReportEvent(that, collidedWith, displaced, direction));
          return;
        }
        collidedWith.moveTo(pushLoc);
        collider.moveTo(destLoc);
      };
    }
  }
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class MessageReportEvent extends ReportEvent {
    private static final long serialVersionUID = 1L;
    private final int senderId;
    private final int recipientId;
    private final Serializable message;

    public MessageReportEvent(final Object source, final int senderId,
      final int recipientId, final Serializable message) {
      super(source);
      this.senderId = senderId;
      this.recipientId = recipientId;
      this.message = message;
    }

    public static BiConsumer<Watchman, ReportEvent> impl() {
      return (final Watchman that, final ReportEvent r_) -> {
        final MessageReportEvent r = (MessageReportEvent) r_;
        final int recipientId = r.getRecipientId();
        final Serializable message = r.getMessage();
        final Shell recipient = that.getWorld().getShells().get(recipientId);
        if (recipient.getGrid() == null) {
          return;
        }
        recipient.respond(
          new ActorEvents.MessageEvent("I see what you did there", message));
      };
    }
  }
}
