package info.gridworld.actor;

import java.util.EventListener;

public interface ReportListener extends EventListener {
  void report(ReportEvent r);
}
