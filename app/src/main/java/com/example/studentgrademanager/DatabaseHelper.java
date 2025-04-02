package com.example.studentgrademanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE_TEACHER_MODULES = "teacher_modules";
    private static final String COLUMN_TEACHER_ID = "teacher_id";
    private static final String COLUMN_MODULE_ID = "module_id";
    private static final String DATABASE_NAME = "StudentGrades.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_MODULES = "modules";
    private static final String TABLE_GRADES = "grades";

    // Common columns
    private static final String COLUMN_ID = "id";

    // Users table
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role"; // "admin", "teacher", "student"
    private static final String COLUMN_FULLNAME = "fullname";

    // Modules table
    private static final String COLUMN_MODULE_NAME = "module_name";
    private static final String COLUMN_MODULE_CODE = "module_code";

    // Grades table
    private static final String COLUMN_STUDENT_ID = "student_id";
    //private static final String COLUMN_MODULE_ID = "module_id";
    private static final String COLUMN_GRADE = "grade";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_ROLE + " TEXT,"
                + COLUMN_FULLNAME + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        // Create modules table
        String CREATE_MODULES_TABLE = "CREATE TABLE " + TABLE_MODULES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MODULE_NAME + " TEXT,"
                + COLUMN_MODULE_CODE + " TEXT UNIQUE)";
        db.execSQL(CREATE_MODULES_TABLE);

        // Create grades table
        String CREATE_GRADES_TABLE = "CREATE TABLE " + TABLE_GRADES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_STUDENT_ID + " INTEGER,"
                + COLUMN_MODULE_ID + " INTEGER,"
                + COLUMN_GRADE + " REAL,"
                + "FOREIGN KEY(" + COLUMN_STUDENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
                + "FOREIGN KEY(" + COLUMN_MODULE_ID + ") REFERENCES " + TABLE_MODULES + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_GRADES_TABLE);
        String CREATE_TEACHER_MODULES_TABLE = "CREATE TABLE " + TABLE_TEACHER_MODULES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TEACHER_ID + " INTEGER,"
                + COLUMN_MODULE_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_TEACHER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
                + "FOREIGN KEY(" + COLUMN_MODULE_ID + ") REFERENCES " + TABLE_MODULES + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_TEACHER_MODULES_TABLE);

        // Insert default admin
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USERNAME, "admin");
        adminValues.put(COLUMN_PASSWORD, "admin123");
        adminValues.put(COLUMN_ROLE, "admin");
        adminValues.put(COLUMN_FULLNAME, "Administrator");
        db.insert(TABLE_USERS, null, adminValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER_MODULES);
        onCreate(db);
    }

    // Check if user exists
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // Get user role
    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ROLE};
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            return role;
        }
        cursor.close();
        return "";
    }
    public boolean assignModuleToTeacher(String teacherUsername, String moduleCode) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First get teacher ID
        int teacherId = getUserId(teacherUsername);
        if (teacherId == -1) return false;

        // Then get module ID
        int moduleId = getModuleId(moduleCode);
        if (moduleId == -1) return false;

        // Insert into teacher_modules table
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEACHER_ID, teacherId);
        values.put(COLUMN_MODULE_ID, moduleId);

        long result = db.insert(TABLE_TEACHER_MODULES, null, values);
        return result != -1;
    }

    private int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    private int getModuleId(String moduleCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MODULES,
                new String[]{COLUMN_ID},
                COLUMN_MODULE_CODE + "=?",
                new String[]{moduleCode},
                null, null, null);

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }
    // Add this method to your DatabaseHelper class
    public boolean addUser(String username, String password, String role, String fullName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_FULLNAME, fullName);

        // Insert the new user
        long result = db.insert(TABLE_USERS, null, values);

        // Return true if insertion was successful
        return result != -1;
    }
}