package com.example.modprojecttracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.modprojecttracker.R;
import com.example.modprojecttracker.model.AiMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AiMessageAdapter extends RecyclerView.Adapter<AiMessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_ASSISTANT = 2;

    private final List<AiMessage> items = new ArrayList<>();

    public void addMessage(@NonNull AiMessage message) {
        items.add(message);
        notifyItemInserted(items.size() - 1);
    }

    public void setItems(@NonNull List<AiMessage> messages) {
        items.clear();
        items.addAll(messages);
        notifyDataSetChanged();
    }

    @NonNull
    public List<AiMessage> getItemsSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getRole() == AiMessage.ROLE_USER ? VIEW_TYPE_USER : VIEW_TYPE_ASSISTANT;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = viewType == VIEW_TYPE_USER
                ? R.layout.item_ai_message_user
                : R.layout.item_ai_message_assistant;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageText;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_ai_message);
        }

        void bind(@NonNull AiMessage message) {
            messageText.setText(message.getContent());
        }
    }
}

