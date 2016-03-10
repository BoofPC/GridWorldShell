package info.gridworld.actor;

public interface Action {
  default String getType() {
    final String name = this.getClass().getName();
    return name.substring(0, name.length() - 6);
  }

  boolean isFinal();
}
