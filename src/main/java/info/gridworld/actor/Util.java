package info.gridworld.actor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
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
import lombok.Data;
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
    @Getter private final Object value;
    @Getter private final boolean right;

    public static <L, R> Either<L, R> right(R right) {
      return Either.of(right, true);
    }

    public static <L, R> Either<L, R> left(L left) {
      return Either.of(left, false);
    }

    public boolean isLeft() {
      return !right;
    }

    @SuppressWarnings("unchecked")
    public R getRightValue() {
      return right ? (R) value : null;
    }

    @SuppressWarnings("unchecked")
    public L getLeftValue() {
      return right ? null : (L) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T either(Function<L, T> ifLeft, Function<R, T> ifRight) {
      if (right) {
        return ifRight.apply((R) value);
      } else {
        return ifLeft.apply((L) value);
      }
    }

    @SuppressWarnings("unchecked")
    public <T> void either(Consumer<L> ifLeft, Consumer<R> ifRight) {
      if (right) {
        ifRight.accept((R) value);
      } else {
        ifLeft.accept((L) value);
      }
    }
  }
  @Data
  public class PipeStream {
    private final Pipe pipe;
    private final InputStream in;
    private final OutputStream out;

    public PipeStream() {
      Pipe pipe_ = null;
      try {
        pipe_ = Pipe.open();
      } catch (IOException e) {
        e.printStackTrace();
      }
      pipe = pipe_;
      in = Channels.newInputStream(pipe.source());
      out = Channels.newOutputStream(pipe.sink());
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
    final int endRow = (int) Math.min(myRow + radius,
      (numRows == -1) ? Integer.MAX_VALUE : numRows);
    // wheeee, square radii
    for (int row = startRow; row < endRow; row++) {
      final int startCol = (int) Math.max(myCol - radius, 0);
      final int endCol = (int) Math.min(myCol + radius,
        (numCols == -1) ? Integer.MAX_VALUE : numCols);
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
    public <A> Pair<A, A> dup(A a) {
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
    public <A, B, C> C apply(Pair<A, B> p, BiFunction<A, B, C> fun) {
      return fun.apply(p.getKey(), p.getValue());
    }

    public <A, B> Pair<B, B> thread(Pair<A, A> p, Function<A, B> fun) {
      return thread(p, Pairs.dup(fun));
    }

    public <A, B, C, D> Pair<B, D> thread(Pair<A, C> p,
      Pair<Function<A, B>, Function<C, D>> funs) {
      return thread(funs, p,
        new Pair<BiFunction<Function<A, B>, A, B>, BiFunction<Function<C, D>, C, D>>(
          Function::apply, Function::apply));
    }

    public <A, B, C> Pair<C, C> thread(Pair<A, A> p, Pair<B, B> q,
      BiFunction<A, B, C> fun) {
      return thread(p, q, Pairs.dup(fun));
    }

    public <A, B, C, D, E, F> Pair<C, F> thread(Pair<A, D> p, Pair<B, E> q,
      Pair<BiFunction<A, B, C>, BiFunction<D, E, F>> funs) {
      return new Pair<>(funs.getKey().apply(p.getKey(), q.getKey()),
        funs.getValue().apply(p.getValue(), q.getValue()));
    }

    public <A, B, C> C applyNullable(Pair<A, B> p, BiFunction<A, B, C> fun) {
      return Util.applyNullable(p.getKey(), p.getValue(), fun);
    }

    public <A, B, C> C applyNullableOrDefault(Pair<A, B> p,
      BiFunction<A, B, C> fun, C defaultValue) {
      return Util.applyNullableOrDefault(p.getKey(), p.getValue(), fun,
        defaultValue);
    }

    public <A, B> Pair<A, B> liftNull(A a, B b) {
      return Util.applyNullable(a, b, Pair<A, B>::new);
    }

    public <A, B> Pair<A, B> liftNull(Pair<A, B> p) {
      return liftNull(p.getKey(), p.getValue());
    }

    public <A, B> Pair<A, B> liftNullOrDefault(Pair<A, B> p,
      Pair<A, B> defaultValue) {
      return Util.applyNullableOrDefault(p.getKey(), p.getValue(),
        Pair<A, B>::new, defaultValue);
    }
  }

  public <A> A coalesce(A nullable, A ifNull) {
    return nullable == null ? ifNull : nullable;
  }

  public <A, B> B applyNullable(A a, Function<A, B> fun) {
    return a == null ? null : fun.apply(a);
  }

  public <A> void applyNullable(A a, Consumer<A> fun) {
    if (a != null) {
      fun.accept(a);
    }
  }

  public <A, B> B applyNullableOrDefault(A a, Function<A, B> fun,
    B defaultValue) {
    return a == null ? defaultValue : fun.apply(a);
  }

  public <A, B, C> C applyNullable(A a, B b, BiFunction<A, B, C> fun) {
    return (a == null || b == null) ? null : fun.apply(a, b);
  }

  public <A, B> void applyNullable(A a, B b, BiConsumer<A, B> fun) {
    if (a != null && b != null) {
      fun.accept(a, b);
    }
  }

  public <A, B, C> C applyNullableOrDefault(A a, B b, BiFunction<A, B, C> fun,
    C defaultValue) {
    return (a == null || b == null) ? defaultValue : fun.apply(a, b);
  }

  public Pair<Double, Double> rectToPolar(double x, double y) {
    return new Pair<>(Math.hypot(x, y), Math.atan2(y, x));
  }

  public Pair<Double, Double> rectToPolar(Pair<Double, Double> p) {
    return Pairs.apply(p, Util::rectToPolar);
  }

  /**
   * Converts a polar coordinate into a rectangular coordinate.
   * 
   * @param r Radius
   * @param theta Angle in radians
   * @return Pair of x, y in rectangular form
   */
  public Pair<Double, Double> polarToRect(double r, double theta) {
    return new Pair<>(r * Math.cos(theta), r * Math.sin(theta));
  }

  public Pair<Double, Double> polarToRect(Pair<Double, Double> p) {
    return Pairs.apply(p, Util::polarToRect);
  }

  public double polarUp(double polarRight) {
    return -(polarRight - Math.PI / 2);
  }

  public double polarRight(double polarUp) {
    return -polarUp + Math.PI / 2;
  }

  public double normalizeRadians(double theta) {
    return theta
      - (2 * Math.PI) * Math.floor((theta + Math.PI) / (2 * Math.PI));
  }

  public double normalizeDegrees(double theta) {
    return theta - 360.0 * Math.floor((theta + 180.0) / 360.0);
  }

  public Location sanitize(Location loc, int numRows, int numCols) {
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

  public Location sanitize(Location loc, Grid<?> grid) {
    return sanitize(loc, grid.getNumRows(), grid.getNumCols());
  }

  public Pair<Integer, Integer> locToRectInt(Location loc) {
    return new Pair<>(loc.getCol(), -loc.getRow());
  }

  public Pair<Double, Double> locToRect(Location loc) {
    return new Pair<>((double) loc.getCol(), (double) -loc.getRow());
  }

  public Location rectToLoc(Pair<Double, Double> rect) {
    return new Location((int) Math.round(-rect.getValue()),
      (int) Math.round(rect.getKey()));
  }

  public Pair<Double, Double> rectOffset(Pair<Double, Double> from,
    Pair<Double, Double> to) {
    return Util.Pairs.thread(to, from, (x, y) -> x - y);
  }

  public Shell genShell(ShellWorld world, AtomicReference<Integer> id,
    ActorListener brain) {
    return new Shell(id.getAndUpdate(x -> x + 1), brain, world.getWatchman());
  }

  public Stream<Shell> genShells(ShellWorld world, AtomicReference<Integer> id,
    Stream<? extends ActorListener> brains) {
    return brains.map(brain -> genShell(world, id, brain));
  }

  public Stream<Shell> genShells(ShellWorld world, AtomicReference<Integer> id,
    Stream<? extends ActorListener> brains,
    Map<Class<? extends Action>, BiConsumer<Shell, Action>> baseImpls) {
    return brains
      .map(brain -> genShell(world, id, brain).addAllImpls(baseImpls));
  }

  public Stream.Builder<Actor> addShells(Stream.Builder<Actor> actors,
    ShellWorld world, AtomicReference<Integer> id,
    Stream<? extends ActorListener> brains) {
    genShells(world, id, brains).forEach(actors::add);
    return actors;
  }

  public Stream.Builder<Actor> addShells(Stream.Builder<Actor> actors,
    ShellWorld world, AtomicReference<Integer> id,
    Stream<? extends ActorListener> brains,
    Map<Class<? extends Action>, BiConsumer<Shell, Action>> baseImpls) {
    brains.forEach(
      brain -> actors.add(genShell(world, id, brain).addAllImpls(baseImpls)));
    return actors;
  }

  public <T> void scatter(World<T> world, Stream<? extends T> things) {
    things.forEach(t -> Optional.ofNullable(world.getRandomEmptyLocation())
      .ifPresent(loc -> world.add(loc, t)));
  }
}
