package com.example.modprojecttracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modprojecttracker.BuildConfig;
import com.example.modprojecttracker.R;
import com.example.modprojecttracker.ai.GeminiService;
import com.example.modprojecttracker.adapter.AiMessageAdapter;
import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.AiMessage;
import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.util.Constants;
import com.example.modprojecttracker.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AiAssistantActivity extends AppCompatActivity {

    private static final int MAX_CONTEXT_EXPENSE_ROWS = 120;
    private static final int MAX_SAVED_MESSAGES = 120;
    private static final int MAX_HISTORY_MESSAGES_FOR_PROMPT = 16;
    private static final int MAX_MESSAGE_CONTENT_LENGTH = 1000;
    private static final String PREF_AI_HISTORY = "ai_history_store";
    private static final String HISTORY_KEY_PREFIX = "account_history_";

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private GeminiService geminiService;
    private SharedPreferences historyPreferences;
    private String currentAdminId;
    private AiMessageAdapter adapter;

    private EditText inputMessage;
    private MaterialButton sendButton;
    private ProgressBar progressBar;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        sessionManager = new SessionManager(this);
        databaseHelper = DatabaseHelper.getInstance(this);
        geminiService = new GeminiService();
        historyPreferences = getSharedPreferences(PREF_AI_HISTORY, MODE_PRIVATE);
        currentAdminId = sessionManager.getUserId();

        if (!sessionManager.isAdminLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_ai_assistant);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_ai_assistant);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recycler_ai_messages);
        inputMessage = findViewById(R.id.edit_ai_message);
        sendButton = findViewById(R.id.button_send_ai_message);
        progressBar = findViewById(R.id.progress_ai_response);
        statusText = findViewById(R.id.text_ai_status);

        adapter = new AiMessageAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadConversationHistory();

        if (!geminiService.isConfigured()) {
            statusText.setText(R.string.error_gemini_not_configured);
        }

        sendButton.setOnClickListener(v -> sendMessage());
        inputMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ai_assistant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_chat_history) {
            showClearHistoryConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Releases GeminiService resources when the activity is destroyed. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (geminiService != null) {
            geminiService.shutdown();
        }
    }

    private void sendMessage() {
        String prompt = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(prompt)) {
            return;
        }

        appendMessageAndPersist(new AiMessage(AiMessage.ROLE_USER, prompt));
        inputMessage.setText("");
        setLoading(true);

        String finalPromptToSend = buildPreloadedPrompt(prompt);
        if (BuildConfig.DEBUG) {
            android.util.Log.d("Check API", "Request payload: " + finalPromptToSend);
        }
        geminiService.ask(this, finalPromptToSend, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String text) {
                runOnUiThread(() -> {
                    appendMessageAndPersist(new AiMessage(AiMessage.ROLE_ASSISTANT, text.trim()));
                    setLoading(false);
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    appendMessageAndPersist(new AiMessage(AiMessage.ROLE_ASSISTANT,
                            buildFallbackAssistantMessage(message)));
                    inputMessage.setText(prompt);
                    inputMessage.setSelection(prompt.length());
                    Toast.makeText(AiAssistantActivity.this,
                            getString(R.string.message_ai_retry_hint),
                            Toast.LENGTH_SHORT).show();
                    setLoading(false);
                    scrollToBottom();
                });
            }
        });

        scrollToBottom();
    }

    private String buildPreloadedPrompt(String userQuestion) {
        StringBuilder promptBuilder = new StringBuilder(4096);
        promptBuilder.append(getString(R.string.ai_preload_role_prompt))
                .append("\n\nCONTEXT_DATA:\n");

        appendDataContext(promptBuilder);
        appendConversationHistoryContext(promptBuilder, userQuestion);

        promptBuilder.append("\nUSER_QUESTION:\n")
                .append(userQuestion);
        return promptBuilder.toString();
    }

    private void appendConversationHistoryContext(StringBuilder builder, String currentQuestion) {
        List<AiMessage> messages = adapter.getItemsSnapshot();
        if (messages.isEmpty()) {
            return;
        }

        int start = Math.max(0, messages.size() - MAX_HISTORY_MESSAGES_FOR_PROMPT);
        builder.append("\nCONVERSATION_HISTORY:\n");
        for (int i = start; i < messages.size(); i++) {
            AiMessage message = messages.get(i);
            if (i == messages.size() - 1
                    && message.getRole() == AiMessage.ROLE_USER
                    && sanitize(message.getContent()).equals(sanitize(currentQuestion))) {
                continue;
            }
            String role = message.getRole() == AiMessage.ROLE_USER ? "User" : "Assistant";
            builder.append("- ")
                    .append(role)
                    .append(": ")
                    .append(sanitize(message.getContent()))
                    .append('\n');
        }
    }

    private void appendDataContext(StringBuilder builder) {
        String adminId = sessionManager.getUserId();
        List<Project> projects = databaseHelper.getProjectsVisibleToAdmin(adminId);
        List<Expense> allExpenses = databaseHelper.getAllExpenses();

        Set<String> visibleProjectIds = new HashSet<>();
        for (Project project : projects) {
            visibleProjectIds.add(project.getId());
        }

        Map<String, Double> spentByProject = new HashMap<>();
        double totalBudget = 0.0;
        for (Project project : projects) {
            totalBudget += Math.max(0.0, project.getBudget());
            spentByProject.put(project.getId(), databaseHelper.getTotalExpenseByProject(project.getId()));
        }

        int draftCount = 0;
        int pendingCount = 0;
        int approvedCount = 0;
        int paidCount = 0;
        int rejectedCount = 0;

        double draftAmount = 0.0;
        double pendingAmount = 0.0;
        double approvedAmount = 0.0;
        double paidAmount = 0.0;
        double rejectedAmount = 0.0;
        double trackedAmount = 0.0;

        for (Expense expense : allExpenses) {
            if (!visibleProjectIds.contains(expense.getProjectId())) {
                continue;
            }

            double safeAmount = Math.max(0.0, expense.getAmount());
            String status = expense.getStatus();
            if (!Constants.EXPENSE_STATUS_REJECTED.equals(status)) {
                trackedAmount += safeAmount;
            }

            if (Constants.EXPENSE_STATUS_DRAFT.equals(status)) {
                draftCount++;
                draftAmount += safeAmount;
            } else if (Constants.EXPENSE_STATUS_PENDING_APPROVAL.equals(status)) {
                pendingCount++;
                pendingAmount += safeAmount;
            } else if (Constants.EXPENSE_STATUS_APPROVED.equals(status)) {
                approvedCount++;
                approvedAmount += safeAmount;
            } else if (Constants.EXPENSE_STATUS_PAID.equals(status)) {
                paidCount++;
                paidAmount += safeAmount;
            } else if (Constants.EXPENSE_STATUS_REJECTED.equals(status)) {
                rejectedCount++;
                rejectedAmount += safeAmount;
            }
        }

        builder.append(String.format(Locale.US,
                "AdminId=%s\nProjectCount=%d\nExpenseCount=%d\nTotalBudget=%.2f\nTotalTrackedNonRejected=%.2f\n",
                adminId,
                projects.size(),
                allExpenses.size(),
                totalBudget,
                trackedAmount));

        builder.append(String.format(Locale.US,
                "LifecycleTotals: Draft=%d(%.2f), Pending=%d(%.2f), Approved=%d(%.2f), Paid=%d(%.2f), Rejected=%d(%.2f)\n",
                draftCount,
                draftAmount,
                pendingCount,
                pendingAmount,
                approvedCount,
                approvedAmount,
                paidCount,
                paidAmount,
                rejectedCount,
                rejectedAmount));

        builder.append("\nProjects:\n");
        for (Project project : projects) {
            double spent = spentByProject.containsKey(project.getId())
                    ? spentByProject.get(project.getId())
                    : 0.0;
            double remaining = Math.max(0.0, project.getBudget() - spent);

            builder.append(String.format(Locale.US,
                    "- ProjectId=%s | Name=%s | Status=%s | Manager=%s | Budget=%.2f | Spent=%.2f | Remaining=%.2f\n",
                    project.getId(),
                    sanitize(project.getProjectName()),
                    sanitize(project.getStatus()),
                    sanitize(project.getManager()),
                    Math.max(0.0, project.getBudget()),
                    Math.max(0.0, spent),
                    remaining));
        }

        builder.append("\nRecentExpenses:\n");
        int appendedRows = 0;
        for (Expense expense : allExpenses) {
            if (!visibleProjectIds.contains(expense.getProjectId())) {
                continue;
            }
            if (appendedRows >= MAX_CONTEXT_EXPENSE_ROWS) {
                builder.append("- ...trimmed remaining expenses for token safety...\n");
                break;
            }

            builder.append(String.format(Locale.US,
                    "- ExpenseId=%s | ProjectId=%s | Title=%s | Status=%s | Amount=%.2f %s | DateMs=%d\n",
                    expense.getId(),
                    expense.getProjectId(),
                    sanitize(expense.getTitle()),
                    sanitize(expense.getStatus()),
                    Math.max(0.0, expense.getAmount()),
                    sanitize(expense.getCurrency()),
                    expense.getDate()));
            appendedRows++;
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace('|', '/').trim();
    }

    private void appendMessageAndPersist(AiMessage message) {
        adapter.addMessage(message);
        persistConversationHistory();
    }

    private void loadConversationHistory() {
        String raw = historyPreferences.getString(getHistoryPreferenceKey(), null);
        if (TextUtils.isEmpty(raw)) {
            adapter.setItems(new ArrayList<>());
            appendMessageAndPersist(new AiMessage(AiMessage.ROLE_ASSISTANT, getString(R.string.message_ai_welcome)));
            return;
        }

        List<AiMessage> restored = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                int role = item.optInt("role", AiMessage.ROLE_ASSISTANT);
                String content = item.optString("content", "").trim();
                long createdAt = item.optLong("createdAt", System.currentTimeMillis());
                if (TextUtils.isEmpty(content)) {
                    continue;
                }
                restored.add(new AiMessage(role, content, createdAt));
            }
        } catch (Exception ignored) {
            restored.clear();
        }

        if (restored.isEmpty()) {
            restored.add(new AiMessage(AiMessage.ROLE_ASSISTANT, getString(R.string.message_ai_welcome)));
        }

        adapter.setItems(restored);
        persistConversationHistory();
        scrollToBottom();
    }

    private void persistConversationHistory() {
        List<AiMessage> snapshot = adapter.getItemsSnapshot();
        int start = Math.max(0, snapshot.size() - MAX_SAVED_MESSAGES);
        JSONArray array = new JSONArray();
        for (int i = start; i < snapshot.size(); i++) {
            AiMessage message = snapshot.get(i);
            JSONObject item = new JSONObject();
            try {
                item.put("role", message.getRole());
                item.put("content", trimMessageContent(message.getContent()));
                item.put("createdAt", message.getCreatedAt());
                array.put(item);
            } catch (Exception ignored) {
                // Ignore malformed message item.
            }
        }
        historyPreferences.edit().putString(getHistoryPreferenceKey(), array.toString()).apply();
    }

    private String getHistoryPreferenceKey() {
        String accountId = TextUtils.isEmpty(currentAdminId) ? "anonymous" : currentAdminId;
        return HISTORY_KEY_PREFIX + accountId;
    }

    private String trimMessageContent(String content) {
        if (content == null) {
            return "";
        }
        if (content.length() <= MAX_MESSAGE_CONTENT_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_MESSAGE_CONTENT_LENGTH);
    }

    private String buildFallbackAssistantMessage(String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            return getString(R.string.message_ai_fallback_generic);
        }

        String lower = errorMessage.toLowerCase(Locale.US);
        if (lower.contains("no internet") || lower.contains("network")) {
            return getString(R.string.message_ai_fallback_network);
        }
        if (lower.contains("rate-limited") || lower.contains("resource_exhausted") || lower.contains("quota")) {
            return getString(R.string.message_ai_fallback_rate_limit, errorMessage);
        }
        if (lower.contains("authentication") || lower.contains("api key") || lower.contains("403") || lower.contains("401")) {
            return getString(R.string.message_ai_fallback_auth);
        }
        return getString(R.string.message_ai_fallback_generic_with_error, errorMessage);
    }

    private void showClearHistoryConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_clear_chat_history)
                .setMessage(R.string.message_clear_chat_history)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_clear, (dialog, which) -> clearConversationHistory())
                .show();
    }

    private void clearConversationHistory() {
        historyPreferences.edit().remove(getHistoryPreferenceKey()).apply();
        adapter.setItems(new ArrayList<>());
        appendMessageAndPersist(new AiMessage(AiMessage.ROLE_ASSISTANT, getString(R.string.message_ai_welcome)));
        inputMessage.setText("");
        setLoading(false);
        scrollToBottom();
        Toast.makeText(this, R.string.message_ai_history_cleared, Toast.LENGTH_SHORT).show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        sendButton.setEnabled(!isLoading);
        inputMessage.setEnabled(!isLoading);
        statusText.setText(isLoading ? R.string.label_ai_thinking : R.string.label_ai_ready);
    }

    private void scrollToBottom() {
        RecyclerView recyclerView = findViewById(R.id.recycler_ai_messages);
        recyclerView.post(() -> {
            if (adapter.getItemCount() > 0) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }
}

