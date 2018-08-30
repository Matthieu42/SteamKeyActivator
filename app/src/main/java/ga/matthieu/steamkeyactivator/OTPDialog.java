package ga.matthieu.steamkeyactivator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class OTPDialog extends AppCompatDialogFragment {
    private EditText otpkey;
    private OTPDialogListener listener;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.otp_dialog,null);
        builder.setView(view)
                .setTitle("Steam Guard")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    listener.applyKey("");
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    String code = otpkey.getText().toString();
                    listener.applyKey(code);
                });
        otpkey = view.findViewById(R.id.otpkey);
        return builder.create();
    }

    public void addListener(OTPDialogListener listener){
        this.listener = listener;
    }
}
