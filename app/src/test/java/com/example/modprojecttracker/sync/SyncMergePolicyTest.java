package com.example.modprojecttracker.sync;

import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.model.Project;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SyncMergePolicyTest {

    @Test
    public void shouldPushLocal_whenRemoteMissing_afterOfflineCreate() {
        Project local = project(200L);

        assertTrue(SyncMergePolicy.shouldPushLocal(local.getLastModified(), (Project) null));
    }

    @Test
    public void shouldPushLocal_whenLocalIsNewer_afterOfflineUpdate() {
        Project local = project(300L);
        Project remote = project(200L);

        assertTrue(SyncMergePolicy.shouldPushLocal(local.getLastModified(), remote));
    }

    @Test
    public void shouldPullRemote_whenCloudIsNewer_afterReconnect() {
        Project local = project(200L);
        Project remote = project(350L);

        assertFalse(SyncMergePolicy.shouldPushLocal(local.getLastModified(), remote));
    }

    @Test
    public void shouldPushLocal_tombstone_whenOfflineDeleteIsNewerThanCloudCopy() {
        Expense localTombstone = expense(500L);
        Expense remote = expense(400L);

        assertTrue(SyncMergePolicy.shouldPushLocal(localTombstone.getLastModified(), remote));
    }

    private Project project(long lastModified) {
        Project project = new Project();
        project.setId("p1");
        project.setLastModified(lastModified);
        return project;
    }

    private Expense expense(long lastModified) {
        Expense expense = new Expense();
        expense.setId("e1");
        expense.setLastModified(lastModified);
        return expense;
    }
}



