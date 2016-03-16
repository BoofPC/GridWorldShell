package info.gridworld.actor;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ActorEvents {
  /**
   * The {@code StepEvent} class represents a step in the state of the world.
   */
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class StepEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;

    public StepEvent(final Object source) {
      super(source);
    }
  }
  /**
   * The {@code MessageEvent} class represents a received message to allow a response and state
   * change before the next {@link StepEvent}.
   */
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class MessageEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;
    /**
     * The message to pass. It <em>will</em> be serialized and deserialized in transit.
     */
    private final Serializable message;

    public MessageEvent(final Object source, final Serializable message) {
      super(source);
      this.message = message;
    }
  }
  /**
   * The {@code CollisionEvent} class represents a report of a collision with another actor.
   */
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class CollisionEvent extends ActorEvent {
    private static final long serialVersionUID = 1L;
    /**
     * The actor that was collided with.
     */
    private final @NonNull ActorInfo collidedWith;

    public CollisionEvent(final Object source,
      final @NonNull ActorInfo collidedWith) {
      super(source);
      this.collidedWith = collidedWith;
    }
  }
}
