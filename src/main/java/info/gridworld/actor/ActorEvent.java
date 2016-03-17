package info.gridworld.actor;

import java.awt.Color;
import java.util.EventObject;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * The {@code ActorEvent} class represents activity in a world that actors can respond to.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActorEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  /**
   * The {@code ActorInfo} data class represents another {@link Actor} in the world. Any field may
   * be {@code null}.
   */
  @Value
  @Builder
  public static class ActorInfo {
    /**
     * The unique identifier of this actor.
     * 
     * @return the actor's unique identifier
     */
    Integer id;
    /**
     * The type of this actor.
     * 
     * @return the actor's type
     */
    String type;
    /**
     * The distance away from the reciever's perspective.
     * 
     * @return the actor's distance away
     */
    Double distance;
    /**
     * The direction offset from the reciever's perspective.
     * 
     * @return the offset of the actor's direction
     */
    Double direction;
    /**
     * The color of the actor from the reciever's perspective.
     * 
     * @return the actor's color
     */
    Color color;
  }

  public ActorEvent(final Object source) {
    super(source);
  }
}
