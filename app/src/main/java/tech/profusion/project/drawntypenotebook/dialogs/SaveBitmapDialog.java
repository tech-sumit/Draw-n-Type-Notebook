package tech.profusion.project.drawntypenotebook.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import tech.profusion.project.drawntypenotebook.R;

public class SaveBitmapDialog extends DialogFragment {

    private OnSaveBitmapListener onSaveBitmapListener;

    // VARS
    private Bitmap mPreviewBitmap;
    private String mPreviewFormat;

    public SaveBitmapDialog(){}

    public static SaveBitmapDialog newInstance(){
        return new SaveBitmapDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_save_bitmap, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_capture_preview);
        final TextInputEditText textInputEditText = (TextInputEditText) view.findViewById(R.id.et_file_name);

        final File filePath = Environment.getExternalStorageDirectory();
        final String[] fileName = {"DrawNTypeNote." + mPreviewFormat.toLowerCase()};

        if (mPreviewBitmap != null)
            imageView.setImageBitmap(mPreviewBitmap);
        else
            imageView.setImageResource(R.color.colorAccent);
        textInputEditText.setText(fileName[0]);

        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable editable) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fileName[0] = charSequence.toString();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if (!fileName[0].contains("."))
                                fileName[0] = fileName[0] + "." + mPreviewFormat.toLowerCase();
                            textInputEditText.setText(fileName[0]);

                            File image = new File(filePath + File.separator + fileName[0]);
                            image.createNewFile();

                            FileOutputStream fileOutputStream = new FileOutputStream(image);
                            mPreviewBitmap.compress(
                                    mPreviewFormat.toLowerCase().equals("jpg") ?
                                            Bitmap.CompressFormat.JPEG :
                                            Bitmap.CompressFormat.PNG, 100, fileOutputStream);

                            if (onSaveBitmapListener != null)
                                onSaveBitmapListener.onSaveBitmapCompleted(image);
                            Log.i("PATH",""+image.getPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (onSaveBitmapListener != null)
                            onSaveBitmapListener.onSaveBitmapCanceled();
                        dismiss();
                    }
                });

        return builder.create();
    }

    // METHODS
    public void setPreviewBitmap(Bitmap bitmap){
        this.mPreviewBitmap = bitmap;
    }

    public void setPreviewFormat(String previewFormat){
        this.mPreviewFormat = previewFormat;
    }

    // LISTENER
    public void setOnSaveBitmapListener(OnSaveBitmapListener onSaveBitmapListener){
        this.onSaveBitmapListener = onSaveBitmapListener;
    }

    public interface OnSaveBitmapListener{
        void onSaveBitmapCompleted(File file);
        void onSaveBitmapCanceled();
    }
}