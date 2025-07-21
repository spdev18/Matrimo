package com.techotd.matrimo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.techotd.matrimo.utils.UserData;
import com.techotd.matrimo.utils.PdfGenerator;

public class PreviewActivity extends AppCompatActivity {
    private static final String TAG = "PreviewActivity";
    private UserData data;
    private int templateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        // Back button to finish the activity
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        templateId = getIntent().getIntExtra("template_id", 0); // Default to 0 if not provided
        if (templateId == 0) {
            Log.e(TAG, "Template ID not provided");
            Toast.makeText(this, "Error: No template selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FrameLayout container = findViewById(R.id.template_container);
        getLayoutInflater().inflate(templateId, container, true);

        data = getIntent().getParcelableExtra("user_data");
        if (data == null) {
            Log.e(TAG, "UserData is null");
            Toast.makeText(this, "Error: No user data received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "UserData: name=" + data.name + ", age=" + data.age + ", photoPath=" + data.photoPath);

        initializeViews();
        populateTemplate();
        setupButtons();
    }

    private void initializeViews() {
        ImageView photo = findViewById(R.id.preview_photo);
        if (photo != null && data.photoPath != null) {
            // Apply corner radius to match the border
            RequestOptions requestOptions = new RequestOptions()
                    .transform(new RoundedCorners(8));

            Glide.with(this)
                    .load(data.photoPath)
                    .apply(requestOptions)
                    .into(photo);

            Log.d(TAG, "Photo loaded: " + data.photoPath);
        } else {
            Log.w(TAG, "Photo path is null or photo view not found");
        }
    }

    private void populateTemplate() {
        setTextWithDefault(R.id.preview_name, data.name);
        setTextWithDefault(R.id.preview_age, data.age);
        setTextWithDefault(R.id.preview_gender, data.gender);
        setTextWithDefault(R.id.preview_religion, data.religion);
        setTextWithDefault(R.id.preview_caste, data.caste);
        setTextWithDefault(R.id.preview_height, data.height);
        setTextWithDefault(R.id.preview_weight, data.weight);
        setTextWithDefault(R.id.preview_profession, data.profession);
        setTextWithDefault(R.id.preview_qualification, data.qualification);
        setTextWithDefault(R.id.preview_father_name, data.fatherName);
        setTextWithDefault(R.id.preview_father_occupation, data.fatherOccupation);
        setTextWithDefault(R.id.preview_mother_name, data.motherName);
        setTextWithDefault(R.id.preview_mother_occupation, data.motherOccupation);
        setTextWithDefault(R.id.preview_brothers, data.brothers);
        setTextWithDefault(R.id.preview_sisters, data.sisters);
        setTextWithDefault(R.id.preview_contact, data.contact);
        setTextWithDefault(R.id.preview_address, data.address);
        setTextWithDefault(R.id.preview_hobbies, data.hobbies);
        setTextWithDefault(R.id.preview_additional_info, data.additionalInfo);
    }

    private void setTextWithDefault(int viewId, String value) {
        TextView textView = findViewById(viewId);
        if (textView != null) {
            textView.setText(value != null && !value.isEmpty() ? value : "N/A");
        }
    }

    private void setupButtons() {

        // Generate PDF
        Button generatePdf = findViewById(R.id.generate_pdf);
        generatePdf.setOnClickListener(v -> {
            try {
                String pdfPath = PdfGenerator.generatePdf(this, data, templateId);
                if (pdfPath != null) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/pdf");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(pdfPath));
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.view_or_share)));
                } else {
                    Log.e(TAG, "PDF path is null");
                    Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error generating PDF", e);
                Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Generate Image
        Button generateImage = findViewById(R.id.generate_image);
        generateImage.setOnClickListener(v -> {
            try {
                FrameLayout container = findViewById(R.id.template_container);
                View templateView = container.getChildAt(0);
                if (templateView == null) {
                    Toast.makeText(this, "Template not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Measure and layout the view at its full height (like PDF)
                int pageWidth = 595;
                templateView.measure(
                        View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );
                int contentHeight = templateView.getMeasuredHeight();
                templateView.layout(0, 0, pageWidth, contentHeight);

                // Create bitmap of the full view
                Bitmap bitmap = Bitmap.createBitmap(pageWidth, contentHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                templateView.draw(canvas);

                // Save bitmap as PNG in Downloads/Matrimo
                String safeName = (data.name != null && !data.name.trim().isEmpty())
                        ? data.name.trim().replaceAll("[^a-zA-Z0-9]", "_")
                        : "Biodata";
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Matrimo");
                if (!directory.exists()) directory.mkdirs();
                File file = new File(directory, safeName + "_" + System.currentTimeMillis() + ".png");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }

                Toast.makeText(this, "Image saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

                // Share using FileProvider
                Uri uri = FileProvider.getUriForFile(this, "com.techotd.matrimo.fileprovider", file);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share Image"));

            } catch (Exception e) {
                Log.e(TAG, "Error generating image", e);
                Toast.makeText(this, "Error generating image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}