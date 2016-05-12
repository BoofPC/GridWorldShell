package info.gridworld.cashgrab;

import java.util.function.BiConsumer;

import info.gridworld.actor.Action;
import info.gridworld.actor.Actor;
import info.gridworld.actor.Shell;
import info.gridworld.actor.Util;
import info.gridworld.actor.Util.Pairs;
import info.gridworld.cashgrab.CashGrab.Bank;
import info.gridworld.grid.Grid;
import info.gridworld.grid.Location;
import javafx.util.Pair;
import lombok.Data;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Actions {
  /**
   * The {@code CollectCoinAction} class represents collection of a nearby coin in the world.
   */
  @Data
  public class CollectCoinAction implements Action {
    /**
     * The distance away of the coin.
     */
    private final double distance;
    /**
     * The direction of the coin from the actor's viewpoint.
     */
    private final double direction;

    @Override
    public boolean isFinal() {
      return true;
    }

    public static BiConsumer<Shell, Action> impl(final int maxDist, final int maxMine) {
      return (final Shell that, final Action a) -> {
        @SuppressWarnings("unchecked")
        final Pair<Bank, Integer> bank_ = (Pair<Bank, Integer>) that.getTag(CashGrab.Tags.BANK);
        if (bank_ == null || bank_.getKey() == null || bank_.getValue() == null) {
          System.out.println("null bank of " + that.getId());
          return;
        }
        final CollectCoinAction cca = (CollectCoinAction) a;
        final double distance = Math.min(cca.getDistance(), maxDist);
        final double direction = cca.getDirection();
        System.out.println(that.getId() + " at " + that.getLocation()
            + " attempting collection of distance " + distance + " direction " + that.getDirection()
            + " target direction " + direction);
        final Pair<Double, Double> loc = Util.locToRect(that.getLocation());
        final Grid<Actor> grid = that.getGrid();
        final Pair<Double, Double> offset = Util.polarToRect(distance,
            Util.polarRight(Math.toRadians(that.getDirection() + direction)));
        final Location targetLoc = Util.rectToLoc(Pairs.thread(loc, offset, (x, y) -> x + y));
        System.out.println("targeting " + targetLoc + " offset " + offset);
        final Actor target_ = grid.get(Util.sanitize(targetLoc, grid));
        if (target_ == null) {
          System.out.println("null target coin");
          return;
        }
        Pair<Bank, Integer> targetBank_ = null;
        if (target_ instanceof Shell) {
          final Shell target = (Shell) target_;
          if (!(boolean) target.getTagOrDefault(CashGrab.Tags.MINABLE, false)) {
            System.out.println("non-minable target");
            return;
          }
          @SuppressWarnings({"unchecked"})
          final Pair<Bank, Integer> targetBank__ =
              ((Pair<Bank, Integer>) target.getTagOrDefault(CashGrab.Tags.BANK, null));
          targetBank_ = targetBank__;
        } else if (target_ instanceof Coin) {
          final Coin target = (Coin) target_;
          targetBank_ = Pairs.liftNull(target.getBank(), target.getId());
        } else {
          System.out.println("unknown target");
          return;
        }
        if (targetBank_ == null) {
          System.out.println("null target bank");
          return;
        }
        final Bank bank = bank_.getKey();
        final int id = bank_.getValue();
        final Bank targetBank = targetBank_.getKey();
        final int targetId = targetBank_.getValue();
        System.out.println("transfer attempt " + id + " to " + targetId);
        if (targetBank.getBalance(targetId) > 0) {
          bank.transfer(targetBank, targetId, id, maxMine);
          System.out.println("transfer " + id + " to " + targetId + " complete");
        }
      };
    }
  }
  /**
   * The {@code ConsumeAction} class represents consumption of another actor in the world.
   */
  @Data
  public class ConsumeAction implements Action {
    /**
     * The distance away of the prey.
     */
    private final double distance;
    /**
     * The direction of the prey from the actor's viewpoint.
     */
    private final double direction;

    @Override
    public boolean isFinal() {
      return true;
    }

    public static BiConsumer<Shell, Action> impl(final int maxDist) {
      return (final Shell that, final Action a) -> {
        if (!((boolean) that.getTagOrDefault(CashGrab.Tags.PREDATOR, false))) {
          System.out.println(that.getId() + " not a pred");
          return;
        }
        final Location loc = that.getLocation();
        final ConsumeAction ca = ((ConsumeAction) a);
        final Grid<Actor> grid = that.getGrid();
        final Pair<Double, Double> offsets = Util.polarToRect(ca.getDistance(),
            Util.polarRight(Math.toRadians(that.getDirection() + ca.getDirection())));
        final Location offsetLoc = Util.rectToLoc(offsets);
        final Location preyLoc = new Location((int) (loc.getRow() + offsetLoc.getRow()),
            (int) (loc.getCol() + offsetLoc.getCol()));
        System.out.print(that.getId() + " going for " + preyLoc + ", ");
        final Location cleanPreyLoc = Util.sanitize(preyLoc, grid);
        System.out.print("got " + cleanPreyLoc + ", ");
        final Actor prey = grid.get(cleanPreyLoc);
        if (prey == null) {
          System.out.println("nothing to eat");
          return;
        }
        if (prey == that) {
          System.out.println("can't eat itself");
          return;
        }
        if (prey instanceof Coin) {
          System.out.println("can't eat coin");
          return;
        }
        System.out.println("om nom nom for " + that.getId() + " at " + preyLoc);
        prey.removeSelfFromGrid();
      };
    }
  }
}
