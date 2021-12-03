package nz.alpha.pinchzoomapp;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScrollImageView extends androidx.appcompat.widget.AppCompatImageView{
	public ScrollImageView(@NonNull Context context) {
		super(context);
	}

	public ScrollImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
}
