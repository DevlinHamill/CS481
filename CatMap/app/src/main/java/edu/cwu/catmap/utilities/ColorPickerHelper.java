package edu.cwu.catmap.utilities;

import android.content.Context;
import android.content.DialogInterface;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

public class ColorPickerHelper {
    public static void getColor(Context context, ColorPickerTraceback traceback) {
        ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose a color")
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(10)
                .noSliders()
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                        traceback.selectColor(selectedColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        traceback.cancel();
                    }
                })
                .setPositiveButton("confirm", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        traceback.confirm(lastSelectedColor);
                    }
                })
                .build()
                .show();
    }
}
