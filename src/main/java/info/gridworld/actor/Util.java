package info.gridworld.actor;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import info.gridworld.world.World;
import javafx.util.Pair;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;


@UtilityClass
public class Util {
  @ToString
  @EqualsAndHashCode
  @RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
  public class Either<L, R> implements Serializable {
    private static final long serialVersionUID = 1L;
    @Getter
    private final Object value;
    @Getter
    private final boolean right;

    public static <L, R> Either<L, R> right(final R right) {
      return Either.of(right, true);
    }

    public static <L, R> Either<L, R> left(final L left) {
      return Either.of(left, false);
    }

    public boolean isLeft() {
      return !this.right;
    }

    @SuppressWarnings("unchecked")
    public R getRightValue() {
      return this.right ? (R) this.value : null;
    }

    @SuppressWarnings("unchecked")
    public L getLeftValue() {
      return this.right ? null : (L) this.value;
    }

    @SuppressWarnings("unchecked")
    public <T> T either(final Function<L, T> ifLeft, final Function<R, T> ifRight) {
      if (this.right) {
        return ifRight.apply((R) this.value);
      } else {
        return ifLeft.apply((L) this.value);
      }
    }

    @SuppressWarnings("unchecked")
    public <T> void either(final Consumer<L> ifLeft, final Consumer<R> ifRight) {
      if (this.right) {
        ifRight.accept((R) this.value);
      } else {
        ifLeft.accept((L) this.value);
      }
    }
  }

  public Stream<Actor> actorsInRadius(final Shell that, final double radius) {
    final Stream.Builder<Actor> stream = Stream.builder();
    final Grid<Actor> grid = that.getGrid();
    final int numRows = grid.getNumRows();
    final int numCols = grid.getNumCols();
    final Location myLoc = that.getLocation();
    final int myRow = myLoc.getRow();
    final int myCol = myLoc.getCol();
    final int startRow = (int) Math.max(myRow - radius, 0);
    final int endRow =
        (int) Math.min(myRow + radius, (numRows == -1) ? Integer.MAX_VALUE : numRows);
    // wheeee, square radii
    for (int row = startRow; row < endRow; row++) {
      final int startCol = (int) Math.max(myCol - radius, 0);
      final int endCol =
          (int) Math.min(myCol + radius, (numCols == -1) ? Integer.MAX_VALUE : numCols);
      for (int col = Math.max(startCol, 0); col < endCol; col++) {
        if (row == myRow && col == myCol) {
          continue;
        }
        final Location loc = new Location(row, col);
        final Actor actor = grid.get(loc);
        if (actor == null) {
          continue;
        }
        stream.add(actor);
      }
    }
    return stream.build();
  }

  @UtilityClass
  public class Pairs {
    public <A> Pair<A, A> dup(final A a) {
      return new Pair<>(a, a);
    }

    /**
     * Apply a <code>BiFunction</code> using a <code>Pair</code>'s values as arguments.
     * 
     * @param <A> the first item in the pair
     * @param <B> the second item in the pair
     * @param <C> what's returned from the function
     * @param p the values to be used
     * @param fun the function to apply
     * @return the application of <code>xs</code> to <code>fun</code>
     */
    public <A, B, C> C apply(final Pair<A, B> p, final BiFunction<A, B, C> fun) {
      return fun.apply(p.getKey(), p.getValue());
    }

    public <A, B> Pair<B, B> thread(final Pair<A, A> p, final Function<A, B> fun) {
      return Pairs.thread(p, Pairs.dup(fun));
    }

    public <A, B, C, D> Pair<B, D> thread(final Pair<A, C> p,
        final Pair<Function<A, B>, Function<C, D>> funs) {
      return Pairs.thread(funs, p,
          new Pair<BiFunction<Function<A, B>, A, B>, BiFunction<Function<C, D>, C, D>>(
              Function::apply, Function::apply));
    }

    public <A, B, C> Pair<C, C> thread(final Pair<A, A> p, final Pair<B, B> q,
        final BiFunction<A, B, C> fun) {
      return Pairs.thread(p, q, Pairs.dup(fun));
    }

    public <A, B, C, D, E, F> Pair<C, F> thread(final Pair<A, D> p, final Pair<B, E> q,
        final Pair<BiFunction<A, B, C>, BiFunction<D, E, F>> funs) {
      return new Pair<>(funs.getKey().apply(p.getKey(), q.getKey()),
          funs.getValue().apply(p.getValue(), q.getValue()));
    }

    public <A, B, C> C applyNullable(final Pair<A, B> p, final BiFunction<A, B, C> fun) {
      return Util.applyNullable(p.getKey(), p.getValue(), fun);
    }

    public <A, B, C> C applyNullableOrDefault(final Pair<A, B> p, final BiFunction<A, B, C> fun,
        final C defaultValue) {
      return Util.applyNullableOrDefault(p.getKey(), p.getValue(), fun, defaultValue);
    }

    public <A, B> Pair<A, B> liftNull(final A a, final B b) {
      return Util.applyNullable(a, b, Pair<A, B>::new);
    }

    public <A, B> Pair<A, B> liftNull(final Pair<A, B> p) {
      return Pairs.liftNull(p.getKey(), p.getValue());
    }

    public <A, B> Pair<A, B> liftNullOrDefault(final Pair<A, B> p, final Pair<A, B> defaultValue) {
      return Util.applyNullableOrDefault(p.getKey(), p.getValue(), Pair<A, B>::new, defaultValue);
    }
  }

  public <A> A coalesce(final A nullable, final A ifNull) {
    return nullable == null ? ifNull : nullable;
  }

  public <A, B> B applyNullable(final A a, final Function<A, B> fun) {
    return a == null ? null : fun.apply(a);
  }

  public <A> void applyNullable(final A a, final Consumer<A> fun) {
    if (a != null) {
      fun.accept(a);
    }
  }

  public <A, B> B applyNullableOrDefault(final A a, final Function<A, B> fun,
      final B defaultValue) {
    return a == null ? defaultValue : fun.apply(a);
  }

  public <A, B, C> C applyNullable(final A a, final B b, final BiFunction<A, B, C> fun) {
    return (a == null || b == null) ? null : fun.apply(a, b);
  }

  public <A, B> void applyNullable(final A a, final B b, final BiConsumer<A, B> fun) {
    if (a != null && b != null) {
      fun.accept(a, b);
    }
  }

  public <A, B, C> C applyNullableOrDefault(final A a, final B b, final BiFunction<A, B, C> fun,
      final C defaultValue) {
    return (a == null || b == null) ? defaultValue : fun.apply(a, b);
  }

  public Pair<Double, Double> rectToPolar(final double x, final double y) {
    return new Pair<>(Math.hypot(x, y), Math.atan2(y, x));
  }

  public Pair<Double, Double> rectToPolar(final Pair<Double, Double> p) {
    return Pairs.apply(p, Util::rectToPolar);
  }

  /**
   * Converts a polar coordinate into a rectangular coordinate.
   * 
   * @param r Radius
   * @param theta Angle in radians
   * @return Pair of x, y in rectangular form
   */
  public Pair<Double, Double> polarToRect(final double r, final double theta) {
    return new Pair<>(r * Math.cos(theta), r * Math.sin(theta));
  }

  public Pair<Double, Double> polarToRect(final Pair<Double, Double> p) {
    return Pairs.apply(p, Util::polarToRect);
  }

  public double polarUp(final double polarRight) {
    return -(polarRight - Math.PI / 2);
  }

  public double polarRight(final double polarUp) {
    return -polarUp + Math.PI / 2;
  }

  public double normalizeRadians(final double theta) {
    return theta - (2 * Math.PI) * Math.floor((theta + Math.PI) / (2 * Math.PI));
  }

  public double normalizeDegrees(final double theta) {
    return theta - 360.0 * Math.floor((theta + 180.0) / 360.0);
  }

  public Location sanitize(final Location loc, final int numRows, final int numCols) {
    int row = loc.getRow();
    int col = loc.getCol();
    if (numRows != -1) {
      row = Math.max(Math.min(row, numRows - 1), 0);
    }
    if (numCols != -1) {
      col = Math.max(Math.min(col, numCols - 1), 0);
    }
    return new Location(row, col);
  }

  public Location sanitize(final Location loc, final Grid<?> grid) {
    return Util.sanitize(loc, grid.getNumRows(), grid.getNumCols());
  }

  public Pair<Integer, Integer> locToRectInt(final Location loc) {
    return new Pair<>(loc.getCol(), -loc.getRow());
  }

  public Pair<Double, Double> locToRect(final Location loc) {
    return new Pair<>((double) loc.getCol(), (double) -loc.getRow());
  }

  public Location rectToLoc(final Pair<Double, Double> rect) {
    return new Location((int) Math.round(-rect.getValue()), (int) Math.round(rect.getKey()));
  }

  public Pair<Double, Double> rectOffset(final Pair<Double, Double> from,
      final Pair<Double, Double> to) {
    return Util.Pairs.thread(to, from, (x, y) -> x - y);
  }

  public Shell genShell(final ShellWorld world, final AtomicReference<Integer> id,
      final ActorListener brain) {
    return new Shell(id.getAndUpdate(x -> x + 1), brain, world.getWatchman());
  }

  public Stream<Shell> genShells(final ShellWorld world, final AtomicReference<Integer> id,
      final Stream<? extends ActorListener> brains) {
    return brains.map(brain -> Util.genShell(world, id, brain));
  }

  public Stream<Shell> genShells(final ShellWorld world, final AtomicReference<Integer> id,
      final Stream<? extends ActorListener> brains,
      final Map<Class<? extends Action>, BiConsumer<Shell, Action>> baseImpls) {
    return brains.map(brain -> Util.genShell(world, id, brain).addAllImpls(baseImpls));
  }

  public Stream.Builder<Actor> addShells(final Stream.Builder<Actor> actors, final ShellWorld world,
      final AtomicReference<Integer> id, final Stream<? extends ActorListener> brains) {
    Util.genShells(world, id, brains).forEach(actors::add);
    return actors;
  }

  public Stream.Builder<Actor> addShells(final Stream.Builder<Actor> actors, final ShellWorld world,
      final AtomicReference<Integer> id, final Stream<? extends ActorListener> brains,
      final Map<Class<? extends Action>, BiConsumer<Shell, Action>> baseImpls) {
    brains.forEach(brain -> actors.add(Util.genShell(world, id, brain).addAllImpls(baseImpls)));
    return actors;
  }

  public <T> void scatter(final World<T> world, final Stream<? extends T> things) {
    things.forEach(t -> Optional.ofNullable(world.getRandomEmptyLocation())
        .ifPresent(loc -> world.add(loc, t)));
  }

  public void scatterShells(final ShellWorld world,
      final Map<Class<? extends Action>, BiConsumer<Shell, Action>> baseImpls,
      final AtomicReference<Integer> id, Stream<? extends ActorListener> brains,
      Stream<Function<Shell, Shell>> modifiers) {
    final Stream<Shell> generatedShells =
        genShells(world, id, brains, baseImpls).map(modifiers.reduce(f -> f, Function::andThen));
    Util.scatter(world, generatedShells);
  }
}
