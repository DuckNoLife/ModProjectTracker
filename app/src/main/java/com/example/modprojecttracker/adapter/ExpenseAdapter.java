package com.example.modprojecttracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseActionListener {
        void onOpen(Expense expense);

        void onEdit(Expense expense);

        void onDelete(Expense expense);
    }

    private final List<Expense> items = new ArrayList<>();
    private final OnExpenseActionListener listener;

    public ExpenseAdapter(OnExpenseActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Expense> expenses) {
        items.clear();
        if (expenses != null) {
            items.addAll(expenses);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_card, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView claimant;
        private final TextView title;
        private final TextView amount;
        private final TextView meta;
        private final TextView description;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_expense_title);
            claimant = itemView.findViewById(R.id.text_expense_claimant);
            amount = itemView.findViewById(R.id.text_expense_amount);
            meta = itemView.findViewById(R.id.text_expense_meta);
            description = itemView.findViewById(R.id.text_expense_desc);
            editButton = itemView.findViewById(R.id.button_edit_expense);
            deleteButton = itemView.findViewById(R.id.button_delete_expense);
        }

        void bind(Expense expense, OnExpenseActionListener listener) {
            title.setText(safeText(expense.getTitle()));
            claimant.setText(itemView.getContext().getString(R.string.label_expense_claimant, safeText(expense.getClaimant())));
            
            String amountDisplay = String.format(Locale.US, "%.2f %s", expense.getAmount(), expense.getCurrency());
            amount.setText(itemView.getContext().getString(R.string.label_expense_amount, 0.0).replace("$0.00", amountDisplay));

            meta.setText(String.format(Locale.US, "%s • %s • %s",
                    itemView.getContext().getString(R.string.label_expense_date, DateUtil.formatDate(expense.getDate())),
                    itemView.getContext().getString(R.string.label_expense_type, safeText(expense.getType())),
                    safeText(expense.getStatus())));
            description.setText(safeText(expense.getDescription()));

            itemView.setOnClickListener(v -> listener.onOpen(expense));
            editButton.setOnClickListener(v -> listener.onEdit(expense));
            deleteButton.setOnClickListener(v -> listener.onDelete(expense));
        }

        private String safeText(String text) {
            return text == null || text.trim().isEmpty() ? "-" : text.trim();
        }
    }
}

