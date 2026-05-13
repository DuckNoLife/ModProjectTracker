package com.example.modprojecttracker.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class ExpenseFormActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_EXPENSE_ID = "expense_id";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private TextInputLayout titleLayout;

    private EditText titleInput;
    private EditText amountInput;
    private EditText dateInput;
    private EditText claimantInput;
    private EditText descriptionInput;
    private EditText locationInput;
    private Spinner typeSpinner;
    private Spinner paymentSpinner;
    private Spinner statusSpinner;
    private Spinner currencySpinner;

    private String projectId;
    private Expense editingExpense;
    private long selectedDate = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_form);

        databaseHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (!sessionManager.isAdminLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null || databaseHelper.getProjectById(projectId) == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_expense_form);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.title_expense_form));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        titleLayout = findViewById(R.id.layout_expense_title);
        titleInput = findViewById(R.id.edit_expense_title);
        amountInput = findViewById(R.id.edit_expense_amount);
        dateInput = findViewById(R.id.edit_expense_date);
        claimantInput = findViewById(R.id.edit_expense_claimant);
        descriptionInput = findViewById(R.id.edit_expense_description);
        locationInput = findViewById(R.id.edit_expense_location);
        typeSpinner = findViewById(R.id.spinner_expense_type);
        paymentSpinner = findViewById(R.id.spinner_expense_payment);
        statusSpinner = findViewById(R.id.spinner_expense_status);
        currencySpinner = findViewById(R.id.spinner_expense_currency);
        Button saveButton = findViewById(R.id.button_save_expense);

        setupSpinners();

        titleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearTitleError();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dateInput.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> saveExpense());

        selectedDate = System.currentTimeMillis();
        dateInput.setText(DateUtil.formatDate(selectedDate));
        claimantInput.setText(sessionManager.getUsername());
        setSpinnerSelection(statusSpinner, Constants.EXPENSE_STATUS_DRAFT);

        String expenseId = getIntent().getStringExtra(EXTRA_EXPENSE_ID);
        if (!TextUtils.isEmpty(expenseId)) {
            editingExpense = databaseHelper.getExpenseById(expenseId);
            if (editingExpense == null) {
                Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (!projectId.equals(editingExpense.getProjectId())) {
                Toast.makeText(this, "Expense does not belong to this project", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            populateForm(editingExpense);
        }
    }

    private void setupSpinners() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        Constants.EXPENSE_TYPE_TRAVEL,
                        Constants.EXPENSE_TYPE_EQUIPMENT,
                        Constants.EXPENSE_TYPE_MATERIALS,
                        Constants.EXPENSE_TYPE_SERVICES,
                        Constants.EXPENSE_TYPE_SOFTWARE,
                        Constants.EXPENSE_TYPE_LABOUR,
                        Constants.EXPENSE_TYPE_UTILITIES,
                        Constants.EXPENSE_TYPE_MISC
                });
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        Constants.PAYMENT_CASH,
                        Constants.PAYMENT_CREDIT_CARD,
                        Constants.PAYMENT_BANK_TRANSFER,
                        Constants.PAYMENT_CHEQUE,
                });
        paymentSpinner.setAdapter(paymentAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        Constants.EXPENSE_STATUS_DRAFT,
                        Constants.EXPENSE_STATUS_PENDING_APPROVAL,
                        Constants.EXPENSE_STATUS_APPROVED,
                        Constants.EXPENSE_STATUS_PAID,
                        Constants.EXPENSE_STATUS_REJECTED
                });
        statusSpinner.setAdapter(statusAdapter);

        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        Constants.CURRENCY_USD,
                        Constants.CURRENCY_EUR,
                        Constants.CURRENCY_VND,
                        Constants.CURRENCY_GBP,
                        Constants.CURRENCY_JPY
                });
        currencySpinner.setAdapter(currencyAdapter);
    }

    private void populateForm(Expense expense) {
        titleInput.setText(expense.getTitle());
        amountInput.setText(String.format(Locale.US, "%.2f", expense.getAmount()));
        selectedDate = expense.getDate();
        dateInput.setText(DateUtil.formatDate(selectedDate));
        claimantInput.setText(expense.getClaimant());
        descriptionInput.setText(expense.getDescription());
        locationInput.setText(expense.getLocation());

        setSpinnerSelection(typeSpinner, expense.getType());
        setSpinnerSelection(paymentSpinner, expense.getPaymentMethod());
        setSpinnerSelection(statusSpinner, expense.getStatus());
        setSpinnerSelection(currencySpinner, expense.getCurrency());
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) {
            return;
        }
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && value.equals(String.valueOf(item))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            selectedDate = selected.getTimeInMillis();
            dateInput.setText(DateUtil.formatDate(selectedDate));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveExpense() {
        String titleText = titleInput.getText().toString().trim();
        String amountText = amountInput.getText().toString().trim();
        String claimantText = claimantInput.getText().toString().trim();
        if (!validateTitle(titleText)) {
            titleInput.requestFocus();
            return;
        }
        if (amountText.isEmpty() || claimantText.isEmpty() || selectedDate <= 0) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Amount is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

          Expense expense = editingExpense != null ? editingExpense : new Expense();
          if (TextUtils.isEmpty(expense.getId())) {
              expense.setId(UUID.randomUUID().toString());
          }

          expense.setProjectId(projectId);
          expense.setTitle(titleText);
          expense.setDate(selectedDate);
          expense.setAmount(amount);
          expense.setType(String.valueOf(typeSpinner.getSelectedItem()));
          expense.setCurrency(String.valueOf(currencySpinner.getSelectedItem()));
          expense.setPaymentMethod(String.valueOf(paymentSpinner.getSelectedItem()));
          expense.setClaimant(claimantText);
          expense.setDescription(descriptionInput.getText().toString().trim());
          expense.setLocation(locationInput.getText().toString().trim());
          expense.setStatus(String.valueOf(statusSpinner.getSelectedItem()));
          expense.setDeleted(false);
          expense.setDeletedAt(0L);
          expense.setLastModified(System.currentTimeMillis());

        if (editingExpense == null) {
            databaseHelper.insertExpense(expense);
            Toast.makeText(this, "Expense created", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.updateExpense(expense);
            Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
        }

        Project project = databaseHelper.getProjectById(projectId);
        if (project != null) {
            project.setLastModified(System.currentTimeMillis());
            databaseHelper.updateProject(project);
        }

        SyncCoordinator.syncNow(this, new SyncCoordinator.SyncCallback() {
            @Override
            public void onSuccess() {
                // Best-effort sync after local save.
            }

            @Override
            public void onError(String message) {
                // Keep form flow fast; local save is already committed.
            }
        });

        finish();
    }

    private boolean validateTitle(String titleText) {
        if (TextUtils.isEmpty(titleText)) {
            titleLayout.setError(getString(R.string.error_expense_title_required));
            titleInput.requestFocus();
            return false;
        }
        if (titleText.trim().length() < 3) {
            titleLayout.setError(getString(R.string.error_expense_title_too_short));
            titleInput.requestFocus();
            return false;
        }
        clearTitleError();
        return true;
    }

    private void clearTitleError() {
        if (titleLayout != null) {
            titleLayout.setError(null);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}





