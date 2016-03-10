package info.gridworld.actor;

import java.awt.Color;
import java.util.EventObject;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActorEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  @Value
  @Builder
  public static class ActorInfo {
    Integer id;
    String type;
    Double distance;
    Double direction;
    Color color;
  }

  private final String type;

  public ActorEvent(final Object source, final String type) {
    super(source);
    this.type = type;
  }
}
