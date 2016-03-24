package info.gridworld.cashgrab;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import info.gridworld.actor.Shell;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CashGrab {
  @Getter
  @RequiredArgsConstructor
  public enum Tags implements info.gridworld.actor.Tag {
    BANK("CashGrab.Bank"), MINABLE("CashGrab.Minable"), IS_FEMALE(
      "CashGrab.IsFemale"), PREDATOR("CashGrab.Predator");
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
}
