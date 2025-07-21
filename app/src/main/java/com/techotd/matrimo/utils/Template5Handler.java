package com.techotd.matrimo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.techotd.matrimo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Template5Handler {
    private static final String TAG = "Template5Handler";
    private static final int MAX_HEIGHT_DP = 1200; // Maximum height for template5

    /**
     * Generate PDF for Template5 with fixed dimensions
     *
     * @param context Application context
     * @param data UserData to populate template
     * @param templateView Inflated template view
     * @return Path to saved PDF file
     */
    public static String generatePdf(Context context, UserData data, View templateView) {
        if (templateView == null) {
            Log.e(TAG, "Template view is null");
            return null;
        }

        // Apply fixed dimensions to the template
        fixTemplateDimensions(templateView);

        // Populate the template data (using existing logic)
        populateTemplateData(context, templateView, data);

        // Standard A4 dimensions (595 x 842 points)
        int pageWidth = 595;
        int pageHeight = 842;

        // Measure with fixed dimensions
        templateView.measure(
                View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(pageHeight, View.MeasureSpec.AT_MOST)
        );

        // Set layout bounds
        templateView.layout(0, 0, templateView.getMeasuredWidth(),
                Math.min(templateView.getMeasuredHeight(), pageHeight));

        // Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(pageWidth,
                Math.min(templateView.getMeasuredHeight(), pageHeight),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        templateView.draw(canvas);

        // Create PDF document
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Draw bitmap centered on page
        float left = (pageWidth - bitmap.getWidth()) / 2f;
        float top = (pageHeight - bitmap.getHeight()) / 2f;
        page.getCanvas().drawBitmap(bitmap, left, top, null);

        document.finishPage(page);

        // Save PDF file (reuse logic from PdfGenerator)
        return savePdfToFile(context, document, data);
    }

    /**
     * Generate PNG image for Template5 with fixed dimensions
     *
     * @param context Application context
     * @param templateView Inflated template view
     * @return Bitmap of the template
     */
    public static Bitmap generatePngBitmap(Context context, View templateView) {
        if (templateView == null) {
            Log.e(TAG, "Template view is null");
            return null;
        }

        // Apply fixed dimensions
        fixTemplateDimensions(templateView);

        // Standard width for biodata
        int pageWidth = 595;
        int maxHeight = (int)(MAX_HEIGHT_DP * context.getResources().getDisplayMetrics().density);

        // Measure with constraints
        templateView.measure(
                View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
        );

        // Layout with constraints
        templateView.layout(0, 0, templateView.getMeasuredWidth(),
                Math.min(templateView.getMeasuredHeight(), maxHeight));

        // Create bitmap with constrained height
        Bitmap bitmap = Bitmap.createBitmap(
                templateView.getMeasuredWidth(),
                templateView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        templateView.draw(canvas);

        return bitmap;
    }

    /**
     * Fix the layout dimensions for Template5
     */
    private static void fixTemplateDimensions(View view) {
        if (view == null) return;

        // Find the root layout and set fixed dimensions
        RelativeLayout rootLayout = view.findViewById(R.id.relativee);
        if (rootLayout != null) {
            // Set fixed layout params
            ViewGroup.LayoutParams params = rootLayout.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            rootLayout.setLayoutParams(params);
        }

        // Find the frame layout and constrain it
        FrameLayout frameLayout = view.findViewById(R.id.framelayout);
        if (frameLayout != null) {
            frameLayout.setPadding(frameLayout.getPaddingLeft(),
                    frameLayout.getPaddingTop(),
                    frameLayout.getPaddingRight(),
                    frameLayout.getPaddingBottom());
        }
    }

    /**
     * Populate template with user data
     */
    private static void populateTemplateData(Context context, View view, UserData data) {
        // Reuse the existing logic from PdfGenerator to populate fields
        // If photo is available, load it efficiently
        ImageView photo = view.findViewById(R.id.preview_photo);
        if (data.photoPath != null && photo != null) {
            try {
                Bitmap bmp = Glide.with(context)
                        .asBitmap()
                        .load(data.photoPath)
                        .submit(90, 110)
                        .get();
                photo.setImageBitmap(bmp);
            } catch (Exception e) {
                Log.e(TAG, "Failed to load photo", e);
            }
        }

        // Set text fields (non-null checks for each view and value)
        setTextIfViewExists(view, R.id.preview_name, data.name);
        setTextIfViewExists(view, R.id.preview_age, data.age);
        setTextIfViewExists(view, R.id.preview_gender, data.gender);
        setTextIfViewExists(view, R.id.preview_religion, data.religion);
        setTextIfViewExists(view, R.id.preview_caste, data.caste);
        setTextIfViewExists(view, R.id.preview_height, data.height);
        setTextIfViewExists(view, R.id.preview_weight, data.weight);
        setTextIfViewExists(view, R.id.preview_profession, data.profession);
        setTextIfViewExists(view, R.id.preview_qualification, data.qualification);
        setTextIfViewExists(view, R.id.preview_father_name, data.fatherName);
        setTextIfViewExists(view, R.id.preview_father_occupation, data.fatherOccupation);
        setTextIfViewExists(view, R.id.preview_mother_name, data.motherName);
        setTextIfViewExists(view, R.id.preview_mother_occupation, data.motherOccupation);
        setTextIfViewExists(view, R.id.preview_brothers, data.brothers);
        setTextIfViewExists(view, R.id.preview_sisters, data.sisters);
        setTextIfViewExists(view, R.id.preview_contact, data.contact);
        setTextIfViewExists(view, R.id.preview_address, data.address);
        setTextIfViewExists(view, R.id.preview_hobbies, data.hobbies);
        setTextIfViewExists(view, R.id.preview_additional_info, data.additionalInfo);
    }

    /**
     * Helper method to set text on TextView if it exists
     */
    private static void setTextIfViewExists(View parent, int viewId, String value) {
        android.widget.TextView textView = parent.findViewById(viewId);
        if (textView != null) {
            textView.setText(value != null ? value : "N/A");
        }
    }

    /**
     * Save PDF document to file
     */
    private static String savePdfToFile(Context context, PdfDocument document, UserData data) {
        // Create directory for saved PDFs
        File directory = new File(context.getExternalFilesDir(null), "Matrimo");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create filename from user data
        String safeName = (data.name != null && !data.name.trim().isEmpty())
                ? data.name.trim().replaceAll("[^a-zA-Z0-9]", "_")
                : "Biodata";

        File file = new File(directory, safeName + "_" + System.currentTimeMillis() + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            Log.d(TAG, "PDF saved to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error writing PDF", e);
            return null;
        } finally {
            document.close();
        }
    }
}