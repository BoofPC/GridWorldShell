import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actions.ColorAction;
import info.gridworld.actor.Actions.MessageAction;
import info.gridworld.actor.Actions.MoveAction;
import info.gridworld.actor.Actions.TurnAction;
import info.gridworld.actor.Actor;
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
    // Set up CashGrab world
    final Grid<Actor> grid = new BoundedGrid<>(50, 50);
    final ShellWorld world =
        new ShellWorld(grid, Arrays.asList(ShellWorld::clearMessage, CashGrab::coinMessage));
    // Set up World ReportEvent behaviors
    world.getWatchman().addImpl(CollisionReportEvent.class, CollisionReportEvent.impl())
        .addImpl(MessageReportEvent.class, MessageReportEvent.impl());

    // Set up Action behaviors
    final Map<Class<? extends Action>, BiConsumer<Shell, Action>> baseImpls = new HashMap<>();
    baseImpls.put(MessageAction.class, MessageAction.impl(5));
    baseImpls.put(MoveAction.class, MoveAction.impl(1));
    baseImpls.put(TurnAction.class, TurnAction.impl());
    baseImpls.put(ColorAction.class, ColorAction.impl());
    baseImpls.put(CollectCoinAction.class, CollectCoinAction.impl(2, 1));
    baseImpls.put(ConsumeAction.class, ConsumeAction.impl(1));

    // Prepare world ids
    final AtomicReference<Integer> id = new AtomicReference<>(0);

    // Prepare bank and generate coins
    final AtomicReference<Integer> bankId = new AtomicReference<>(0);
    final CashGrab.Bank bank = new CashGrab.Bank();
    Util.scatter(world, Stream.generate(() -> CashGrab.genCoin(bankId, bank, 5)).limit(84));

    // Generate player bugs.
    final Collection<Function<Shell, Shell>> playerModifiers =
        Arrays.asList(CashGrabRunner.tagBank(bank, bankId), CashGrabRunner.tagScore());
    // You probably want to just replace CalebBug with your brain's name,
    //   and 15 with however many you want (probably 1).
    Util.scatterShells(world, baseImpls, id, Stream.generate(CalebBug::new).limit(15),
        playerModifiers.stream());

    // Generate HunterCritters
    final AtomicReference<Boolean> isFemale = new AtomicReference<>(true);
    Util.scatterShells(world, baseImpls, id,
        Stream.generate(() -> new HunterCritter(isFemale.getAndUpdate(b -> !b))).limit(15),
        Stream.of(CashGrabRunner.tagPredator(), CashGrabRunner.tagPushable(),
            CashGrabRunner.tagFemale()));

    world.show();
  }

  public static Function<Shell, Shell> tagFemale() {
    return s -> s.tag(CashGrab.Tags.IS_FEMALE, ((HunterCritter) s.getBrain()).isFemale());
  }

  public static Function<Shell, Shell> tagPushable() {
    return s -> s.tag(Shell.Tags.PUSHABLE, true);
  }

  public static Function<Shell, Shell> tagPredator() {
    return s -> s.tag(CashGrab.Tags.PREDATOR, true);
  }

  public static Function<Shell, Shell> tagScore() {
    return s -> s.tag(CashGrab.Tags.SHOW_SCORE, true);
  }

  public static Function<Shell, Shell> tagBank(final CashGrab.Bank bank,
      final AtomicReference<Integer> bankId) {
    return s -> s.tag(CashGrab.Tags.BANK, new Pair<>(bank, bankId.getAndUpdate(n -> n + 1)));
  }
}
