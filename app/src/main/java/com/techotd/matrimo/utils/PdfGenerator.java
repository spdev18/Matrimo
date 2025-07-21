package com.techotd.matrimo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.techotd.matrimo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {
    private static final String TAG = "PdfGenerator";

    public static String generatePdf(Context context, UserData data, int templateId) {
        if (data == null) {
            Log.e(TAG, "UserData is null");
            return null;
        }

        // Create a temporary view to render the template
        View view = View.inflate(context, templateId, null);
        if (view == null) {
            Log.e(TAG, "Failed to inflate template layout: " + templateId);
            return null;
        }

        // Populate view with data
        ImageView photo = view.findViewById(R.id.preview_photo);
        TextView name = view.findViewById(R.id.preview_name);
        TextView age = view.findViewById(R.id.preview_age);
        TextView gender = view.findViewById(R.id.preview_gender);
        TextView religion = view.findViewById(R.id.preview_religion);
        TextView caste = view.findViewById(R.id.preview_caste);
        TextView height = view.findViewById(R.id.preview_height);
        TextView weight = view.findViewById(R.id.preview_weight);
        TextView profession = view.findViewById(R.id.preview_profession);
        TextView qualification = view.findViewById(R.id.preview_qualification);
        TextView fatherName = view.findViewById(R.id.preview_father_name);
        TextView fatherOccupation = view.findViewById(R.id.preview_father_occupation);
        TextView motherName = view.findViewById(R.id.preview_mother_name);
        TextView motherOccupation = view.findViewById(R.id.preview_mother_occupation);
        TextView brothers = view.findViewById(R.id.preview_brothers);
        TextView sisters = view.findViewById(R.id.preview_sisters);
        TextView contact = view.findViewById(R.id.preview_contact);
        TextView address = view.findViewById(R.id.preview_address);
        TextView hobbies = view.findViewById(R.id.preview_hobbies);
        TextView additionalInfo = view.findViewById(R.id.preview_additional_info);

        // Load photo (synchronously for PDF)
        if (data.photoPath != null && photo != null) {
            try {
                Bitmap bmp = Glide.with(context)
                        .asBitmap()
                        .load(data.photoPath)
                        .submit(200, 200)
                        .get();
                photo.setImageBitmap(bmp);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load photo for PDF", e);
            }
        }

        // Set text fields
        if (name != null) name.setText(data.name != null ? data.name : "N/A");
        if (age != null) age.setText (data.age != null ? data.age : "N/A");
        if (gender != null) gender.setText(data.gender != null ? data.gender : "N/A");
        if (religion != null) religion.setText(data.religion != null ? data.religion : "N/A");
        if (caste != null) caste.setText(data.caste != null ? data.caste : "N/A");
        if (height != null) height.setText (data.height != null ? data.height : "N/A");
        if (weight != null) weight.setText (data.weight != null ? data.weight : "N/A");
        if (profession != null) profession.setText(data.profession != null ? data.profession : "N/A");
        if (qualification != null) qualification.setText(data.qualification != null ? data.qualification : "N/A");
        if (fatherName != null) fatherName.setText(data.fatherName != null ? data.fatherName : "N/A");
        if (fatherOccupation != null) fatherOccupation.setText (data.fatherOccupation != null ? data.fatherOccupation : "N/A");
        if (motherName != null) motherName.setText(data.motherName != null ? data.motherName : "N/A");
        if (motherOccupation != null) motherOccupation.setText (data.motherOccupation != null ? data.motherOccupation : "N/A");
        if (brothers != null) brothers.setText (data.brothers != null ? data.brothers : "N/A");
        if (sisters != null) sisters.setText(data.sisters != null ? data.sisters : "N/A");
        if (contact != null) contact.setText (data.contact != null ? data.contact : "N/A");
        if (address != null) address.setText(data.address != null ? data.address : "N/A");
        if (hobbies != null) hobbies.setText(data.hobbies != null ? data.hobbies : "N/A");
        if (additionalInfo != null) additionalInfo.setText(data.additionalInfo != null ? data.additionalInfo : "N/A");

        // Set A4 size: 595 x 842 points (about 8.27 x 11.69 inches)
        int pageWidth = 595;
        int pageHeight = 842;

        // Measure the view at WRAP_CONTENT to get its natural height
        view.measure(
                View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int contentHeight = view.getMeasuredHeight();

        // Layout the view at its measured height
        view.layout(0, 0, pageWidth, contentHeight);

        // Create a bitmap of the view at its full height
        Bitmap bitmap = Bitmap.createBitmap(pageWidth, contentHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        // Now scale the bitmap to fit A4 size
        float scale = Math.min(
                (float) pageWidth / bitmap.getWidth(),
                (float) pageHeight / bitmap.getHeight()
        );

        int scaledWidth = (int) (bitmap.getWidth() * scale);
        int scaledHeight = (int) (bitmap.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

        // Create PDF document
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Center the scaled bitmap on the page
        float left = (pageWidth - scaledWidth) / 2f;
        float top = (pageHeight - scaledHeight) / 2f;
        page.getCanvas().drawBitmap(scaledBitmap, left, top, null);

        document.finishPage(page);

        // Save PDF to file
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Matrimo");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // Sanitize the name for file system
        String safeName = (data.name != null && !data.name.trim().isEmpty())
                ? data.name.trim().replaceAll("[^a-zA-Z0-9]", "_")
                : "Biodata";
        File file = new File(directory, safeName + "_" + System.currentTimeMillis() + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            Log.d(TAG, "PDF saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error writing PDF", e);
            return null;
        } finally {
            document.close();
        }

        // Get URI using FileProvider
        try {
            return FileProvider.getUriForFile(context, "com.techotd.matrimo.fileprovider", file).toString();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error getting FileProvider URI", e);
            return null;
        }
    }
}