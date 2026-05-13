package com.example.modprojecttracker.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.adapter.ProjectAdapter;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.sync.SyncCoordinator;
import com.example.modprojecttracker.util.Constants;
import com.example.modprojecttracker.util.SessionManager;
import com.example.modprojecttracker.util.SimpleItemSelectedListener;
import com.example.modprojecttracker.util.SimpleTextWatcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProjectListActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ProjectAdapter adapter;
    private final List<Project> currentProjects = new ArrayList<>();
    private EditText searchInput;
    private Spinner sortSpinner;
    private Spinner statusSpinner;

    public static final String EXTRA_OPEN_FORM = "open_form";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdminLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_projects);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_projects));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        searchInput = findViewById(R.id.edit_search_project);
        sortSpinner = findViewById(R.id.spinner_sort_projects);
        statusSpinner = findViewById(R.id.spinner_status_filter);
        RecyclerView recyclerView = findViewById(R.id.recycler_projects);
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_project);

        adapter = new ProjectAdapter(new ProjectAdapter.OnProjectActionListener() {
            @Override
            public void onOpen(Project project) {
                openDetail(project.getId());
            }

            @Override
            public void onEdit(Project project) {
                if (!isCurrentUserOwner(project)) {
                    Toast.makeText(ProjectListActivity.this, "Only project owner can edit", Toast.LENGTH_SHORT).show();
                    return;
                }
                openForm(project.getId());
            }

            @Override
            public void onDelete(Project project) {
                if (!isCurrentUserOwner(project)) {
                    Toast.makeText(ProjectListActivity.this, "Only project owner can delete", Toast.LENGTH_SHORT).show();
                    return;
                }
                confirmDelete(project);
            }
        });
        adapter.setCurrentAdminId(sessionManager.getUserId());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Recently updated", "Name A-Z", "Budget high-low", "Budget low-high"});
        sortSpinner.setAdapter(sortAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        getString(R.string.status_all),
                        getString(R.string.status_active),
                        getString(R.string.status_completed),
                        getString(R.string.status_on_hold)
                });
        statusSpinner.setAdapter(statusAdapter);

        searchInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProjects();
            }
        });

        sortSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                filterProjects();
            }
        });

        statusSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                filterProjects();
            }
        });

        fabAdd.setOnClickListener(v -> openForm(null));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_projects);
        bottomNavigationView.setSelectedItemId(R.id.nav_projects);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_projects) {
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                showSettingsDialog();
                return false;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SyncCoordinator.syncNow(this, new SyncCoordinator.SyncCallback() {
            @Override
            public void onSuccess() {
                loadProjects();
            }

            @Override
            public void onError(String message) {
                loadProjects();
            }
        });
    }

    private void loadProjects() {
        currentProjects.clear();
        currentProjects.addAll(databaseHelper.getProjectsVisibleToAdmin(sessionManager.getUserId()));
        filterProjects();
    }

    private void filterProjects() {
        String keyword = searchInput.getText().toString().trim().toLowerCase();
        String selectedStatus = String.valueOf(statusSpinner.getSelectedItem());
        List<Project> filtered = new ArrayList<>();
        for (Project project : currentProjects) {
            boolean match = keyword.isEmpty()
                    || project.getProjectName().toLowerCase().contains(keyword)
                    || project.getDescription().toLowerCase().contains(keyword)
                    || project.getManager().toLowerCase().contains(keyword);
            boolean statusMatch = getString(R.string.status_all).equals(selectedStatus)
                    || selectedStatus.equals(project.getStatus());
            if (match && statusMatch) {
                filtered.add(project);
            }
        }

        String sortMode = String.valueOf(sortSpinner.getSelectedItem());
        if ("Name A-Z".equals(sortMode)) {
            filtered.sort(Comparator.comparing(Project::getProjectName, String.CASE_INSENSITIVE_ORDER));
        } else if ("Budget high-low".equals(sortMode)) {
            filtered.sort((a, b) -> Double.compare(b.getBudget(), a.getBudget()));
        } else if ("Budget low-high".equals(sortMode)) {
            filtered.sort(Comparator.comparingDouble(Project::getBudget));
        } else {
            filtered.sort((a, b) -> Long.compare(b.getLastModified(), a.getLastModified()));
        }

        adapter.setItems(filtered);
    }

    private void openDetail(String projectId) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(ProjectDetailActivity.EXTRA_PROJECT_ID, projectId);
        startActivity(intent);
    }

    private void openForm(String projectId) {
        Intent intent = new Intent(this, ProjectFormActivity.class);
        if (projectId != null) {
            intent.putExtra(ProjectFormActivity.EXTRA_PROJECT_ID, projectId);
        }
        startActivity(intent);
    }

    private void confirmDelete(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Delete project")
                .setMessage("Delete \"" + project.getProjectName() + "\" and all related expenses?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String currentAdminId = sessionManager.getUserId();
                    int deleted = databaseHelper.deleteProjectAsOwner(project.getId(), currentAdminId);
                    if (deleted == 0) {
                        Toast.makeText(this, "Only project owner can delete", Toast.LENGTH_SHORT).show();
                    }
                    loadProjects();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isCurrentUserOwner(Project project) {
        String currentAdminId = sessionManager.getUserId();
        return currentAdminId != null && currentAdminId.equals(project.getOwnerId());
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Settings")
                .setItems(new String[]{"Gemini AI Assistant", "Logout"}, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(ProjectListActivity.this, AiAssistantActivity.class));
                    } else if (which == 1) {
                        sessionManager.clearSession();
                        startActivity(new Intent(ProjectListActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}


