package com.example.modprojecttracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.util.ProjectImageLoader;
import com.example.modprojecttracker.util.DateUtil;
import com.example.modprojecttracker.util.SessionManager;

import java.util.Locale;

public class ProjectDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private Project project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdminLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_project_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_project_detail));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        String projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId != null) {
            project = databaseHelper.getProjectById(projectId);
        }

        if (project == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        boolean canModify = isCurrentUserOwner(project);

        ((TextView) findViewById(R.id.text_detail_name)).setText(project.getProjectName());
        ((TextView) findViewById(R.id.text_detail_description)).setText(project.getDescription());
        ((TextView) findViewById(R.id.text_detail_manager)).setText(project.getManager());
        ((TextView) findViewById(R.id.text_detail_status)).setText(project.getStatus());
        ((TextView) findViewById(R.id.text_detail_budget)).setText(String.format(Locale.US, "$%.2f", project.getBudget()));
        
        TextView totalExpenseLabel = findViewById(R.id.text_detail_total_expense);
        if (totalExpenseLabel != null) {
            double totalExpense = databaseHelper.getTotalExpenseByProject(project.getId());
            totalExpenseLabel.setText(String.format(Locale.US, "Total Expense: $%.2f", totalExpense));
            if (project.getBudget() > 0 && totalExpense > project.getBudget()) {
                totalExpenseLabel.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
            }
        }

        ((TextView) findViewById(R.id.text_detail_dates)).setText(String.format(Locale.US, "%s → %s",
                DateUtil.formatDate(project.getStartDate()),
                DateUtil.formatDate(project.getEndDate())));

        bindProjectImage(project.getImageUrl());

        TextView accessBadge = findViewById(R.id.text_detail_access_badge);
        accessBadge.setText(canModify ? getString(R.string.badge_owner) : getString(R.string.badge_read_only));
        accessBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getColor(canModify ? R.color.neon_purple : R.color.project_hold)));

        View editButton = findViewById(R.id.button_detail_edit);
        View deleteButton = findViewById(R.id.button_detail_delete);
        View expensesButton = findViewById(R.id.button_detail_expenses);

        editButton.setVisibility(canModify ? View.VISIBLE : View.GONE);
        deleteButton.setVisibility(canModify ? View.VISIBLE : View.GONE);

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProjectFormActivity.class);
            intent.putExtra(ProjectFormActivity.EXTRA_PROJECT_ID, project.getId());
            startActivity(intent);
        });

        expensesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseListActivity.class);
            intent.putExtra(ExpenseListActivity.EXTRA_PROJECT_ID, project.getId());
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete project")
                .setMessage("Are you sure you want to delete this project?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String currentAdminId = sessionManager.getUserId();
                    int deleted = databaseHelper.deleteProjectAsOwner(project.getId(), currentAdminId);
                    if (deleted == 0) {
                        Toast.makeText(this, "Only project owner can delete", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isCurrentUserOwner(Project currentProject) {
        String currentAdminId = sessionManager.getUserId();
        return currentAdminId != null && currentAdminId.equals(currentProject.getOwnerId());
    }

    private void bindProjectImage(String imageUrl) {
        ImageView coverImage = findViewById(R.id.image_detail_project_cover);
        View placeholder = findViewById(R.id.text_detail_project_placeholder);
        ProjectImageLoader.load(imageUrl, coverImage, placeholder);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}


