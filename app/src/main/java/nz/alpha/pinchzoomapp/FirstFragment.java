package nz.alpha.pinchzoomapp;

import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import java.util.Date;
import java.util.Objects;

import nz.alpha.pinchzoom.PinchZoom;
import nz.alpha.pinchzoomapp.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

	private FragmentFirstBinding binding;

	@Override
	public View onCreateView(
			LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState
	) {

		binding = FragmentFirstBinding.inflate(inflater, container, false);
		return binding.getRoot();

	}

	private ScrollImageView imageView;

	private PinchZoom pz;

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Drawable d = ResourcesCompat.getDrawable(getResources(), R.drawable.lighthouse, null);
		Objects.requireNonNull(d);

		imageView = view.findViewById(R.id.scrollimageview);
		pz = new PinchZoom(2.0f, 2.5f, imageView, d);

		Button btn = view.findViewById(R.id.button);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Drawable d = ResourcesCompat.getDrawable(getResources(), R.drawable.lighthouse2, null);
				pz.changeImage(d);
			}
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

}