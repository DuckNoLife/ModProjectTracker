package com.example.modprojecttracker.util;

/** App-wide constants. */
public class Constants {

    // Database constants.
    public static final String DATABASE_NAME = "modtracker_db";
    public static final int DATABASE_VERSION = 4;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_EXPENSES = "expenses";

    public static final String COLUMN_USER_UID = "uid";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_ROLE = "role";
    public static final String COLUMN_USER_CREATED_AT = "createdAt";

    public static final String COLUMN_PROJECT_ID = "id";
    public static final String COLUMN_PROJECT_NAME = "projectName";
    public static final String COLUMN_PROJECT_DESCRIPTION = "description";
    public static final String COLUMN_PROJECT_START_DATE = "startDate";
    public static final String COLUMN_PROJECT_END_DATE = "endDate";
    public static final String COLUMN_PROJECT_MANAGER = "manager";
    public static final String COLUMN_PROJECT_STATUS = "status";
    public static final String COLUMN_PROJECT_BUDGET = "budget";
    public static final String COLUMN_PROJECT_SPECIAL_REQ = "specialReq";
    public static final String COLUMN_PROJECT_CLIENT_INFO = "clientInfo";
    public static final String COLUMN_PROJECT_IMAGE_URL = "imageUrl";
    public static final String COLUMN_PROJECT_OWNER_ID = "ownerId";
    public static final String COLUMN_PROJECT_IS_FAVORITE = "isFavorite";
    public static final String COLUMN_PROJECT_LAST_MODIFIED = "lastModified";
    public static final String COLUMN_PROJECT_IS_DELETED = "isDeleted";
    public static final String COLUMN_PROJECT_DELETED_AT = "deletedAt";

    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_EXPENSE_PROJECT_ID = "projectId";
    public static final String COLUMN_EXPENSE_TITLE = "title";
    public static final String COLUMN_EXPENSE_DATE = "date";
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_CURRENCY = "currency";
    public static final String COLUMN_EXPENSE_TYPE = "type";
    public static final String COLUMN_EXPENSE_PAYMENT_METHOD = "paymentMethod";
    public static final String COLUMN_EXPENSE_CLAIMANT = "claimant";
    public static final String COLUMN_EXPENSE_STATUS = "status";
    public static final String COLUMN_EXPENSE_DESCRIPTION = "description";
    public static final String COLUMN_EXPENSE_LOCATION = "location";
    public static final String COLUMN_EXPENSE_LAST_MODIFIED = "lastModified";
    public static final String COLUMN_EXPENSE_IS_DELETED = "isDeleted";
    public static final String COLUMN_EXPENSE_DELETED_AT = "deletedAt";

    // Project statuses.
    public static final String PROJECT_STATUS_ACTIVE = "Active";
    public static final String PROJECT_STATUS_COMPLETED = "Completed";
    public static final String PROJECT_STATUS_ON_HOLD = "On Hold";

    // Expense statuses.
    public static final String EXPENSE_STATUS_DRAFT = "Draft";
    public static final String EXPENSE_STATUS_PENDING_APPROVAL = "Pending Approval";
    public static final String EXPENSE_STATUS_APPROVED = "Approved";
    public static final String EXPENSE_STATUS_PAID = "Paid";
    public static final String EXPENSE_STATUS_REJECTED = "Rejected";

    // Expense types.
    public static final String EXPENSE_TYPE_TRAVEL = "Travel";
    public static final String EXPENSE_TYPE_EQUIPMENT = "Equipment";
    public static final String EXPENSE_TYPE_MATERIALS = "Materials";
    public static final String EXPENSE_TYPE_SERVICES = "Services";
    public static final String EXPENSE_TYPE_SOFTWARE = "Software";
    public static final String EXPENSE_TYPE_LABOUR = "Labour";
    public static final String EXPENSE_TYPE_UTILITIES = "Utilities";
    public static final String EXPENSE_TYPE_MISC = "Other";

    // Payment methods.
    public static final String PAYMENT_CASH = "Cash";
    public static final String PAYMENT_CREDIT_CARD = "Credit Card";
    public static final String PAYMENT_BANK_TRANSFER = "Bank Transfer";
    public static final String PAYMENT_CHEQUE = "Cheque";

    // User roles.
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    // Supported currencies.
    public static final String CURRENCY_USD = "USD";
    public static final String CURRENCY_EUR = "EUR";
    public static final String CURRENCY_VND = "VND";
    public static final String CURRENCY_GBP = "GBP";
    public static final String CURRENCY_JPY = "JPY";

    // Time constants.
    public static final long MILLISECONDS_PER_DAY = 86400000L;

    // Firebase paths.
    public static final String FIREBASE_PATH_USERS = "users";
    public static final String FIREBASE_PATH_PROJECTS = "projects";
    public static final String FIREBASE_PATH_EXPENSES = "expenses";

    // SharedPreferences keys.
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_USER_USERNAME = "user_username";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";

    // Validation limits.
    public static final int MIN_PROJECT_NAME_LENGTH = 5;
    public static final int MAX_PROJECT_NAME_LENGTH = 100;
    public static final int MIN_DESCRIPTION_LENGTH = 10;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final double MIN_BUDGET = 0.01;
    public static final double MIN_AMOUNT = 0.01;

    private Constants() {
        // Private constructor to prevent instantiation
    }
}
