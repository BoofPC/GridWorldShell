package info.gridworld.cashgrab;

import info.gridworld.actor.Rock;
import info.gridworld.cashgrab.CashGrab.Bank;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Coin extends Rock {
  private final int id;
  private final @NonNull Bank bank;

  public Coin(final int id, final Bank bank, final int initBalance) {
    this(id, bank);
    this.bank.setBalance(id, initBalance);
  }

  @Override
  public void act() {
    super.act();
    if (bank.getBalance(id) == 0) {
      this.removeSelfFromGrid();
    }
  }
}
