package com.example.modprojecttracker.sync;

import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.model.Project;

/**
 * Pure merge policy used by sync logic and JVM tests.
 */
public final class SyncMergePolicy {

    private SyncMergePolicy() {
    }

    public static boolean shouldPushLocal(long localLastModified, Project remote) {
        if (remote == null) {
            return true;
        }
        return localLastModified >= remote.getLastModified();
    }

    public static boolean shouldPushLocal(long localLastModified, Expense remote) {
        if (remote == null) {
            return true;
        }
        return localLastModified >= remote.getLastModified();
    }
}


