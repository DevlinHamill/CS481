package edu.cwu.catmap.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.common.base.MoreObjects;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivityColorPickerTestBinding;
import edu.cwu.catmap.databinding.ActivityMainBinding;
import edu.cwu.catmap.utilities.ColorPickerHelper;
import edu.cwu.catmap.utilities.ColorPickerTraceback;
import edu.cwu.catmap.utilities.ToastHelper;

public class ColorPickerTest extends AppCompatActivity {
    ActivityColorPickerTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityColorPickerTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Context context = this;

        binding.colorPickerButton.setOnClickListener(view -> {
            ColorDrawable backgroundColor = (ColorDrawable) binding.main.getBackground();
            int backgroundColorInt = backgroundColor.getColor();

            ColorPickerHelper.getColor(this, new ColorPickerTraceback() {
                @Override
                public void selectColor(int color) {
                    ToastHelper.showToast(context, "selected color " + color);
                }

                @Override
                public void confirm(int color) {
                    binding.main.setBackgroundColor(color);
                }

                @Override
                public void cancel() {
                    ToastHelper.showToast(context, "Select color canceled");
                }
            });

            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("Choose a color")
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .density(10)
                    .noSliders()
                    .setOnColorSelectedListener(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int selectedColor) {
                            ToastHelper.showToast(context, "selected color " + selectedColor);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ToastHelper.showToast(context, "Select color canceled");
                        }
                    })
                    .setPositiveButton("confirm", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                            binding.main.setBackgroundColor(lastSelectedColor);
                        }
                    })
                    .build()
                    .show();
        });
    }
}