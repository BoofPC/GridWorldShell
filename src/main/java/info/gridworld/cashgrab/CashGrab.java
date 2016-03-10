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

    public int getBalance(int id) {
      return balances.getOrDefault(id, 0);
    }

    public Bank setBalance(int id, int balance) {
      balances.put(id, balance);
      return this;
    }

    public Bank bank(Shell shell) {
      shell.tag(CashGrab.Tags.BANK, this);
      return this;
    }

    public Bank transfer(Bank destBank, int src, int dest, int amount) {
      final int srcBalance = this.getBalance(src);
      final int destBalance = destBank.getBalance(dest);
      this.setBalance(src, srcBalance - amount);
      destBank.setBalance(dest, destBalance + amount);
      System.out.println("transfer " + amount + " from " + src + " to " + dest);
      return this;
    }

    public Bank transfer(int src, int dest, int amount) {
      return this.transfer(this, src, dest, amount);
    }
  }

  public Coin genCoin(AtomicReference<Integer> id, Bank bank) {
    return new Coin(id.getAndUpdate(x -> x + 1), bank);
  }

  public Coin genCoin(AtomicReference<Integer> id, Bank bank, int initBalance) {
    return new Coin(id.getAndUpdate(x -> x + 1), bank, initBalance);
  }

  public Stream.Builder<Coin> addCoins(Stream.Builder<Coin> coins,
    AtomicReference<Integer> id, Stream<Bank> banks) {
    banks.forEach(bank -> coins.add(genCoin(id, bank)));
    return coins;
  }
}
