package com.example.snapsomething;

import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SnapAdapter extends ArrayAdapter<Snap> {
	private Context context;
	private List<Snap> objects;
	private DisplayImageOptions options;
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	public SnapAdapter(Context context, List<Snap> objects) {
		super(context, R.layout.listview_snap_item, objects);
		this.objects = objects;
		this.context = context;
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.placeholder)
				.showImageForEmptyUri(R.drawable.placeholder)
				.showImageOnFail(R.drawable.placeholder).cacheInMemory()
				.cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.listview_snap_item, null);
		}
		if (objects != null) {
			Snap snap = objects.get(position);
			ImageView snap_item_profile_image = (ImageView) view
					.findViewById(R.id.snap_item_profile_image);
			if (snap.getCreator().getPhoto() != null) {
				imageLoader.displayImage(snap.getCreator().getPhoto()
						.getS3Url(), snap_item_profile_image, options);
			} else {
				Bitmap bitmap = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.default_avatar);
				snap_item_profile_image.setImageBitmap(bitmap);
			}
			TextView user_name = (TextView) view
					.findViewById(R.id.snap_item_username);
			user_name.setText(snap.getCreator().getUsername());
			ImageView snap_item_image = (ImageView) view
					.findViewById(R.id.snap_item_image);
			imageLoader.displayImage(snap.getPhoto().getS3Url(),
					snap_item_image, options);
		}
		return view;
	}
}