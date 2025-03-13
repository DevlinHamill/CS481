package edu.cwu.catmap.activities;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

public class BitmapUtils {
    public static Bitmap getBitmapFromDrawable(Context context, int resId) {
        Bitmap bitmap = null;
        Drawable drawable = ContextCompat.getDrawable(context, resId);  // Use ContextCompat for compatibility
        if (drawable != null) {
            bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }
}