package com.example.snapsomething;

import com.stackmob.sdk.model.StackMobModel;

public class Comment extends StackMobModel {
	private User creator;
	private String text;
	private Snap snap;

	public Comment(User creator, String text, Snap snap) {
		super(Comment.class);
		this.creator = creator;
		this.text = text;
		this.snap = snap;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Snap getSnap() {
		return snap;
	}

	public void setSnap(Snap snap) {
		this.snap = snap;
	}
}