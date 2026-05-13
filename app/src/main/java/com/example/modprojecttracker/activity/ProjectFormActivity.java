package com.example.modprojecttracker.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.cloud.CloudinaryUploadService;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.sync.SyncCoordinator;
import com.example.modprojecttracker.util.Constants;
import com.example.modprojecttracker.util.DateUtil;
import com.example.modprojecttracker.util.SessionManager;
import com.example.modprojecttracker.util.ValidationUtil;

import java.util.Calendar;
import java.util.UUID;

public class ProjectFormActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private EditText editName;
    private EditText editDescription;
    private EditText editManager;
    private EditText editBudget;
    private EditText editStartDate;
    private EditText editEndDate;
    private Spinner spinnerStatus;
    private Project editingProject;
    private long selectedStartDate = -1L;
    private long selectedEndDate = -1L;
    private TextView imageUploadStatus;
    private String pendingImageUrl;

    private final CloudinaryUploadService cloudinaryUploadService = new CloudinaryUploadService();
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null || result.getData().getData() == null) {
                    return;
                }
                Uri imageUri = result.getData().getData();
                uploadSelectedImage(imageUri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_form);

        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdminLoggedIn()) {
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_project_form);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_project_form));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        editName = findViewById(R.id.edit_project_name);
        editDescription = findViewById(R.id.edit_project_description);
        editManager = findViewById(R.id.edit_project_manager);
        editBudget = findViewById(R.id.edit_project_budget);
        editStartDate = findViewById(R.id.edit_project_start_date);
        editEndDate = findViewById(R.id.edit_project_end_date);
        spinnerStatus = findViewById(R.id.spinner_project_status);
        imageUploadStatus = findViewById(R.id.text_image_upload_status);
        Button pickImageButton = findViewById(R.id.button_pick_image);
        Button saveButton = findViewById(R.id.button_save_project);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{Constants.PROJECT_STATUS_ACTIVE, Constants.PROJECT_STATUS_COMPLETED, Constants.PROJECT_STATUS_ON_HOLD});
        spinnerStatus.setAdapter(statusAdapter);
        updateImageStatus();

        editStartDate.setOnClickListener(v -> showDatePicker(true));
        editEndDate.setOnClickListener(v -> showDatePicker(false));
        pickImageButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveProject());

        String projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (!TextUtils.isEmpty(projectId)) {
            editingProject = databaseHelper.getProjectById(projectId);
            if (editingProject != null) {
                if (!isCurrentUserOwner(editingProject)) {
                    Toast.makeText(this, "Only project owner can edit", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                populateForm(editingProject);
            }
        }
    }

    private void populateForm(Project project) {
        editName.setText(project.getProjectName());
        editDescription.setText(project.getDescription());
        editManager.setText(project.getManager());
        editBudget.setText(String.valueOf(project.getBudget()));
        editStartDate.setText(DateUtil.formatDate(project.getStartDate()));
        editEndDate.setText(DateUtil.formatDate(project.getEndDate()));
        pendingImageUrl = project.getImageUrl();
        updateImageStatus();
        selectedStartDate = project.getStartDate();
        selectedEndDate = project.getEndDate();
        int statusIndex = ((ArrayAdapter<String>) spinnerStatus.getAdapter()).getPosition(project.getStatus());
        if (statusIndex >= 0) {
            spinnerStatus.setSelection(statusIndex);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadSelectedImage(Uri imageUri) {
        imageUploadStatus.setText("Image: uploading...");
        cloudinaryUploadService.uploadImage(this, imageUri, new CloudinaryUploadService.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                pendingImageUrl = imageUrl;
                updateImageStatus();
                Toast.makeText(ProjectFormActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                imageUploadStatus.setText("Image: upload failed");
                Toast.makeText(ProjectFormActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateImageStatus() {
        if (TextUtils.isEmpty(pendingImageUrl)) {
            imageUploadStatus.setText("Image: not selected");
            return;
        }
        imageUploadStatus.setText("Image URL ready");
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            long timestamp = selected.getTimeInMillis();
            if (isStartDate) {
                selectedStartDate = timestamp;
                editStartDate.setText(DateUtil.formatDate(timestamp));
            } else {
                selectedEndDate = timestamp;
                editEndDate.setText(DateUtil.formatDate(timestamp));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveProject() {
        if (TextUtils.isEmpty(editName.getText()) || TextUtils.isEmpty(editDescription.getText())
                || TextUtils.isEmpty(editManager.getText()) || TextUtils.isEmpty(editBudget.getText())
                || selectedStartDate <= 0 || selectedEndDate <= 0) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Project project = editingProject != null ? editingProject : new Project();
        if (editingProject == null) {
            project.setId(UUID.randomUUID().toString());
            project.setOwnerId(sessionManager.getUserId());
        }

        project.setProjectName(editName.getText().toString().trim());
        project.setDescription(editDescription.getText().toString().trim());
        project.setManager(editManager.getText().toString().trim());
        project.setBudget(Double.parseDouble(editBudget.getText().toString().trim()));
        project.setStartDate(selectedStartDate);
        project.setEndDate(selectedEndDate);
        project.setImageUrl(pendingImageUrl);
        project.setStatus(String.valueOf(spinnerStatus.getSelectedItem()));
        project.setLastModified(System.currentTimeMillis());

        String validationError = ValidationUtil.getProjectValidationError(project);
        if (validationError != null) {
            Toast.makeText(this, validationError, Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingProject == null) {
            databaseHelper.insertProject(project);
            Toast.makeText(this, "Project created", Toast.LENGTH_SHORT).show();
        } else {
            int updated = databaseHelper.updateProjectAsOwner(project, sessionManager.getUserId());
            if (updated == 0) {
                Toast.makeText(this, "Only project owner can edit", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Project updated", Toast.LENGTH_SHORT).show();
        }

        SyncCoordinator.syncNow(this, new SyncCoordinator.SyncCallback() {
            @Override
            public void onSuccess() {
                // Keep save flow fast; sync result is best-effort.
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ProjectFormActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }

    private boolean isCurrentUserOwner(Project project) {
        String currentAdminId = sessionManager.getUserId();
        return currentAdminId != null && currentAdminId.equals(project.getOwnerId());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}




