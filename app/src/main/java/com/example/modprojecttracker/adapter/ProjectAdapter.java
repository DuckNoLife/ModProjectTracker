package com.example.modprojecttracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.util.DateUtil;
import com.example.modprojecttracker.util.Constants;
import com.example.modprojecttracker.util.ProjectImageLoader;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectActionListener {
        void onOpen(Project project);
        void onEdit(Project project);
        void onDelete(Project project);
    }

    private final List<Project> items = new ArrayList<>();
    private final OnProjectActionListener listener;
    private String currentAdminId;

    public ProjectAdapter(OnProjectActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Project> projects) {
        items.clear();
        if (projects != null) {
            items.addAll(projects);
        }
        notifyDataSetChanged();
    }

    public void setCurrentAdminId(String currentAdminId) {
        this.currentAdminId = currentAdminId;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_card, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = items.get(position);
        holder.bind(project, currentAdminId, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView coverImage;
        private final TextView placeholder;
        private final View accentView;
        private final TextView title;
        private final TextView statusBadge;
        private final TextView accessBadge;
        private final TextView description;
        private final TextView budget;
        private final TextView dateRange;
        private final TextView manager;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_project);
            coverImage = itemView.findViewById(R.id.image_project_cover);
            placeholder = itemView.findViewById(R.id.text_project_placeholder);
            accentView = itemView.findViewById(R.id.view_project_accent);
            title = itemView.findViewById(R.id.text_project_title);
            description = itemView.findViewById(R.id.text_project_description);
            statusBadge = itemView.findViewById(R.id.text_project_status_badge);
            accessBadge = itemView.findViewById(R.id.text_project_access_badge);
            budget = itemView.findViewById(R.id.text_project_budget);
            dateRange = itemView.findViewById(R.id.text_project_dates);
            manager = itemView.findViewById(R.id.text_project_manager);
            editButton = itemView.findViewById(R.id.button_edit_project);
            deleteButton = itemView.findViewById(R.id.button_delete_project);
        }

        void bind(Project project, String currentAdminId, OnProjectActionListener listener) {
            title.setText(project.getProjectName());
            description.setText(project.getDescription());
            statusBadge.setText(project.getStatus());
            budget.setText(String.format(Locale.US, "Budget: $%.2f", project.getBudget()));
            dateRange.setText(String.format(Locale.US, "%s → %s",
                    DateUtil.formatDate(project.getStartDate()),
                    DateUtil.formatDate(project.getEndDate())));
            manager.setText(String.format(Locale.US, "Manager: %s", project.getManager()));

            int statusColor;
            if (Constants.PROJECT_STATUS_COMPLETED.equals(project.getStatus())) {
                statusColor = itemView.getContext().getColor(R.color.project_completed);
            } else if (Constants.PROJECT_STATUS_ON_HOLD.equals(project.getStatus())) {
                statusColor = itemView.getContext().getColor(R.color.project_hold);
            } else {
                statusColor = itemView.getContext().getColor(R.color.project_active);
            }
            accentView.setBackgroundColor(statusColor);
            cardView.setStrokeColor(statusColor);
            statusBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));

            ProjectImageLoader.load(project.getImageUrl(), coverImage, placeholder);

            boolean isOwner = currentAdminId != null && currentAdminId.equals(project.getOwnerId());
            accessBadge.setText(isOwner ? itemView.getContext().getString(R.string.badge_owner)
                    : itemView.getContext().getString(R.string.badge_read_only));
            int accessColor = isOwner
                    ? itemView.getContext().getColor(R.color.neon_purple)
                    : itemView.getContext().getColor(R.color.project_hold);
            accessBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accessColor));

            cardView.setOnClickListener(v -> listener.onOpen(project));
            editButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            deleteButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);

            if (isOwner) {
                editButton.setOnClickListener(v -> listener.onEdit(project));
                deleteButton.setOnClickListener(v -> listener.onDelete(project));
            } else {
                editButton.setOnClickListener(null);
                deleteButton.setOnClickListener(null);
            }
        }

    }
}


