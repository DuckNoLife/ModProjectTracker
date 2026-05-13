package com.example.modprojecttracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.adapter.ExpenseAdapter;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.sync.SyncCoordinator;
import com.example.modprojecttracker.util.Constants;
import com.example.modprojecttracker.util.SessionManager;
import com.example.modprojecttracker.util.SimpleItemSelectedListener;
import com.example.modprojecttracker.util.SimpleTextWatcher;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExpenseListActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ExpenseAdapter adapter;
    private final List<Expense> currentExpenses = new ArrayList<>();
    private String projectId;
    private TextView totalView;
    private TextView emptyView;
    private EditText searchInput;
    private Spinner sortSpinner;
    private Spinner statusFilterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_list);

        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdminLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        Project project = projectId != null ? databaseHelper.getProjectById(projectId) : null;
        if (project == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_expenses);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_expense_list));
            getSupportActionBar().setSubtitle(project.getProjectName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        totalView = findViewById(R.id.text_expense_total);
        emptyView = findViewById(R.id.text_empty_expenses);
        searchInput = findViewById(R.id.edit_search_expense);
        sortSpinner = findViewById(R.id.spinner_expense_sort);
        statusFilterSpinner = findViewById(R.id.spinner_expense_status_filter);
        RecyclerView recyclerView = findViewById(R.id.recycler_expenses);
        FloatingActionButton fab = findViewById(R.id.fab_add_expense);

        adapter = new ExpenseAdapter(new ExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onOpen(Expense expense) {
                openExpenseDetail(expense.getId());
            }

            @Override
            public void onEdit(Expense expense) {
                openExpenseForm(expense.getId());
            }

            @Override
            public void onDelete(Expense expense) {
                confirmDelete(expense);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Date newest", "Date oldest", "Amount high-low", "Amount low-high"});
        sortSpinner.setAdapter(sortAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        getString(R.string.status_all),
                        Constants.EXPENSE_STATUS_DRAFT,
                        Constants.EXPENSE_STATUS_PENDING_APPROVAL,
                        Constants.EXPENSE_STATUS_APPROVED,
                        Constants.EXPENSE_STATUS_PAID,
                        Constants.EXPENSE_STATUS_REJECTED
                });
        statusFilterSpinner.setAdapter(statusAdapter);
        statusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilters();
            }
        });

        sortSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
        });

        searchInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        });

        fab.setOnClickListener(v -> openExpenseForm(null));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SyncCoordinator.syncNow(this, new SyncCoordinator.SyncCallback() {
            @Override
            public void onSuccess() {
                loadExpenses();
            }

            @Override
            public void onError(String message) {
                loadExpenses();
            }
        });
    }

    private void loadExpenses() {
        currentExpenses.clear();
        currentExpenses.addAll(databaseHelper.getExpensesByProject(projectId));
        applyFilters();

        double total = databaseHelper.getTotalExpenseByProject(projectId);
        totalView.setText(getString(R.string.label_total_expenses, total));

        Project project = databaseHelper.getProjectById(projectId);
        if (project != null && project.getBudget() > 0 && total > project.getBudget()) {
            totalView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
            Toast.makeText(this, "Warning: Total expenses exceed project budget!", Toast.LENGTH_LONG).show();
        } else {
            totalView.setTextColor(getResources().getColor(R.color.text_primary_light, getTheme()));
        }
    }

    private void applyFilters() {
        String selectedStatus = String.valueOf(statusFilterSpinner.getSelectedItem());
        String keyword = searchInput.getText().toString().trim().toLowerCase();
        List<Expense> filtered = new ArrayList<>();
        for (Expense expense : currentExpenses) {
            if (matchesStatus(selectedStatus, expense.getStatus()) && matchesKeyword(keyword, expense)) {
                filtered.add(expense);
            }
        }

        String sortMode = String.valueOf(sortSpinner.getSelectedItem());
        if ("Date oldest".equals(sortMode)) {
            filtered.sort(Comparator.comparingLong(Expense::getDate));
        } else if ("Amount high-low".equals(sortMode)) {
            filtered.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        } else if ("Amount low-high".equals(sortMode)) {
            filtered.sort(Comparator.comparingDouble(Expense::getAmount));
        } else {
            filtered.sort((a, b) -> Long.compare(b.getDate(), a.getDate()));
        }

        adapter.setItems(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private boolean matchesKeyword(String keyword, Expense expense) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }
        return containsIgnoreCase(expense.getTitle(), keyword)
                || containsIgnoreCase(expense.getClaimant(), keyword)
                || containsIgnoreCase(expense.getDescription(), keyword)
                || containsIgnoreCase(expense.getType(), keyword);
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }

    private boolean matchesStatus(String selectedStatus, String expenseStatus) {
        if (selectedStatus == null || selectedStatus.trim().isEmpty() || getString(R.string.status_all).equals(selectedStatus)) {
            return true;
        }
        return normalizeStatus(selectedStatus).equals(normalizeStatus(expenseStatus));
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        String trimmed = status.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if ("Pending".equalsIgnoreCase(trimmed)) {
            return Constants.EXPENSE_STATUS_PENDING_APPROVAL;
        }
        if ("Reimbursed".equalsIgnoreCase(trimmed)) {
            return Constants.EXPENSE_STATUS_APPROVED;
        }
        return trimmed;
    }

    private void openExpenseForm(String expenseId) {
        Intent intent = new Intent(this, ExpenseFormActivity.class);
        intent.putExtra(ExpenseFormActivity.EXTRA_PROJECT_ID, projectId);
        if (expenseId != null) {
            intent.putExtra(ExpenseFormActivity.EXTRA_EXPENSE_ID, expenseId);
        }
        startActivity(intent);
    }

    private void openExpenseDetail(String expenseId) {
        Intent intent = new Intent(this, ExpenseDetailActivity.class);
        intent.putExtra(ExpenseDetailActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(ExpenseDetailActivity.EXTRA_EXPENSE_ID, expenseId);
        startActivity(intent);
    }

    private void confirmDelete(Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete expense")
                .setMessage("Delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteExpense(expense.getId());
                    SyncCoordinator.syncNow(this, new SyncCoordinator.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            loadExpenses();
                        }

                        @Override
                        public void onError(String message) {
                            loadExpenses();
                        }
                    });
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



