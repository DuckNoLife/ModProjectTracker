package com.example.modprojecttracker.sync;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.modprojecttracker.database.DatabaseHelper;
import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.util.Constants;
import com.example.modprojecttracker.util.NetworkUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncCoordinator {

    public interface SyncCallback {
        void onSuccess();

        void onError(String message);
    }

    public static void syncNow(@NonNull Context context, @NonNull SyncCallback callback) {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            callback.onError("No internet. Data stays local and will sync later.");
            return;
        }

        DatabaseHelper db = DatabaseHelper.getInstance(context);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        root.child(Constants.FIREBASE_PATH_PROJECTS).get()
                .addOnSuccessListener(projectSnapshot -> {
                    try {
                        mergeProjects(db, root.child(Constants.FIREBASE_PATH_PROJECTS), projectSnapshot);
                        root.child(Constants.FIREBASE_PATH_EXPENSES).get()
                                .addOnSuccessListener(expenseSnapshot -> {
                                    try {
                                        mergeExpenses(db, root.child(Constants.FIREBASE_PATH_EXPENSES), expenseSnapshot);
                                        callback.onSuccess();
                                    } catch (Exception ex) {
                                        callback.onError(ex.getMessage() != null ? ex.getMessage() : "Expense sync failed");
                                    }
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "Expense sync failed"));
                    } catch (Exception ex) {
                        callback.onError(ex.getMessage() != null ? ex.getMessage() : "Project sync failed");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null ? e.getMessage() : "Project sync failed"));
    }

    private static void mergeProjects(DatabaseHelper db, DatabaseReference cloudProjectsRef, DataSnapshot cloudSnapshot) {
        Map<String, Project> remote = new HashMap<>();
        for (DataSnapshot child : cloudSnapshot.getChildren()) {
            Project project = child.getValue(Project.class);
            if (project != null && project.getId() != null) {
                remote.put(project.getId(), project);
            }
        }

        List<Project> localProjects = db.getAllProjectsIncludingDeleted();
        for (Project local : localProjects) {
            Project cloud = remote.remove(local.getId());
            if (SyncMergePolicy.shouldPushLocal(local.getLastModified(), cloud)) {
                cloudProjectsRef.child(local.getId()).setValue(local);
            } else {
                db.upsertProject(cloud);
            }
        }

        for (Project cloudOnly : remote.values()) {
            db.upsertProject(cloudOnly);
        }
    }

    private static void mergeExpenses(DatabaseHelper db, DatabaseReference cloudExpensesRef, DataSnapshot cloudSnapshot) {
        Map<String, Expense> remote = new HashMap<>();
        for (DataSnapshot child : cloudSnapshot.getChildren()) {
            Expense expense = child.getValue(Expense.class);
            if (expense != null && expense.getId() != null) {
                remote.put(expense.getId(), expense);
            }
        }

        List<Expense> localExpenses = db.getAllExpensesIncludingDeleted();
        for (Expense local : localExpenses) {
            Expense cloud = remote.remove(local.getId());
            if (SyncMergePolicy.shouldPushLocal(local.getLastModified(), cloud)) {
                cloudExpensesRef.child(local.getId()).setValue(local);
            } else {
                db.upsertExpense(cloud);
            }
        }

        for (Expense cloudOnly : remote.values()) {
            db.upsertExpense(cloudOnly);
        }
    }
}

