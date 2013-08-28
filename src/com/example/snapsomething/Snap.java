package com.example.snapsomething;
import com.stackmob.sdk.api.StackMobFile;
import com.stackmob.sdk.api.StackMobGeoPoint;
import com.stackmob.sdk.model.StackMobModel;
public class Snap extends StackMobModel {
	private User creator;
	private StackMobGeoPoint location;
	private StackMobFile photo;
	public Snap(User creator, StackMobGeoPoint location) {
		super(Snap.class);
		this.creator = creator;
		this.location = location;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
	}
	public StackMobGeoPoint getLocation() {
		return location;
	}
	public void setLocation(StackMobGeoPoint location) {
		this.location = location;
	}
	public void setPhoto(StackMobFile photo) {
		this.photo = photo;
	}
	public StackMobFile getPhoto() {
		return photo;
	}
}