package info.gridworld.actor;

import java.util.EventObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  public ReportEvent(final Object source) {
    super(source);
  }
}
