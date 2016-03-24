import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MessageAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.Actor;
import info.gridworld.actor.ActorListener;
import info.gridworld.actor.ReportEvents.CollisionReportEvent;
import info.gridworld.actor.ReportEvents.MessageReportEvent;
import info.gridworld.actor.Shell;
import info.gridworld.actor.ShellWorld;
import info.gridworld.actor.Util;
import info.gridworld.cashgrab.Actions.CollectCoinAction;
import info.gridworld.cashgrab.Actions.ConsumeAction;
import info.gridworld.cashgrab.CalebBug;
import info.gridworld.cashgrab.CashGrab;
import info.gridworld.cashgrab.HunterCritter;
import info.gridworld.grid.BoundedGrid;
import info.gridworld.grid.Grid;
import javafx.util.Pair;

public class CashGrabRunner {
  public static void main(final String[] args) {
    final Grid<Actor> grid = new BoundedGrid<>(10, 10);
    final ShellWorld world = new ShellWorld(grid);
    world.getWatchman()
      .addImpl(CollisionReportEvent.class, CollisionReportEvent.impl())
      .addImpl(MessageReportEvent.class, MessageReportEvent.impl());
    final Map<Class<? extends Action>, BiConsumer<Shell, Action>> baseImpls =
      new HashMap<>();
    baseImpls.put(MessageAction.class, MessageAction.impl(5));
    baseImpls.put(MoveAction.class, MoveAction.impl(1));
    baseImpls.put(TurnAction.class, TurnAction.impl());
    baseImpls.put(ColorAction.class, ColorAction.impl());
    baseImpls.put(CollectCoinAction.class, CollectCoinAction.impl(2, 1));
    baseImpls.put(ConsumeAction.class, ConsumeAction.impl(1));
    final Stream.Builder<Actor> players = Stream.builder();
    final AtomicReference<Integer> id = new AtomicReference<>(0);
    final AtomicReference<Integer> bankId = new AtomicReference<>(0);
    final CashGrab.Bank bank = new CashGrab.Bank();
    Util.scatter(world,
      Stream.generate(() -> CashGrab.genCoin(bankId, bank, 5)).limit(1));
    final Stream.Builder<ActorListener> brains = Stream.builder();
    Util.addShells(players, world, id, brains.build(), baseImpls);
    Util.scatter(world,
      Util
        .genShells(world, id, Stream.generate(CalebBug::new).limit(1),
          baseImpls)
        .map(s -> s.tag(CashGrab.Tags.BANK,
          new Pair<>(bank, bankId.getAndUpdate(n -> n + 1)))));
    Util.scatter(world,
      players.build().filter(a -> a instanceof Shell).map(a -> (Shell) a)
        .filter(s -> s.getBrain() instanceof CalebBug)
        .map(s -> s.tag(Shell.Tags.PUSHABLE, true)));
    final AtomicReference<Boolean> isFemale = new AtomicReference<>(true);
    Util.scatter(world, Stream.generate(() -> {
      final boolean female = isFemale.getAndUpdate(b -> !b);
      return Util.genShell(world, id, new HunterCritter(female))
        .addAllImpls(baseImpls).tag(CashGrab.Tags.IS_FEMALE.getTag(), female)
        .tag(CashGrab.Tags.PREDATOR.getTag(), true)
        .tag(Shell.Tags.PUSHABLE.getTag(), true);
    }).limit(0));
    world.show();
  }
}
