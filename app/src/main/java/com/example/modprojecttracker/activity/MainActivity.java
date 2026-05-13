package com.example.modprojecttracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.adapter.ProjectAdapter;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.sync.SyncCoordinator;
import com.example.modprojecttracker.sync.SyncWorker;
import com.example.modprojecttracker.util.Constants;
import com.example.modprojecttracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ProjectAdapter projectAdapter;
    private TextView totalProjectsView;
    private TextView activeProjectsView;
    private TextView totalBudgetView;
    private TextView budgetUtilizationView;
    private TextView pendingExpensesView;
    private TextView paidExpensesView;
    private TextView pendingAmountView;
    private TextView totalTrackedExpenseView;
    private TextView recentProjectsLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdminLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        schedulePeriodicSync();

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_dashboard));
        }

        totalProjectsView = findViewById(R.id.text_total_projects);
        activeProjectsView = findViewById(R.id.text_active_projects);
        totalBudgetView = findViewById(R.id.text_total_budget);
        budgetUtilizationView = findViewById(R.id.text_budget_utilization);
        pendingExpensesView = findViewById(R.id.text_pending_expenses);
        paidExpensesView = findViewById(R.id.text_paid_expenses);
        pendingAmountView = findViewById(R.id.text_pending_amount);
        totalTrackedExpenseView = findViewById(R.id.text_total_tracked_expense);
        recentProjectsLabel = findViewById(R.id.text_recent_projects_label);

        MaterialButton openProjectsButton = findViewById(R.id.button_open_projects);
        MaterialButton addProjectButton = findViewById(R.id.button_add_project);
        MaterialCardView projectCard = findViewById(R.id.card_projects_overview);
        RecyclerView recentProjectsRecycler = findViewById(R.id.recycler_recent_projects);

        projectAdapter = new ProjectAdapter(new ProjectAdapter.OnProjectActionListener() {
            @Override
            public void onOpen(Project project) {
                openProjectDetail(project.getId());
            }

            @Override
            public void onEdit(Project project) {
                openProjectForm(project.getId());
            }

            @Override
            public void onDelete(Project project) {
                openProjectDetail(project.getId());
            }
        });
        projectAdapter.setCurrentAdminId(sessionManager.getUserId());

        recentProjectsRecycler.setLayoutManager(new LinearLayoutManager(this));
        recentProjectsRecycler.setAdapter(projectAdapter);
        recentProjectsRecycler.setNestedScrollingEnabled(false);

        openProjectsButton.setOnClickListener(v -> startActivity(new Intent(this, ProjectListActivity.class)));
        addProjectButton.setOnClickListener(v -> startActivity(new Intent(this, ProjectFormActivity.class)));
        projectCard.setOnClickListener(v -> startActivity(new Intent(this, ProjectListActivity.class)));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_dashboard) {
                return true;
            } else if (item.getItemId() == R.id.nav_projects) {
                startActivity(new Intent(this, ProjectListActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                showSettingsMenu();
                return false; // Don't highlight the menu item
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
                refreshDashboard();
            }

            @Override
            public void onError(String message) {
                refreshDashboard();
            }
        });
    }

    private void refreshDashboard() {
        List<Project> projects = new ArrayList<>(databaseHelper.getProjectsVisibleToAdmin(sessionManager.getUserId()));
        projects.sort((a, b) -> Long.compare(b.getLastModified(), a.getLastModified()));

        totalProjectsView.setText(String.valueOf(projects.size()));
        int activeCount = 0;
        double totalBudget = 0.0;
        for (Project project : projects) {
            totalBudget += Math.max(project.getBudget(), 0.0);
            if (Constants.PROJECT_STATUS_ACTIVE.equals(project.getStatus())) {
                activeCount++;
            }
        }
        activeProjectsView.setText(String.valueOf(activeCount));
        totalBudgetView.setText(getString(R.string.value_currency, totalBudget));

        List<Expense> expenses = databaseHelper.getAllExpenses();
        int pendingCount = 0;
        int paidOrApprovedCount = 0;
        double pendingAmount = 0.0;
        double trackedExpense = 0.0;
        for (Expense expense : expenses) {
            double amountInUsd = com.example.modprojecttracker.util.CurrencyUtil.convertToUsd(
                    Math.max(expense.getAmount(), 0.0), expense.getCurrency());
            
            if (!Constants.EXPENSE_STATUS_REJECTED.equals(expense.getStatus())) {
                trackedExpense += amountInUsd;
            }
            if (Constants.EXPENSE_STATUS_PENDING_APPROVAL.equals(expense.getStatus())) {
                pendingCount++;
                pendingAmount += amountInUsd;
            }
            if (Constants.EXPENSE_STATUS_APPROVED.equals(expense.getStatus())
                    || Constants.EXPENSE_STATUS_PAID.equals(expense.getStatus())) {
                paidOrApprovedCount++;
            }
        }

        double utilizationPercent = totalBudget > 0.0 ? (trackedExpense / totalBudget) * 100.0 : 0.0;
        budgetUtilizationView.setText(getString(R.string.value_percent, utilizationPercent));
        
        if (utilizationPercent > 100) {
            budgetUtilizationView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
        } else {
            budgetUtilizationView.setTextColor(getResources().getColor(R.color.text_primary_light, getTheme()));
        }
        pendingExpensesView.setText(String.valueOf(pendingCount));
        paidExpensesView.setText(String.valueOf(paidOrApprovedCount));
        pendingAmountView.setText(getString(R.string.value_currency, pendingAmount));
        totalTrackedExpenseView.setText(getString(R.string.value_currency, trackedExpense));

        List<Project> recent = projects.size() > 5 ? projects.subList(0, 5) : projects;
        projectAdapter.setItems(recent);

        if (recent.isEmpty()) {
            recentProjectsLabel.setText(R.string.label_recent_projects_empty);
        } else {
            recentProjectsLabel.setText(R.string.label_recent_projects);
        }
    }

    private void openProjectDetail(String projectId) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra(ProjectDetailActivity.EXTRA_PROJECT_ID, projectId);
        startActivity(intent);
    }

    private void openProjectForm(String projectId) {
        Intent intent = new Intent(this, ProjectFormActivity.class);
        intent.putExtra(ProjectFormActivity.EXTRA_PROJECT_ID, projectId);
        startActivity(intent);
    }

    private void schedulePeriodicSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "phase3_periodic_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }

    private void showSettingsMenu() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_settings)
                .setItems(new String[]{getString(R.string.action_gemini_ai), getString(R.string.action_logout)}, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(MainActivity.this, AiAssistantActivity.class));
                    } else if (which == 1) {
                        sessionManager.clearSession();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

