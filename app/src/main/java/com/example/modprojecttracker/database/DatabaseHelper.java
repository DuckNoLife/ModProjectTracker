package com.example.modprojecttracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.modprojecttracker.model.Expense;
import com.example.modprojecttracker.model.Project;
import com.example.modprojecttracker.model.User;
import com.example.modprojecttracker.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteOpenHelper for local users, projects, and expenses tables.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = Constants.DATABASE_NAME;
    private static final int DATABASE_VERSION = Constants.DATABASE_VERSION;

    private static volatile DatabaseHelper INSTANCE;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Constants.TABLE_USERS + " ("
                + Constants.COLUMN_USER_UID + " TEXT PRIMARY KEY, "
                + Constants.COLUMN_USER_USERNAME + " TEXT NOT NULL UNIQUE, "
                + Constants.COLUMN_USER_PASSWORD + " TEXT NOT NULL, "
                + Constants.COLUMN_USER_NAME + " TEXT NOT NULL, "
                + Constants.COLUMN_USER_ROLE + " TEXT NOT NULL, "
                + Constants.COLUMN_USER_CREATED_AT + " INTEGER NOT NULL"
                + ");");

        db.execSQL("CREATE TABLE " + Constants.TABLE_PROJECTS + " ("
                + Constants.COLUMN_PROJECT_ID + " TEXT PRIMARY KEY, "
                + Constants.COLUMN_PROJECT_NAME + " TEXT NOT NULL, "
                + Constants.COLUMN_PROJECT_DESCRIPTION + " TEXT NOT NULL, "
                + Constants.COLUMN_PROJECT_START_DATE + " INTEGER NOT NULL, "
                + Constants.COLUMN_PROJECT_END_DATE + " INTEGER NOT NULL, "
                + Constants.COLUMN_PROJECT_MANAGER + " TEXT NOT NULL, "
                + Constants.COLUMN_PROJECT_STATUS + " TEXT NOT NULL, "
                + Constants.COLUMN_PROJECT_BUDGET + " REAL NOT NULL, "
                + Constants.COLUMN_PROJECT_SPECIAL_REQ + " TEXT, "
                + Constants.COLUMN_PROJECT_CLIENT_INFO + " TEXT, "
                + Constants.COLUMN_PROJECT_IMAGE_URL + " TEXT, "
                + Constants.COLUMN_PROJECT_OWNER_ID + " TEXT NOT NULL, "
                + Constants.COLUMN_PROJECT_IS_FAVORITE + " INTEGER NOT NULL DEFAULT 0, "
                + Constants.COLUMN_PROJECT_LAST_MODIFIED + " INTEGER NOT NULL, "
                + Constants.COLUMN_PROJECT_IS_DELETED + " INTEGER NOT NULL DEFAULT 0, "
                + Constants.COLUMN_PROJECT_DELETED_AT + " INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY(" + Constants.COLUMN_PROJECT_OWNER_ID + ") REFERENCES " + Constants.TABLE_USERS + "(" + Constants.COLUMN_USER_UID + ") ON DELETE CASCADE"
                + ");");

        db.execSQL("CREATE TABLE " + Constants.TABLE_EXPENSES + " ("
                + Constants.COLUMN_EXPENSE_ID + " TEXT PRIMARY KEY, "
                + Constants.COLUMN_EXPENSE_PROJECT_ID + " TEXT NOT NULL, "
                + Constants.COLUMN_EXPENSE_TITLE + " TEXT NOT NULL, "
                + Constants.COLUMN_EXPENSE_DATE + " INTEGER NOT NULL, "
                + Constants.COLUMN_EXPENSE_AMOUNT + " REAL NOT NULL, "
                + Constants.COLUMN_EXPENSE_CURRENCY + " TEXT NOT NULL, "
                + Constants.COLUMN_EXPENSE_TYPE + " TEXT NOT NULL, "
                + Constants.COLUMN_EXPENSE_PAYMENT_METHOD + " TEXT NOT NULL, "
                + Constants.COLUMN_EXPENSE_CLAIMANT + " TEXT NOT NULL, "
                + Constants.COLUMN_EXPENSE_STATUS + " TEXT NOT NULL, "
                + Constants.COLUMN_EXPENSE_DESCRIPTION + " TEXT, "
                + Constants.COLUMN_EXPENSE_LOCATION + " TEXT, "
                + Constants.COLUMN_EXPENSE_LAST_MODIFIED + " INTEGER NOT NULL, "
                + Constants.COLUMN_EXPENSE_IS_DELETED + " INTEGER NOT NULL DEFAULT 0, "
                + Constants.COLUMN_EXPENSE_DELETED_AT + " INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY(" + Constants.COLUMN_EXPENSE_PROJECT_ID + ") REFERENCES " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_PROJECT_ID + ") ON DELETE CASCADE"
                + ");");

        db.execSQL("CREATE INDEX idx_projects_ownerId ON " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_PROJECT_OWNER_ID + ");");
        db.execSQL("CREATE INDEX idx_projects_status ON " + Constants.TABLE_PROJECTS + "(" + Constants.COLUMN_PROJECT_STATUS + ");");
        db.execSQL("CREATE INDEX idx_expenses_projectId ON " + Constants.TABLE_EXPENSES + "(" + Constants.COLUMN_EXPENSE_PROJECT_ID + ");");
        db.execSQL("CREATE INDEX idx_expenses_status ON " + Constants.TABLE_EXPENSES + "(" + Constants.COLUMN_EXPENSE_STATUS + ");");
        db.execSQL("CREATE INDEX idx_expenses_claimant ON " + Constants.TABLE_EXPENSES + "(" + Constants.COLUMN_EXPENSE_CLAIMANT + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_EXPENSES);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PROJECTS);
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);
            onCreate(db);
            return;
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + Constants.TABLE_PROJECTS + " ADD COLUMN "
                    + Constants.COLUMN_PROJECT_IS_DELETED + " INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + Constants.TABLE_PROJECTS + " ADD COLUMN "
                    + Constants.COLUMN_PROJECT_DELETED_AT + " INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + Constants.TABLE_EXPENSES + " ADD COLUMN "
                    + Constants.COLUMN_EXPENSE_IS_DELETED + " INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + Constants.TABLE_EXPENSES + " ADD COLUMN "
                    + Constants.COLUMN_EXPENSE_DELETED_AT + " INTEGER NOT NULL DEFAULT 0");
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + Constants.TABLE_EXPENSES + " ADD COLUMN "
                    + Constants.COLUMN_EXPENSE_TITLE + " TEXT NOT NULL DEFAULT ''");
            db.execSQL("UPDATE " + Constants.TABLE_EXPENSES + " SET " + Constants.COLUMN_EXPENSE_STATUS + " = '" + Constants.EXPENSE_STATUS_PENDING_APPROVAL + "'"
                    + " WHERE " + Constants.COLUMN_EXPENSE_STATUS + " = 'Pending'");
            db.execSQL("UPDATE " + Constants.TABLE_EXPENSES + " SET " + Constants.COLUMN_EXPENSE_STATUS + " = '" + Constants.EXPENSE_STATUS_APPROVED + "'"
                    + " WHERE " + Constants.COLUMN_EXPENSE_STATUS + " = 'Reimbursed'");
        }
    }

    // User CRUD.

    public long insertUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = userToValues(user);
        return db.insert(Constants.TABLE_USERS, null, values);
    }

    public int updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = userToValues(user);
        return db.update(Constants.TABLE_USERS, values, Constants.COLUMN_USER_UID + " = ?", new String[]{user.getUid()});
    }

    public int deleteUser(String uid) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(Constants.TABLE_USERS, Constants.COLUMN_USER_UID + " = ?", new String[]{uid});
    }

    public User getUserById(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constants.TABLE_USERS, null, Constants.COLUMN_USER_UID + " = ?", new String[]{uid}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        }
    }

    public User getUserByEmail(String email) {
        return getUserByUsername(email);
    }

    public User getUserByUsername(String username) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constants.TABLE_USERS, null, Constants.COLUMN_USER_USERNAME + " = ?", new String[]{username}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        }
    }

    public User authenticateAdmin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_USER_USERNAME + " = ? AND "
                + Constants.COLUMN_USER_PASSWORD + " = ? AND "
                + Constants.COLUMN_USER_ROLE + " = ?";
        String[] args = new String[]{username, password, Constants.ROLE_ADMIN};
        try (Cursor cursor = db.query(Constants.TABLE_USERS, null, selection, args, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToUser(cursor);
            }
            return null;
        }
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + Constants.TABLE_USERS + " WHERE " + Constants.COLUMN_USER_USERNAME + " = ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{username})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constants.TABLE_USERS, null, null, null, null, null, Constants.COLUMN_USER_NAME + " ASC")) {
            while (cursor != null && cursor.moveToNext()) {
                users.add(cursorToUser(cursor));
            }
        }
        return users;
    }

    public boolean userExists(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_USERS + " WHERE " + Constants.COLUMN_USER_UID + " = ?", new String[]{uid})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
            return false;
        }
    }

    // Project CRUD.

    public long insertProject(Project project) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = projectToValues(project);
        return db.insert(Constants.TABLE_PROJECTS, null, values);
    }

    public int updateProject(Project project) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = projectToValues(project);
        return db.update(Constants.TABLE_PROJECTS, values, Constants.COLUMN_PROJECT_ID + " = ?", new String[]{project.getId()});
    }

    public int updateProjectAsOwner(Project project, String ownerId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = projectToValues(project);
        String where = Constants.COLUMN_PROJECT_ID + " = ? AND " + Constants.COLUMN_PROJECT_OWNER_ID + " = ?";
        String[] args = new String[]{project.getId(), ownerId};
        return db.update(Constants.TABLE_PROJECTS, values, where, args);
    }

    public int deleteProject(String id) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PROJECT_IS_DELETED, 1);
        values.put(Constants.COLUMN_PROJECT_DELETED_AT, now);
        values.put(Constants.COLUMN_PROJECT_LAST_MODIFIED, now);
        markExpensesDeletedByProject(id, now, null);
        return db.update(Constants.TABLE_PROJECTS, values, Constants.COLUMN_PROJECT_ID + " = ?", new String[]{id});
    }

    public int deleteProjectAsOwner(String id, String ownerId) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PROJECT_IS_DELETED, 1);
        values.put(Constants.COLUMN_PROJECT_DELETED_AT, now);
        values.put(Constants.COLUMN_PROJECT_LAST_MODIFIED, now);
        String where = Constants.COLUMN_PROJECT_ID + " = ? AND " + Constants.COLUMN_PROJECT_OWNER_ID + " = ?";
        String[] args = new String[]{id, ownerId};
        int updated = db.update(Constants.TABLE_PROJECTS, values, where, args);
        if (updated > 0) {
            markExpensesDeletedByProject(id, now, ownerId);
        }
        return updated;
    }

    public boolean isProjectOwner(String projectId, String ownerId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + Constants.TABLE_PROJECTS
                + " WHERE " + Constants.COLUMN_PROJECT_ID + " = ? AND " + Constants.COLUMN_PROJECT_OWNER_ID + " = ?";
        try (Cursor cursor = db.rawQuery(sql, new String[]{projectId, ownerId})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
            return false;
        }
    }

    public Project getProjectById(String id) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_PROJECT_ID + " = ? AND " + activeProjectSelection();
        try (Cursor cursor = db.query(Constants.TABLE_PROJECTS, null, selection, new String[]{id}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToProject(cursor);
            }
            return null;
        }
    }

    public List<Project> getProjectsByOwner(String ownerId) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_PROJECT_OWNER_ID + " = ? AND " + activeProjectSelection();
        try (Cursor cursor = db.query(Constants.TABLE_PROJECTS, null, selection, new String[]{ownerId}, null, null, Constants.COLUMN_PROJECT_LAST_MODIFIED + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                projects.add(cursorToProject(cursor));
            }
        }
        return projects;
    }

    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constants.TABLE_PROJECTS, null, activeProjectSelection(), null, null, null, Constants.COLUMN_PROJECT_LAST_MODIFIED + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                projects.add(cursorToProject(cursor));
            }
        }
        return projects;
    }

    public List<Project> getAllProjectsIncludingDeleted() {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constants.TABLE_PROJECTS, null, null, null, null, null, Constants.COLUMN_PROJECT_LAST_MODIFIED + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                projects.add(cursorToProject(cursor));
            }
        }
        return projects;
    }

    public List<Project> getProjectsVisibleToAdmin(String adminUid) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT p.* FROM " + Constants.TABLE_PROJECTS + " p "
                + "INNER JOIN " + Constants.TABLE_USERS + " u "
                + "ON p." + Constants.COLUMN_PROJECT_OWNER_ID + " = u." + Constants.COLUMN_USER_UID + " "
                + "WHERE u." + Constants.COLUMN_USER_ROLE + " = ? "
                + "AND p." + Constants.COLUMN_PROJECT_IS_DELETED + " = 0 "
                + "ORDER BY p." + Constants.COLUMN_PROJECT_LAST_MODIFIED + " DESC";
        try (Cursor cursor = db.rawQuery(query, new String[]{Constants.ROLE_ADMIN})) {
            while (cursor != null && cursor.moveToNext()) {
                projects.add(cursorToProject(cursor));
            }
        }
        return projects;
    }

    public List<Project> searchProjectsByName(String searchTerm) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String like = "%" + searchTerm + "%";
        String selection = Constants.COLUMN_PROJECT_NAME + " LIKE ? COLLATE NOCASE AND " + activeProjectSelection();
        try (Cursor cursor = db.query(Constants.TABLE_PROJECTS, null, selection, new String[]{like}, null, null, Constants.COLUMN_PROJECT_NAME + " ASC")) {
            while (cursor != null && cursor.moveToNext()) {
                projects.add(cursorToProject(cursor));
            }
        }
        return projects;
    }

    public List<Project> getProjectsByStatus(String status) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_PROJECT_STATUS + " = ? AND " + activeProjectSelection();
        try (Cursor cursor = db.query(Constants.TABLE_PROJECTS, null, selection, new String[]{status}, null, null, Constants.COLUMN_PROJECT_LAST_MODIFIED + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                projects.add(cursorToProject(cursor));
            }
        }
        return projects;
    }

    public List<Project> getProjectsByDateRange(long startDate, long endDate) {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_PROJECT_START_DATE + " >= ? AND " + Constants.COLUMN_PROJECT_END_DATE + " <= ? AND " + activeProjectSelection();
        try (Cursor cursor = db.query(Constants.TABLE_PROJECTS, null, selection, new String[]{String.valueOf(startDate), String.valueOf(endDate)}, null, null, Constants.COLUMN_PROJECT_START_DATE + " ASC")) {
            while (cursor != null && cursor.moveToNext()) {
                projects.add(cursorToProject(cursor));
            }
        }
        return projects;
    }

    public List<Project> getFavoriteProjects() {
        List<Project> projects = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_PROJECT_IS_FAVORITE + " = 1";
        selection += " AND " + activeProjectSelection();
        try (Cursor cursor = db.query(Constants.TABLE_PROJECTS, null, selection, null, null, null, Constants.COLUMN_PROJECT_NAME + " ASC")) {
            while (cursor != null && cursor.moveToNext()) {
                projects.add(cursorToProject(cursor));
            }
        }
        return projects;
    }

    public int projectExists(String id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_PROJECTS + " WHERE " + Constants.COLUMN_PROJECT_ID + " = ?", new String[]{id})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }
    }

    public int getTotalProjectCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_PROJECTS
                + " WHERE " + Constants.COLUMN_PROJECT_IS_DELETED + " = 0", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }
    }

    public void upsertProject(Project project) {
        if (project == null || project.getId() == null) {
            return;
        }
        ensureOwnerUserExists(project.getOwnerId());
        int exists = projectExists(project.getId());
        if (exists > 0) {
            updateProject(project);
        } else {
            insertProject(project);
        }
        if (project.isDeleted()) {
            long deletedTimestamp = project.getLastModified() > 0 ? project.getLastModified() : System.currentTimeMillis();
            markExpensesDeletedByProject(project.getId(), deletedTimestamp, null);
        }
    }

    // Expense CRUD.

    public long insertExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = expenseToValues(expense);
        return db.insert(Constants.TABLE_EXPENSES, null, values);
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = expenseToValues(expense);
        return db.update(Constants.TABLE_EXPENSES, values, Constants.COLUMN_EXPENSE_ID + " = ?", new String[]{expense.getId()});
    }

    public int deleteExpense(String id) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_EXPENSE_IS_DELETED, 1);
        values.put(Constants.COLUMN_EXPENSE_DELETED_AT, now);
        values.put(Constants.COLUMN_EXPENSE_LAST_MODIFIED, now);
        return db.update(Constants.TABLE_EXPENSES, values, Constants.COLUMN_EXPENSE_ID + " = ?", new String[]{id});
    }

    public int deleteExpensesByProjectId(String projectId) {
        return markExpensesDeletedByProject(projectId, System.currentTimeMillis(), null);
    }

    public int deleteExpensesByProjectIdAsOwner(String projectId, String ownerId) {
        if (!isProjectOwner(projectId, ownerId)) {
            return 0;
        }
        return markExpensesDeletedByProject(projectId, System.currentTimeMillis(), ownerId);
    }

    public Expense getExpenseById(String id) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_EXPENSE_ID + " = ? AND " + activeExpenseSelection();
        try (Cursor cursor = db.query(Constants.TABLE_EXPENSES, null, selection, new String[]{id}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToExpense(cursor);
            }
            return null;
        }
    }

    public List<Expense> getExpensesByProject(String projectId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_EXPENSE_PROJECT_ID + " = ? AND " + activeExpenseSelection();
        try (Cursor cursor = db.query(Constants.TABLE_EXPENSES, null, selection, new String[]{projectId}, null, null, Constants.COLUMN_EXPENSE_DATE + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                expenses.add(cursorToExpense(cursor));
            }
        }
        return expenses;
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constants.TABLE_EXPENSES, null, activeExpenseSelection(), null, null, null, Constants.COLUMN_EXPENSE_DATE + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                expenses.add(cursorToExpense(cursor));
            }
        }
        return expenses;
    }

    public List<Expense> getAllExpensesIncludingDeleted() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(Constants.TABLE_EXPENSES, null, null, null, null, null, Constants.COLUMN_EXPENSE_DATE + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                expenses.add(cursorToExpense(cursor));
            }
        }
        return expenses;
    }

    public List<Expense> getExpensesByStatus(String status) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_EXPENSE_STATUS + " = ? AND " + activeExpenseSelection();
        try (Cursor cursor = db.query(Constants.TABLE_EXPENSES, null, selection, new String[]{status}, null, null, Constants.COLUMN_EXPENSE_DATE + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                expenses.add(cursorToExpense(cursor));
            }
        }
        return expenses;
    }

    public List<Expense> getPendingExpenses() {
        return getExpensesByStatus(Constants.EXPENSE_STATUS_PENDING_APPROVAL);
    }

    public List<Expense> getExpensesByDateRange(long startDate, long endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_EXPENSE_DATE + " >= ? AND " + Constants.COLUMN_EXPENSE_DATE + " <= ? AND " + activeExpenseSelection();
        try (Cursor cursor = db.query(Constants.TABLE_EXPENSES, null, selection, new String[]{String.valueOf(startDate), String.valueOf(endDate)}, null, null, Constants.COLUMN_EXPENSE_DATE + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                expenses.add(cursorToExpense(cursor));
            }
        }
        return expenses;
    }

    public List<Expense> getExpensesByType(String type) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection = Constants.COLUMN_EXPENSE_TYPE + " = ? AND " + activeExpenseSelection();
        try (Cursor cursor = db.query(Constants.TABLE_EXPENSES, null, selection, new String[]{type}, null, null, Constants.COLUMN_EXPENSE_DATE + " DESC")) {
            while (cursor != null && cursor.moveToNext()) {
                expenses.add(cursorToExpense(cursor));
            }
        }
        return expenses;
    }

    public double getTotalExpenseByProject(String projectId) {
        List<Expense> expenses = getExpensesByProject(projectId);
        double totalUsd = 0;
        for (Expense e : expenses) {
            if (!Constants.EXPENSE_STATUS_REJECTED.equals(e.getStatus())) {
                totalUsd += com.example.modprojecttracker.util.CurrencyUtil.convertToUsd(e.getAmount(), e.getCurrency());
            }
        }
        return totalUsd;
    }

    public int expenseExists(String id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_EXPENSES + " WHERE " + Constants.COLUMN_EXPENSE_ID + " = ?", new String[]{id})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }
    }

    public int getPendingExpenseCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + Constants.TABLE_EXPENSES
                + " WHERE " + Constants.COLUMN_EXPENSE_IS_DELETED + " = 0 AND ("
                + Constants.COLUMN_EXPENSE_STATUS + " = ? OR "
                + Constants.COLUMN_EXPENSE_STATUS + " = ?)", new String[]{Constants.EXPENSE_STATUS_PENDING_APPROVAL, "Pending"})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }
    }

    public void upsertExpense(Expense expense) {
        if (expense == null || expense.getId() == null || expense.getProjectId() == null) {
            return;
        }
        if (projectExists(expense.getProjectId()) == 0) {
            // Project has not been synced yet; skip this expense until next sync round.
            return;
        }
        int exists = expenseExists(expense.getId());
        if (exists > 0) {
            updateExpense(expense);
        } else {
            insertExpense(expense);
        }
    }

    // Helpers.

    private ContentValues userToValues(User user) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_USER_UID, user.getUid());
        values.put(Constants.COLUMN_USER_USERNAME, user.getUsername());
        values.put(Constants.COLUMN_USER_PASSWORD, user.getPassword());
        values.put(Constants.COLUMN_USER_NAME, user.getName());
        values.put(Constants.COLUMN_USER_ROLE, user.getRole());
        values.put(Constants.COLUMN_USER_CREATED_AT, user.getCreatedAt() > 0 ? user.getCreatedAt() : System.currentTimeMillis());
        return values;
    }

    private ContentValues projectToValues(Project project) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PROJECT_ID, project.getId());
        values.put(Constants.COLUMN_PROJECT_NAME, project.getProjectName());
        values.put(Constants.COLUMN_PROJECT_DESCRIPTION, project.getDescription());
        values.put(Constants.COLUMN_PROJECT_START_DATE, project.getStartDate());
        values.put(Constants.COLUMN_PROJECT_END_DATE, project.getEndDate());
        values.put(Constants.COLUMN_PROJECT_MANAGER, project.getManager());
        values.put(Constants.COLUMN_PROJECT_STATUS, project.getStatus());
        values.put(Constants.COLUMN_PROJECT_BUDGET, project.getBudget());
        values.put(Constants.COLUMN_PROJECT_IMAGE_URL, project.getImageUrl());
        values.put(Constants.COLUMN_PROJECT_OWNER_ID, project.getOwnerId());
        values.put(Constants.COLUMN_PROJECT_IS_FAVORITE, project.isFavorite() ? 1 : 0);
        values.put(Constants.COLUMN_PROJECT_LAST_MODIFIED, project.getLastModified() > 0 ? project.getLastModified() : System.currentTimeMillis());
        values.put(Constants.COLUMN_PROJECT_IS_DELETED, project.isDeleted() ? 1 : 0);
        values.put(Constants.COLUMN_PROJECT_DELETED_AT, project.getDeletedAt());
        return values;
    }

    private ContentValues expenseToValues(Expense expense) {
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_EXPENSE_ID, expense.getId());
        values.put(Constants.COLUMN_EXPENSE_PROJECT_ID, expense.getProjectId());
        values.put(Constants.COLUMN_EXPENSE_TITLE, safeExpenseTitle(expense.getTitle()));
        values.put(Constants.COLUMN_EXPENSE_DATE, expense.getDate());
        values.put(Constants.COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(Constants.COLUMN_EXPENSE_CURRENCY, expense.getCurrency());
        values.put(Constants.COLUMN_EXPENSE_TYPE, expense.getType());
        values.put(Constants.COLUMN_EXPENSE_PAYMENT_METHOD, expense.getPaymentMethod());
        values.put(Constants.COLUMN_EXPENSE_CLAIMANT, expense.getClaimant());
        values.put(Constants.COLUMN_EXPENSE_STATUS, normalizeExpenseStatus(expense.getStatus()));
        values.put(Constants.COLUMN_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(Constants.COLUMN_EXPENSE_LOCATION, expense.getLocation());
        values.put(Constants.COLUMN_EXPENSE_LAST_MODIFIED, expense.getLastModified() > 0 ? expense.getLastModified() : System.currentTimeMillis());
        values.put(Constants.COLUMN_EXPENSE_IS_DELETED, expense.isDeleted() ? 1 : 0);
        values.put(Constants.COLUMN_EXPENSE_DELETED_AT, expense.getDeletedAt());
        return values;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUid(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_UID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_USERNAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_PASSWORD)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_NAME)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_ROLE)));
        user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_USER_CREATED_AT)));
        return user;
    }

    private Project cursorToProject(Cursor cursor) {
        Project project = new Project();
        project.setId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_ID)));
        project.setProjectName(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_NAME)));
        project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_DESCRIPTION)));
        project.setStartDate(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_START_DATE)));
        project.setEndDate(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_END_DATE)));
        project.setManager(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_MANAGER)));
        project.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_STATUS)));
        project.setBudget(cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_BUDGET)));
        project.setImageUrl(getNullableString(cursor, Constants.COLUMN_PROJECT_IMAGE_URL));
        project.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_OWNER_ID)));
        project.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_IS_FAVORITE)) == 1);
        project.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_LAST_MODIFIED)));
        project.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_IS_DELETED)) == 1);
        project.setDeletedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_PROJECT_DELETED_AT)));
        return project;
    }

    private Expense cursorToExpense(Cursor cursor) {
        Expense expense = new Expense();
        expense.setId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_ID)));
        expense.setProjectId(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_PROJECT_ID)));
        expense.setTitle(getNullableString(cursor, Constants.COLUMN_EXPENSE_TITLE));
        expense.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_DATE)));
        expense.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_AMOUNT)));
        expense.setCurrency(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_CURRENCY)));
        expense.setType(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_TYPE)));
        expense.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_PAYMENT_METHOD)));
        expense.setClaimant(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_CLAIMANT)));
        expense.setStatus(normalizeExpenseStatus(cursor.getString(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_STATUS))));
        expense.setDescription(getNullableString(cursor, Constants.COLUMN_EXPENSE_DESCRIPTION));
        expense.setLocation(getNullableString(cursor, Constants.COLUMN_EXPENSE_LOCATION));
        expense.setLastModified(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_LAST_MODIFIED)));
        expense.setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_IS_DELETED)) == 1);
        expense.setDeletedAt(cursor.getLong(cursor.getColumnIndexOrThrow(Constants.COLUMN_EXPENSE_DELETED_AT)));
        return expense;
    }

    private String activeProjectSelection() {
        return Constants.COLUMN_PROJECT_IS_DELETED + " = 0";
    }

    private String activeExpenseSelection() {
        return Constants.COLUMN_EXPENSE_IS_DELETED + " = 0";
    }

    private String normalizeExpenseStatus(@Nullable String status) {
        if (status == null || status.trim().isEmpty()) {
            return Constants.EXPENSE_STATUS_DRAFT;
        }
        String trimmed = status.trim();
        if ("Pending".equalsIgnoreCase(trimmed)) {
            return Constants.EXPENSE_STATUS_PENDING_APPROVAL;
        }
        if ("Reimbursed".equalsIgnoreCase(trimmed)) {
            return Constants.EXPENSE_STATUS_APPROVED;
        }
        return trimmed;
    }

    private String safeExpenseTitle(@Nullable String title) {
        return title == null ? "" : title.trim();
    }

    private int markExpensesDeletedByProject(String projectId, long timestamp, @Nullable String ownerId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_EXPENSE_IS_DELETED, 1);
        values.put(Constants.COLUMN_EXPENSE_DELETED_AT, timestamp);
        values.put(Constants.COLUMN_EXPENSE_LAST_MODIFIED, timestamp);

        String where = Constants.COLUMN_EXPENSE_PROJECT_ID + " = ?";
        List<String> args = new ArrayList<>();
        args.add(projectId);
        if (ownerId != null) {
            where += " AND EXISTS (SELECT 1 FROM " + Constants.TABLE_PROJECTS + " p"
                    + " WHERE p." + Constants.COLUMN_PROJECT_ID + " = " + Constants.TABLE_EXPENSES + "." + Constants.COLUMN_EXPENSE_PROJECT_ID
                    + " AND p." + Constants.COLUMN_PROJECT_OWNER_ID + " = ?)";
            args.add(ownerId);
        }
        return db.update(Constants.TABLE_EXPENSES, values, where, args.toArray(new String[0]));
    }

    private String getNullableString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index == -1 || cursor.isNull(index)) {
            return null;
        }
        return cursor.getString(index);
    }

    private void ensureOwnerUserExists(String ownerId) {
        if (ownerId == null || ownerId.trim().isEmpty() || userExists(ownerId)) {
            return;
        }
        User user = new User();
        user.setUid(ownerId);
        user.setUsername("remote_" + ownerId);
        user.setPassword("remote_sync");
        user.setName("Remote Admin");
        user.setRole(Constants.ROLE_ADMIN);
        user.setCreatedAt(System.currentTimeMillis());
        insertUser(user);
    }
}

