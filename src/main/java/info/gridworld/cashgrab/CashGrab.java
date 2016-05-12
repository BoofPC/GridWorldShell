package info.gridworld.cashgrab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import info.gridworld.actor.Actor;
import info.gridworld.actor.Shell;
import info.gridworld.actor.ShellWorld;
import javafx.util.Pair;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CashGrab {
  @Getter
  @RequiredArgsConstructor
  public enum Tags implements info.gridworld.actor.Tag {
    BANK("CashGrab.Bank"), MINABLE("CashGrab.Minable"), IS_FEMALE("CashGrab.IsFemale"), PREDATOR(
        "CashGrab.Predator"), SHOW_SCORE("CashGrab.ShowScore");
    private final String tag;
  }
  @Data
  public class Bank {
    private final Map<Integer, Integer> balances = new HashMap<>();

    public int getBalance(final int id) {
      return this.balances.getOrDefault(id, 0);
    }

    public Bank setBalance(final int id, final int balance) {
      this.balances.put(id, balance);
      return this;
    }

    public Bank bank(final Shell shell) {
      shell.tag(CashGrab.Tags.BANK, this);
      return this;
    }

    public Bank transfer(final Bank destBank, final int src, final int dest, final int amount) {
      final int srcBalance = this.getBalance(src);
      final int destBalance = destBank.getBalance(dest);
      this.setBalance(src, srcBalance - amount);
      destBank.setBalance(dest, destBalance + amount);
      System.out.println("transfer " + amount + " from " + src + " to " + dest);
      return this;
    }

    public Bank transfer(final int src, final int dest, final int amount) {
      return this.transfer(this, src, dest, amount);
    }
  }

  public Coin genCoin(final AtomicReference<Integer> id, final Bank bank) {
    return new Coin(id.getAndUpdate(x -> x + 1), bank);
  }

  public Coin genCoin(final AtomicReference<Integer> id, final Bank bank, final int initBalance) {
    return new Coin(id.getAndUpdate(x -> x + 1), bank, initBalance);
  }

  public Stream.Builder<Coin> addCoins(final Stream.Builder<Coin> coins,
      final AtomicReference<Integer> id, final Stream<Bank> banks) {
    banks.forEach(bank -> coins.add(CashGrab.genCoin(id, bank)));
    return coins;
  }

  public static void coinMessage(final ShellWorld that,
      final Pair<List<Actor>, AtomicReference<Object>> data) {
    final List<Actor> actors = data.getKey();
    Bank masterBank = null;
    @SuppressWarnings({"unchecked", "rawtypes"})
    final AtomicReference<HashMap<Integer, Pair<String, Integer>>> balances_ =
        (AtomicReference) data.getValue();
    balances_.compareAndSet(null, new HashMap<>());
    final HashMap<Integer, Pair<String, Integer>> balances = balances_.get();
    for (final Actor actor_ : actors) {
      if (!(actor_ instanceof Shell)) {
        continue;
      }
      final Shell actor = (Shell) actor_;
      if (!(boolean) actor.getTagOrDefault(CashGrab.Tags.SHOW_SCORE, false)) {
        continue;
      }
      @SuppressWarnings("unchecked")
      final Pair<Bank, Integer> bank_ = (Pair<Bank, Integer>) actor.getTag(CashGrab.Tags.BANK);
      if (bank_ == null) {
        continue;
      }
      final Bank bank = bank_.getKey();
      if (masterBank == null) {
        masterBank = bank;
      }
      final Integer id_ = bank_.getValue();
      if (id_ == null) {
        continue;
      }
      balances.compute(id_, (id, identOld) -> {
        String name = null;
        if (identOld == null) {
          name = actor.getBrain().getClass().getSimpleName();
        } else {
          name = identOld.getKey();
        }
        final int balance = bank.getBalance(id);
        return new Pair<>(name, balance);
      });
    }
    final StringBuilder msg = new StringBuilder(that.getMessage());
    msg.append("Current standings:");
    balances.forEach((id, info) -> {
      final String name = info.getKey();
      final Integer balance = info.getValue();
      msg.append(" (");
      msg.append(name);
      msg.append('[');
      msg.append(id.toString());
      msg.append("] ");
      msg.append(balance.toString());
      msg.append(')');
    });
    msg.append('\n');
    that.setMessage(msg.toString());
  }
}
