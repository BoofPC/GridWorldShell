package info.gridworld.actor;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ActorEvents {
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class StepEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;

    public StepEvent(final Object source) {
      super(source, "Step");
    }
  }
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class MessageEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;
    private final Serializable message;

    public MessageEvent(final Object source, final Serializable message) {
      super(source, "Step");
      this.message = message;
    }
  }
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class CollisionEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;
    private final @NonNull ActorInfo collidedWith;

    public CollisionEvent(final Object source,
      final @NonNull ActorInfo collidedWith) {
      super(source, "Collision");
      this.collidedWith = collidedWith;
    }
  }
}
