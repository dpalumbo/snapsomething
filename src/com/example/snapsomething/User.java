package com.example.snapsomething;
import com.stackmob.sdk.api.StackMobFile;
import com.stackmob.sdk.model.StackMobUser;

public class User extends StackMobUser {
	private String email;
	private StackMobFile photo;
	public User(String username, String password, String email) {
		super(User.class, username, password);
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public StackMobFile getPhoto() {
		return photo;
	}
	public void setPhoto(StackMobFile photo) {
		this.photo = photo;
	}
}