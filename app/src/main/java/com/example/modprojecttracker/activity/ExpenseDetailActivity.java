package com.example.modprojecttracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.sync.SyncCoordinator;
import com.example.modprojecttracker.util.Constants;
import com.example.modprojecttracker.util.DateUtil;
import com.example.modprojecttracker.util.SessionManager;

public class ExpenseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_EXPENSE_ID = "expense_id";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private String projectId;
    private String expenseId;

    private Expense expense;

    private TextView titleView;
    private TextView statusView;
    private TextView amountView;
    private TextView dateView;
    private TextView typeView;
    private TextView paymentView;
    private TextView claimantView;
    private TextView descriptionView;
    private TextView locationView;

    private View submitButton;
    private View approveButton;
    private View rejectButton;
    private View paidButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdminLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        expenseId = getIntent().getStringExtra(EXTRA_EXPENSE_ID);

        if (projectId == null || expenseId == null) {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Project project = databaseHelper.getProjectById(projectId);
        if (project == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_expense_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_expense_detail));
            getSupportActionBar().setSubtitle(project.getProjectName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        bindViews();
        bindActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SyncCoordinator.syncNow(this, new SyncCoordinator.SyncCallback() {
            @Override
            public void onSuccess() {
                loadExpense();
            }

            @Override
            public void onError(String message) {
                loadExpense();
            }
        });
    }

    private void bindViews() {
        titleView = findViewById(R.id.text_expense_detail_title);
        statusView = findViewById(R.id.text_expense_detail_status);
        amountView = findViewById(R.id.text_expense_detail_amount);
        dateView = findViewById(R.id.text_expense_detail_date);
        typeView = findViewById(R.id.text_expense_detail_type);
        paymentView = findViewById(R.id.text_expense_detail_payment);
        claimantView = findViewById(R.id.text_expense_detail_claimant);
        descriptionView = findViewById(R.id.text_expense_detail_description);
        locationView = findViewById(R.id.text_expense_detail_location);

        submitButton = findViewById(R.id.button_submit_approval);
        approveButton = findViewById(R.id.button_approve_expense);
        rejectButton = findViewById(R.id.button_reject_expense);
        paidButton = findViewById(R.id.button_mark_paid_expense);
    }

    private void bindActions() {
        findViewById(R.id.button_edit_expense_detail).setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseFormActivity.class);
            intent.putExtra(ExpenseFormActivity.EXTRA_PROJECT_ID, projectId);
            intent.putExtra(ExpenseFormActivity.EXTRA_EXPENSE_ID, expenseId);
            startActivity(intent);
        });

        findViewById(R.id.button_delete_expense_detail).setOnClickListener(v -> confirmDelete());

        submitButton.setOnClickListener(v -> updateStatus(Constants.EXPENSE_STATUS_PENDING_APPROVAL));
        approveButton.setOnClickListener(v -> updateStatus(Constants.EXPENSE_STATUS_APPROVED));
        rejectButton.setOnClickListener(v -> updateStatus(Constants.EXPENSE_STATUS_REJECTED));
        paidButton.setOnClickListener(v -> updateStatus(Constants.EXPENSE_STATUS_PAID));
    }

    private void loadExpense() {
        expense = databaseHelper.getExpenseById(expenseId);
        if (expense == null || !projectId.equals(expense.getProjectId())) {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        titleView.setText(safeText(expense.getTitle()));
        statusView.setText(safeText(expense.getStatus()));
        amountView.setText(getString(R.string.label_expense_amount, expense.getAmount()));
        dateView.setText(getString(R.string.label_expense_date, DateUtil.formatDate(expense.getDate())));
        typeView.setText(getString(R.string.label_expense_type, safeText(expense.getType())));
        paymentView.setText(getString(R.string.label_expense_payment_method, safeText(expense.getPaymentMethod())));
        claimantView.setText(getString(R.string.label_expense_claimant, safeText(expense.getClaimant())));
        descriptionView.setText(safeText(expense.getDescription()));
        locationView.setText(safeText(expense.getLocation()));

        applyActionVisibility(expense.getStatus());
    }

    private void applyActionVisibility(String status) {
        submitButton.setVisibility(View.GONE);
        approveButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
        paidButton.setVisibility(View.GONE);

        if (Constants.EXPENSE_STATUS_DRAFT.equals(status)) {
            submitButton.setVisibility(View.VISIBLE);
            return;
        }

        if (Constants.EXPENSE_STATUS_PENDING_APPROVAL.equals(status)) {
            approveButton.setVisibility(View.VISIBLE);
            rejectButton.setVisibility(View.VISIBLE);
            return;
        }

        if (Constants.EXPENSE_STATUS_APPROVED.equals(status)) {
            paidButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatus(String nextStatus) {
        if (expense == null) {
            return;
        }

        expense.setStatus(nextStatus);
        expense.setLastModified(System.currentTimeMillis());
        databaseHelper.updateExpense(expense);

        Project project = databaseHelper.getProjectById(projectId);
        if (project != null) {
            project.setLastModified(System.currentTimeMillis());
            databaseHelper.updateProject(project);
        }

        SyncCoordinator.syncNow(this, new SyncCoordinator.SyncCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ExpenseDetailActivity.this,
                        getString(R.string.message_expense_status_updated, nextStatus),
                        Toast.LENGTH_SHORT).show();
                loadExpense();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ExpenseDetailActivity.this,
                        getString(R.string.message_expense_status_updated, nextStatus),
                        Toast.LENGTH_SHORT).show();
                loadExpense();
            }
        });
    }

    private void confirmDelete() {
        if (expense == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete expense")
                .setMessage("Delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteExpense(expense.getId());
                    SyncCoordinator.syncNow(this, new SyncCoordinator.SyncCallback() {
                        @Override
                        public void onSuccess() {
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            finish();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String safeText(String text) {
        return text == null || text.trim().isEmpty() ? "-" : text.trim();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}




