package com.example.snapsomething;
import java.util.List;
import com.stackmob.sdk.model.StackMobModel;

public class Game extends StackMobModel {
	private User player1;
	private User player2;
	private List<Snap> snaps;
	
	public Game(User player1, User player2) {
		super(Game.class);
		this.player1 = player1;
		this.player2 = player2;
	}
	public User getPlayer1() {
		return player1;
	}
	public User getPlayer2() {
		return player2;
	}
	public void setPlayer1(User player1) {
		this.player1 = player1;
	}
	public void setPlayer2(User player2) {
		this.player2 = player2;
	}
}