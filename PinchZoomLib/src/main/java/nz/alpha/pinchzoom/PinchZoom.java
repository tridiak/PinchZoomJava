package nz.alpha.pinchzoom;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.Objects;

public class PinchZoom {
	private static final float MIN_SF = 0.01f;
	private static final float MAX_SF = 20f;
	private float minSF;
	private float maxSF;
	private final AppCompatImageView imageView;

	private float prevX;
	private float prevY;
	private int imageWidth;
	private int imageHeight;
	private float currSF;
	private float origDistance = 0;
	private float prevSF;
	private boolean pinchZoom = false;
	private float scrollXPercent = 0;
	private float scrollYPercent = 0;

	@SuppressLint("ClickableViewAccessibility")
	public PinchZoom(float minSF, float maxSF, @NonNull AppCompatImageView imageView, @NonNull Drawable image) throws IllegalArgumentException {
		if (imageView.getScaleType() != ImageView.ScaleType.MATRIX) {
			throw new IllegalArgumentException("Imageview must be of scale type matrix");
		}

		if (minSF < MIN_SF || maxSF <= minSF || maxSF > MAX_SF) {
			throw new IllegalArgumentException("Bad scale factors");
		}
		this.minSF = minSF;
		this.maxSF = maxSF;
		this.imageView = imageView;
		currSF = Math.max(minSF, 1.0f);
		prevSF = currSF;

		Matrix m = imageView.getImageMatrix();
		m.setScale(currSF, currSF);
		imageView.setImageMatrix(m);

		imageWidth = image.getIntrinsicWidth();
		imageHeight = image.getIntrinsicHeight();
		imageView.setImageDrawable(image);

		imageView.setOnTouchListener((view, motionEvent) -> {

			switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					imageTouchDown(motionEvent);
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					imageExtraTouch(motionEvent);
					break;
				case MotionEvent.ACTION_MOVE:
					imageDrag(motionEvent);
					break;
				case MotionEvent.ACTION_UP:
					imageTouchUp();
					break;
			}

			return true;
		});
	}

	public void changeImage(@NonNull Drawable image) {
		Objects.requireNonNull(image, "Drawable must be valid");
		imageWidth = image.getIntrinsicWidth();
		imageHeight = image.getIntrinsicHeight();
		currSF = Math.max(minSF, 1.0f);
		origDistance = 0;
		prevSF = currSF;
		scrollXPercent = 0;
		scrollYPercent = 0;
		imageView.setImageMatrix(new Matrix());
		imageView.setImageDrawable(image);
		imageView.scrollTo(0,0);
	}

	private void imageTouchDown(MotionEvent event) {
		prevX = event.getX();
		prevY = event.getY();
	}

	private void imageExtraTouch(MotionEvent event) {
		int idx = event.getActionIndex();
		float x = event.getX(idx);
		float y = event.getY(idx);

		int scrollX = imageView.getScrollX();
		int scrollY = imageView.getScrollY();
		int viewWidth = imageView.getWidth();
		int viewHeight = imageView.getHeight();
		int centreX = scrollX + viewWidth / 2;
		int centreY = scrollY + viewHeight / 2;
		scrollXPercent = (float)centreX / (float) scaledImageWidth();
		scrollYPercent = (float)centreY / (float) scaledImageHeight();

		pinchZoom = true;
		origDistance = (float) Math.sqrt(Math.pow(prevX - x, 2) + Math.pow(prevY - y, 2));
	}

	private float scaledImageWidth() {
		return imageWidth * currSF;
	}

	private float scaledImageHeight() {
		return imageHeight * currSF;
	}

	private void pinchZoom(MotionEvent event) {
		int ct = event.getPointerCount();
		if (ct < 2) { return; }

		float x1 = event.getX(0);
		float y1 = event.getY(0);
		float x2 = event.getX(1);
		float y2 = event.getY(1);

		float d = (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		Matrix SFMatrix = imageView.getImageMatrix();
		float sf = d / origDistance * prevSF;
		// Min. and max SF
		if (sf < minSF || sf > maxSF) {
			return;
		}

		currSF = sf;
		SFMatrix.setScale(sf, sf);
		imageView.setImageMatrix(SFMatrix);

		int sx = imageView.getScrollX();
		int sy = imageView.getScrollY();
		// Pinch-Zoom-Keep-Center

		int viewWidth = imageView.getWidth();
		int centreX = (int) (scaledImageWidth() * scrollXPercent);
		int newScrollX = centreX - viewWidth / 2;

		int viewHeight = imageView.getHeight();
		int centreY = (int) (scaledImageHeight() * scrollYPercent);
		int newScrollY = centreY - viewHeight / 2;

		// Keep in bounds
		int farX = (int) (-sx + scaledImageWidth());
		int dx = farX - imageView.getWidth();
		if (dx > 0) {
			dx = 0;
		}

		int farY = (int) (scaledImageHeight() - sy);
		int dy = farY - imageView.getHeight();
		if (dy > 0) {
			dy = 0;
		}

		imageView.scrollTo(newScrollX + dx, newScrollY + dy);
		// Not sure why, but another scroll delta is required.
		dx = (int) (-imageView.getScrollX() + scaledImageWidth() - imageView.getWidth());
		dy = (int) (-imageView.getScrollY() + scaledImageHeight() - imageView.getHeight());
		if (dx > 0) {
			dx = 0;
		}
		if (dy > 0) {
			dy = 0;
		}
		imageView.scrollBy(dx, dy);

		// Keep image top-left if it shrinks enough
		sx = imageView.getScrollX();
		sy = imageView.getScrollY();
		if ((sx < 0 && sy < 0) || (scaledImageHeight() < imageView.getHeight() && scaledImageWidth() < imageView.getWidth()) ) {
			imageView.scrollTo(0, 0);
		}
		else if (sy < 0 || scaledImageHeight() < imageView.getHeight()) {
			imageView.scrollTo(sx, 0);
		}
		else if (sx < 0 || scaledImageWidth() < imageView.getWidth()) {
			imageView.scrollTo(0, sy);
		}
	}

	private void imageDrag(MotionEvent event) {
		if (pinchZoom) {
			pinchZoom(event);
			return;
		}

		float x = event.getX();
		float y = event.getY();
		int scrollX = imageView.getScrollX();
		int scrollY = imageView.getScrollY();
		int dx = (int) -(x - prevX);
		int dy = (int) -(y - prevY);
		int mapViewWidth = imageView.getWidth();
		int mapViewHeight = imageView.getHeight();

		// Keep within bounds
		if (scaledImageWidth() < mapViewWidth) {
			dx = -scrollX;
		}
		else if (dx < 0) {
			if (dx + scrollX < 0) { dx = -scrollX; }
		} else if (dx > 0) {
			if (dx + scrollX > (scaledImageWidth() - mapViewWidth)) {
				dx = (int) ((scaledImageWidth() - mapViewWidth) - scrollX);
			}
		}

		if (scaledImageHeight() < mapViewHeight) {
			dy = -scrollY;
		}
		else if (dy < 0) {
			if (dy + scrollY < 0) { dy = -scrollY; }
		} else if (dy > 0) {
			if (dy + scrollY > (scaledImageHeight() - mapViewHeight)) {
				dy = (int) ((scaledImageHeight() - mapViewHeight) - scrollY);
			}
		}

		imageView.scrollBy(dx, dy);
		prevX = x;
		prevY = y;
	}

	private void imageTouchUp() {
		origDistance = 0;
		pinchZoom = false;
		prevSF = currSF;
	}
}
