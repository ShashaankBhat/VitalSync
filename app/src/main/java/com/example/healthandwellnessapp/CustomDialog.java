package com.example.healthandwellnessapp;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class CustomDialog {

    private final Dialog dialog;

    public CustomDialog(Context context, String message) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);

        TextView dialogMessage = dialog.findViewById(R.id.dialog_message);
        dialogMessage.setText(message);

        Button closeButton = dialog.findViewById(R.id.dialog_close_button);
        closeButton.setOnClickListener(view -> dialog.dismiss()); // Dismiss the dialog when the button is clicked
    }

    public void show() {
        dialog.show();
    }

    public void setCloseButtonClickListener(View.OnClickListener listener) {
        Button closeButton = dialog.findViewById(R.id.dialog_close_button);
        closeButton.setOnClickListener(view -> {
            listener.onClick(view);
            dialog.dismiss();
        });
    }
}
