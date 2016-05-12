package info.gridworld.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import javafx.util.Pair;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ShellWorld extends ActorWorld {
  public static class Watchman implements ReportListener {
    @Getter private final @NonNull ShellWorld world;
    private final Map<Class<? extends ReportEvent>, BiConsumer<Watchman, ReportEvent>> reportImpls =
      new HashMap<>();

    public Watchman(final @NonNull ShellWorld world) {
      this.world = world;
    }

    public Watchman addImpl(final Class<? extends ReportEvent> clazz,
      final BiConsumer<Watchman, ReportEvent> impl) {
      this.reportImpls.put(clazz, impl);
      return this;
    }

    public Watchman addAllImpls(
      final Map<Class<? extends ReportEvent>, BiConsumer<Watchman, ReportEvent>> impls) {
      this.reportImpls.putAll(impls);
      return this;
    }

    @Override
    public void report(final ReportEvent r) {
      final Class<? extends ReportEvent> clazz = r.getClass();
      final BiConsumer<Watchman, ReportEvent> impl =
        this.reportImpls.get(clazz);
      if (impl != null) {
        impl.accept(this, r);
      }
    }
  }

  private final @NonNull Watchman watchman = new Watchman(this);
  private final @NonNull Map<Integer, Shell> shells = new HashMap<>();
  private final @NonNull Map<BiConsumer<ShellWorld, Pair<List<Actor>, AtomicReference<Object>>>, AtomicReference<Object>> onSteps;

  @Override
  public void add(final Location loc, final Actor occupant) {
    if (occupant instanceof Shell) {
      final Shell shell = (Shell) occupant;
      this.shells.put(shell.getId(), shell);
    }
    super.add(loc, occupant);
  }

  @Override
  public void step() {
    final Grid<Actor> grid = this.getGrid();
    final List<Actor> actors = new ArrayList<>();
    for (final Location loc : grid.getOccupiedLocations()) {
      final Actor actor = grid.get(loc);
      actors.add(actor);
      if (actor instanceof Shell) {
        ((Shell) actor)
          .respond(new ActorEvents.StepEvent("I see what you did there"));
      }
    }
    for (final Actor actor : actors) {
      // only act if another actor hasn't removed actor
      if (actor.getGrid() == grid) {
        actor.act();
      }
    }
    onSteps.forEach((k, v) -> k.accept(this, new Pair<>(actors, v)));
  }
  
  public ShellWorld(final Grid<Actor> grid, final Iterable<BiConsumer<ShellWorld, Pair<List<Actor>, AtomicReference<Object>>>> onSteps) {
    super(grid);
    this.onSteps = new HashMap<>();
    onSteps.forEach(f -> this.onSteps.put(f, new AtomicReference<>()));
  }

  public ShellWorld(final Grid<Actor> grid) {
    this(grid, null);
  }

  public ShellWorld() {
    super();
    this.onSteps = new HashMap<>();
  }
  
  public static void clearMessage(final ShellWorld that, final Pair<List<Actor>, AtomicReference<Object>> data) {
    that.setMessage("");
  }
}
