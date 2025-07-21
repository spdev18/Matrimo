package com.techotd.matrimo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.LoadAdError;
import com.techotd.matrimo.utils.UserData;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class FormActivity extends AppCompatActivity {
    private static final String TAG = "FormActivity";
    private static final int STORAGE_PERMISSION_CODE = 100;
    private ImageView photoView;
    private EditText name, age, religion, caste, profession, qualification, fatherName,
            fatherOccupation, motherName, motherOccupation, brothers, sisters, contact, address, hobbies, additionalInfo, height, weight ;
    private Spinner genderSpinner;
    private String photoPath;
    private ActivityResultLauncher<Intent> photoPickerLauncher;
    private AutoCompleteTextView genderDropdown;
    private final ActivityResultLauncher<Intent> previewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    UserData data = result.getData().getParcelableExtra("user_data");
                    if (data != null) {
                        updateFormWithData(data);
                    }
                }
            });

    private AdView mAdView, mAdView1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        initializeAdMob();

        // --- Help Button: Open HelpFeedbackActivity ---
        findViewById(R.id.helpButton).setOnClickListener(v -> {
            Intent intent = new Intent(FormActivity.this, HelpFeedbackActivity.class);
            startActivity(intent);
        });

        // --- Setting Button: Open SettingsActivity ---
        findViewById(R.id.settingsButton).setOnClickListener(v -> {
            Intent intent = new Intent(FormActivity.this, SettingsActivity.class);
            startActivity(intent);
        });



        initializeViews();
        setupGenderSpinner();
        setupPhotoPicker();

        // Restore saved state if available
        if (savedInstanceState != null) {
            restoreFromSavedState(savedInstanceState);
        }
        // Check for data coming back from PreviewActivity
        else if (getIntent().hasExtra("user_data")) {
            UserData data = getIntent().getParcelableExtra("user_data");
            if (data != null) {
                updateFormWithData(data);
            }
        }

        setupContinueButton();
    }

    private void initializeAdMob() {
        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d(TAG, "AdMob SDK initialized: " + initializationStatus.toString());
                // Load ad after initialization completes
                loadBannerAd();
            }
        });
    }

    private void loadBannerAd() {
        mAdView = findViewById(R.id.adView);
        mAdView1 = findViewById(R.id.adView1);

        // Create ad request
        AdRequest adRequest = new AdRequest.Builder().build();

        // Add listener for better debugging
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.e(TAG, "Ad failed to load: " + loadAdError.getMessage() +
                        " (Code: " + loadAdError.getCode() + ")");
            }
        });

        // Load the ad directly without setting size
        mAdView.loadAd(adRequest);
        mAdView1.loadAd(adRequest);
    }

    private void initializeViews() {
        photoView = findViewById(R.id.photo);
        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        genderDropdown = findViewById(R.id.gender);
        religion = findViewById(R.id.religion);
        caste = findViewById(R.id.caste);
        profession = findViewById(R.id.profession);
        qualification = findViewById(R.id.qualification);
        fatherName = findViewById(R.id.father_name);
        fatherOccupation = findViewById(R.id.father_occupation);
        motherName = findViewById(R.id.mother_name);
        motherOccupation = findViewById(R.id.mother_occupation);
        brothers = findViewById(R.id.brothers);
        sisters = findViewById(R.id.sisters);
        contact = findViewById(R.id.contact);
        address = findViewById(R.id.address);
        hobbies = findViewById(R.id.hobbies);
        additionalInfo = findViewById(R.id.additional_info);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
    }

    private void setupGenderSpinner() {
        String[] genders = {"Male", "Female", "Other"};
        // Use custom layout with explicit black text on white background
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, genders);
        genderDropdown.setAdapter(adapter);
        // Set dropdown background explicitly
        genderDropdown.setDropDownBackgroundResource(android.R.color.white);
        genderDropdown.setOnClickListener(v -> genderDropdown.showDropDown());
    }

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri photoUri = result.getData().getData();
                photoPath = photoUri.toString();
                Glide.with(this).load(photoUri).into(photoView);
                Log.d(TAG, "Photo selected: " + photoPath);
            }
        });

        findViewById(R.id.select_photo).setOnClickListener(v -> {
            if (checkStoragePermission()) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerLauncher.launch(intent);
            } else {
                requestStoragePermission();
            }
        });
    }

    private void setupContinueButton() {
        findViewById(R.id.continue_btn).setOnClickListener(v -> {
            if (!validateFields()) return;

            UserData data = createUserDataFromForm();
            Intent intent = new Intent(FormActivity.this, TemplateSelectActivity.class);
            intent.putExtra("user_data", data);
            startActivity(intent);
        });
    }

    private UserData createUserDataFromForm() {
        UserData data = new UserData();
        data.name = name.getText().toString().trim();
        data.age = age.getText().toString().trim();
        data.gender = genderDropdown.getText().toString().trim();
        data.religion = religion.getText().toString().trim();
        data.caste = caste.getText().toString().trim();
        data.height = height.getText().toString().trim();
        data.weight = weight.getText().toString().trim();
        data.profession = profession.getText().toString().trim();
        data.qualification = qualification.getText().toString().trim();
        data.fatherName = fatherName.getText().toString().trim();
        data.fatherOccupation = fatherOccupation.getText().toString().trim();
        data.motherName = motherName.getText().toString().trim();
        data.motherOccupation = motherOccupation.getText().toString().trim();
        data.brothers = brothers.getText().toString().trim();
        data.sisters = sisters.getText().toString().trim();
        data.contact = contact.getText().toString().trim();
        data.address = address.getText().toString().trim();
        data.hobbies = hobbies.getText().toString().trim();
        data.additionalInfo = additionalInfo.getText().toString().trim();
        data.photoPath = photoPath;
        return data;
    }

    private void updateFormWithData(UserData data) {
        name.setText(data.name);
        age.setText(data.age);
        genderDropdown.setText(data.gender, false);
        religion.setText(data.religion);
        caste.setText(data.caste);
        height.setText(data.height);
        weight.setText(data.weight);
        profession.setText(data.profession);
        qualification.setText(data.qualification);
        fatherName.setText(data.fatherName);
        fatherOccupation.setText(data.fatherOccupation);
        motherName.setText(data.motherName);
        motherOccupation.setText(data.motherOccupation);
        brothers.setText(data.brothers);
        sisters.setText(data.sisters);
        contact.setText(data.contact);
        address.setText(data.address);
        hobbies.setText(data.hobbies);
        additionalInfo.setText(data.additionalInfo);

        photoPath = data.photoPath;
        if (photoPath != null) {
            Glide.with(this).load(photoPath).into(photoView);
        }
    }

    private void restoreFromSavedState(Bundle savedInstanceState) {
        name.setText(savedInstanceState.getString("name"));
        age.setText(savedInstanceState.getString("age"));
        genderDropdown.setText(savedInstanceState.getString("gender"), false);
        religion.setText(savedInstanceState.getString("religion"));
        caste.setText(savedInstanceState.getString("caste"));
        height.setText(savedInstanceState.getString("height"));
        weight.setText(savedInstanceState.getString("weight"));
        profession.setText(savedInstanceState.getString("profession"));
        qualification.setText(savedInstanceState.getString("qualification"));
        fatherName.setText(savedInstanceState.getString("fatherName"));
        fatherOccupation.setText(savedInstanceState.getString("fatherOccupation"));
        motherName.setText(savedInstanceState.getString("motherName"));
        motherOccupation.setText(savedInstanceState.getString("motherOccupation"));
        brothers.setText(savedInstanceState.getString("brothers"));
        sisters.setText(savedInstanceState.getString("sisters"));
        contact.setText(savedInstanceState.getString("contact"));
        address.setText(savedInstanceState.getString("address"));
        hobbies.setText(savedInstanceState.getString("hobbies"));
        additionalInfo.setText(savedInstanceState.getString("additionalInfo"));

        photoPath = savedInstanceState.getString("photoPath");
        if (photoPath != null) {
            Glide.with(this).load(photoPath).into(photoView);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("name", name.getText().toString());
        outState.putString("age", age.getText().toString());
        outState.putString("gender", genderDropdown.getText().toString());
        outState.putString("religion", religion.getText().toString());
        outState.putString("caste", caste.getText().toString());
        outState.putString("height", height.getText().toString());
        outState.putString("weight", weight.getText().toString());
        outState.putString("profession", profession.getText().toString());
        outState.putString("qualification", qualification.getText().toString());
        outState.putString("fatherName", fatherName.getText().toString());
        outState.putString("fatherOccupation", fatherOccupation.getText().toString());
        outState.putString("motherName", motherName.getText().toString());
        outState.putString("motherOccupation", motherOccupation.getText().toString());
        outState.putString("brothers", brothers.getText().toString());
        outState.putString("sisters", sisters.getText().toString());
        outState.putString("contact", contact.getText().toString());
        outState.putString("address", address.getText().toString());
        outState.putString("hobbies", hobbies.getText().toString());
        outState.putString("additionalInfo", additionalInfo.getText().toString());
        outState.putString("photoPath", photoPath);
    }

    private boolean validateFields() {
        // Name validation
        String nameText = name.getText().toString().trim();
        if (nameText.isEmpty()) {
            showError(name, "Name field is required");
            return false;
        } else if (nameText.length() < 2) {
            showError(name, "Name must be at least 2 characters");
            return false;
        } else if (!nameText.matches("^[\\p{L} .'-]+$")) {
            showError(name, "Name contains invalid characters");
            return false;
        }

        // Age validation
        String ageText = age.getText().toString().trim();
        if (ageText.isEmpty()) {
            showError(age, "Age field is required");
            return false;
        } else if (ageText.length() > 3) {
            showError(age, "Age cannot be more than 3 digits");
            return false;
        } else {
            try {
                int ageValue = Integer.parseInt(ageText);
                if (ageValue < 18) {
                    showError(age, "Age must be at least 18 for matrimonial purposes");
                    return false;
                } else if (ageValue > 150) {
                    showError(age, "Please enter a valid age");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(age, "Age must be a valid number");
                return false;
            }
        }

        // Gender validation
        if (genderDropdown.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Religion validation
        if (religion.getText().toString().trim().isEmpty()) {
            showError(religion, "Religion field is required");
            return false;
        }

        // Height validation
        String heightText = height.getText().toString().trim();
        if (!heightText.isEmpty()) {
            try {
                float heightValue = Float.parseFloat(heightText);
                if (heightValue < 1.0 || heightValue > 15.0) {
                    showError(height, "Height should be between 1-15 feet");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(height, "Height must be a valid number");
                return false;
            }
        }

        // Weight validation
        String weightText = weight.getText().toString().trim();
        if (!weightText.isEmpty()) {
            try {
                int weightValue = Integer.parseInt(weightText);
                if (weightValue < 30 || weightValue > 200) {
                    showError(weight, "Weight should be between 30-200 kg");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(weight, "Weight must be a valid number");
                return false;
            }
        }

        // Professions validation
        String pro = profession.getText().toString().trim();
        if (pro.isEmpty()) {
            showError(profession, "Professions field is required");
            return false;
        } else if (pro.length() < 2) {
            showError(profession, "Professions must be at least 2 characters");
            return false;
        } else if (!pro.matches("^[\\p{L} .'-]+$")) {
            showError(profession, "Professions contains invalid characters");
            return false;
        }

        // Qualification validation
        String Qualification = qualification.getText().toString().trim();
        if (Qualification.isEmpty()) {
            showError(qualification, "Qualification field is required");
            return false;
        } else if (Qualification.length() < 2) {
            showError(qualification, "Qualification must be at least 2 characters");
            return false;
        } else if (!Qualification.matches("^[\\p{L} .'-]+$")) {
            showError(qualification, "Qualification contains invalid characters");
            return false;
        }

        // Father validation
        String Father = fatherName.getText().toString().trim();
        if (Father.isEmpty()) {
            showError(fatherName, "Father Name field is required");
            return false;
        } else if (Father.length() < 2) {
            showError(fatherName, "Father Name must be at least 2 characters");
            return false;
        } else if (!Father.matches("^[\\p{L} .'-]+$")) {
            showError(fatherName, "Father Name contains invalid characters");
            return false;
        }

        // FatherO validation
        String FatherO = fatherOccupation.getText().toString().trim();
        if (FatherO.isEmpty()) {
            showError(fatherOccupation, "Father Occupation field is required");
            return false;
        } else if (FatherO.length() < 2) {
            showError(fatherOccupation, "Father Occupation must be at least 2 characters");
            return false;
        } else if (!FatherO.matches("^[\\p{L} .'-]+$")) {
            showError(fatherOccupation, "Father Occupation contains invalid characters");
            return false;
        }

        // mother validation
        String mother = motherName.getText().toString().trim();
        if (mother.isEmpty()) {
            showError(motherName, "Mother Name field is required");
            return false;
        } else if (mother.length() < 2) {
            showError(motherName, "Mother Name must be at least 2 characters");
            return false;
        } else if (!mother.matches("^[\\p{L} .'-]+$")) {
            showError(motherName, "Mother Name contains invalid characters");
            return false;
        }

        // motherO validation
        String motherO = motherOccupation.getText().toString().trim();
        if (motherO.isEmpty()) {
            showError(motherOccupation, "Mother Occupation field is required");
            return false;
        } else if (motherO.length() < 2) {
            showError(motherOccupation, "Mother Occupation must be at least 2 characters");
            return false;
        } else if (!motherO.matches("^[\\p{L} .'-]+$")) {
            showError(motherOccupation, "Mother Occupation contains invalid characters");
            return false;
        }

        // Family members validation
        String brothersText = brothers.getText().toString().trim();
        if (!brothersText.isEmpty()) {
            try {
                int brothersValue = Integer.parseInt(brothersText);
                if (brothersValue < 0 || brothersValue > 20) {
                    showError(brothers, "Please enter a valid number of brothers");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(brothers, "Number of brothers must be a valid number");
                return false;
            }
        }

        String sistersText = sisters.getText().toString().trim();
        if (!sistersText.isEmpty()) {
            try {
                int sistersValue = Integer.parseInt(sistersText);
                if (sistersValue < 0 || sistersValue > 20) {
                    showError(sisters, "Please enter a valid number of sisters");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(sisters, "Number of sisters must be a valid number");
                return false;
            }
        }


        // Contact validation
        String contactText = contact.getText().toString().trim();
        if (contactText.isEmpty()) {
            showError(contact, "Contact field is required");
            return false;
        } else if (!contactText.matches("\\d{10}")) {
            showError(contact, "Contact number must be exactly 10 digits");
            return false;
        }

        // Address validation
        if (address.getText().toString().trim().isEmpty()) {
            showError(address, "Address field is required");
            return false;
        }


        // Hobbies validation
        String hobbiesText = hobbies.getText().toString().trim();
        if (!hobbiesText.isEmpty()) {
            // Check for minimum length
            if (hobbiesText.length() < 3) {
                showError(hobbies, "Hobbies must be at least 3 characters");
                return false;
            }

            // Check for maximum length (prevent excessively long input)
            if (hobbiesText.length() > 200) {
                showError(hobbies, "Hobbies text is too long (max 200 characters)");
                return false;
            }

            // Check for valid characters - allow letters, numbers, spaces, commas, periods
            if (!hobbiesText.matches("^[\\p{L}\\p{N}\\s,.;:-]+$")) {
                showError(hobbies, "Hobbies contain invalid characters");
                return false;
            }
        }

        return true;
    }

    // Helper method to show errors and focus on the field
    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                new String[]{Manifest.permission.READ_MEDIA_IMAGES} :
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                photoPickerLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    // Permission permanently denied
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }
    }

    // Handle AdView lifecycle
    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

}