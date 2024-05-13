package com.app.speechtotext;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText mtlTxtResult;
    private static final int CREATE_FILE_REQUEST_CODE = 1001;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1002;
    private boolean isListening = false; // Biến theo dõi trạng thái "Listening"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ dữ liệu vào biến đã khởi tạo (bắt buộc)
        mtlTxtResult = findViewById(R.id.mtlTxtResult);
    }

    // Clear Result
    public void clearResult(View view) {
        mtlTxtResult.setText("");
    }

    // Export Result
    public void exportResult(View view) {
        String data = mtlTxtResult.getText().toString();
        if (!data.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, "exported_data.txt");
            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
        }
    }

    // Speech to text
    public void toggleListening(View view) {
        if (!isListening) { // If not listening
            isListening = true;

            promptSpeechInput();
        } else { // If listening
            isListening = false;
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...");

        // Check if speech recognition is supported on device
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } else {
            Toast.makeText(this, "Speech recognition is not supported on your device", Toast.LENGTH_SHORT).show();
        }
    }

    // Override
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Export Data
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                try {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(mtlTxtResult.getText().toString().getBytes());
                        outputStream.close();
                        Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Speak to text
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result.size() > 0) {
                    mtlTxtResult.setText(result.get(0)); // Set the first result (most likely option)
                }
            }
        }
    }
}