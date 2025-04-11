package com.example.studentgrademanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StudentGradeManager.db";
    private static final int DATABASE_VERSION = 2;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULLNAME = "fullName";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_GROUP = "user_group";  // For students
    private static final String COLUMN_GROUPS = "user_groups"; // For teachers
    private static final String COLUMN_GRADE = "grade";

    // Modules table
    private static final String TABLE_MODULES = "modules";
    private static final String COLUMN_MODULE_ID = "module_id";
    private static final String COLUMN_MODULE_NAME = "module_name";
    private static final String COLUMN_MODULE_GROUP = "module_group";

    // Grades table
    private static final String TABLE_GRADES = "grades";
    private static final String COLUMN_GRADE_ID = "grade_id";
    private static final String COLUMN_STUDENT_ID = "student_id";

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
                + COLUMN_FULLNAME + " TEXT,"
                + COLUMN_ROLE + " TEXT,"
                + COLUMN_GROUP + " TEXT,"
                + COLUMN_GROUPS + " TEXT,"
                + COLUMN_GRADE + " REAL)";
        db.execSQL(CREATE_USERS_TABLE);

        // Create modules table
        String CREATE_MODULES_TABLE = "CREATE TABLE " + TABLE_MODULES + "("
                + COLUMN_MODULE_ID + " TEXT PRIMARY KEY,"
                + COLUMN_MODULE_NAME + " TEXT,"
                + COLUMN_MODULE_GROUP + " TEXT)";
        db.execSQL(CREATE_MODULES_TABLE);

        // Create grades table
        String CREATE_GRADES_TABLE = "CREATE TABLE " + TABLE_GRADES + "("
                + COLUMN_GRADE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_STUDENT_ID + " INTEGER,"
                + COLUMN_MODULE_ID + " TEXT,"
                + COLUMN_GRADE + " REAL,"
                + "FOREIGN KEY(" + COLUMN_STUDENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
                + "FOREIGN KEY(" + COLUMN_MODULE_ID + ") REFERENCES " + TABLE_MODULES + "(" + COLUMN_MODULE_ID + "))";
        db.execSQL(CREATE_GRADES_TABLE);

        // Insert sample data
        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert sample modules
        ContentValues moduleValues = new ContentValues();
        moduleValues.put(COLUMN_MODULE_ID, "MATH101");
        moduleValues.put(COLUMN_MODULE_NAME, "Mathematics");
        moduleValues.put(COLUMN_MODULE_GROUP, "G1");
        db.insert(TABLE_MODULES, null, moduleValues);

        moduleValues.clear();
        moduleValues.put(COLUMN_MODULE_ID, "PHY101");
        moduleValues.put(COLUMN_MODULE_NAME, "Physics");
        moduleValues.put(COLUMN_MODULE_GROUP, "G1");
        db.insert(TABLE_MODULES, null, moduleValues);

        // Insert sample student
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USERNAME, "student1");
        userValues.put(COLUMN_PASSWORD, "password");
        userValues.put(COLUMN_FULLNAME, "John Doe");
        userValues.put(COLUMN_ROLE, "student");
        userValues.put(COLUMN_GROUP, "G1");
        db.insert(TABLE_USERS, null, userValues);

        // Insert sample teacher
        userValues.clear();
        userValues.put(COLUMN_USERNAME, "teacher1");
        userValues.put(COLUMN_PASSWORD, "password");
        userValues.put(COLUMN_FULLNAME, "Jane Smith");
        userValues.put(COLUMN_ROLE, "teacher");
        userValues.put(COLUMN_GROUPS, "G1,G2");
        db.insert(TABLE_USERS, null, userValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        onCreate(db);
    }

    // Add user with all parameters
    public boolean addUser(String username, String password, String role, String fullName, String groupOrGroups) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_FULLNAME, fullName);
        values.put(COLUMN_ROLE, role);

        if (role.equals("student")) {
            if (!isValidGroup(groupOrGroups)) return false;
            values.put(COLUMN_GROUP, groupOrGroups);
        } else if (role.equals("teacher")) {
            // For teachers, groupOrGroups should be comma-separated
            String[] groups = groupOrGroups.split(",");
            for (String group : groups) {
                if (!isValidGroup(group.trim())) return false;
            }
            values.put(COLUMN_GROUPS, groupOrGroups);
        }

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // Add student with single group
    public boolean addStudent(String username, String password, String fullName, String group) {
        return addUser(username, password, "student", fullName, group);
    }

    // Add teacher with multiple groups
    public boolean addTeacher(String username, String password, String fullName, List<String> groups) {
        String groupsString = TextUtils.join(",", groups);
        return addUser(username, password, "teacher", fullName, groupsString);
    }

    private boolean isValidGroup(String group) {
        return group.matches("G[1-9]|G10");
    }

    // Get all available groups (G1-G10)
    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            groups.add("G" + i);
        }
        return groups;
    }

    // Get students in a specific group
    public List<User> getStudentsByGroup(String group) {
        List<User> students = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_FULLNAME, COLUMN_GROUP},
                COLUMN_ROLE + " = ? AND " + COLUMN_GROUP + " = ?",
                new String[]{"student", group},
                null, null, null);

        while (cursor.moveToNext()) {
            User student = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    "student",
                    cursor.getString(3));
            students.add(student);
        }
        cursor.close();
        return students;
    }

    // Get user by username and password
    public User getUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_FULLNAME, COLUMN_ROLE, COLUMN_GROUP, COLUMN_GROUPS},
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{username, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4) != null ? cursor.getString(4) : cursor.getString(5));
            cursor.close();
            return user;
        }
        return null;
    }

    // Get all modules for a group
    public List<Module> getModulesByGroup(String group) {
        List<Module> modules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MODULES,
                new String[]{COLUMN_MODULE_ID, COLUMN_MODULE_NAME},
                COLUMN_MODULE_GROUP + " = ?",
                new String[]{group},
                null, null, null);

        while (cursor.moveToNext()) {
            Module module = new Module(
                    cursor.getString(0),
                    cursor.getString(1),
                    group);
            modules.add(module);
        }
        cursor.close();
        return modules;
    }

    // Add grade for a student
    public boolean addGrade(int studentId, String moduleId, double grade) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_ID, studentId);
        values.put(COLUMN_MODULE_ID, moduleId);
        values.put(COLUMN_GRADE, grade);
        long result = db.insert(TABLE_GRADES, null, values);
        return result != -1;
    }

    // Get grades for a student
    public List<Grade> getGradesByStudent(int studentId) {
        List<Grade> grades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT m." + COLUMN_MODULE_NAME + ", g." + COLUMN_GRADE +
                " FROM " + TABLE_GRADES + " g" +
                " INNER JOIN " + TABLE_MODULES + " m ON g." + COLUMN_MODULE_ID + " = m." + COLUMN_MODULE_ID +
                " WHERE g." + COLUMN_STUDENT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});

        while (cursor.moveToNext()) {
            String moduleName = cursor.getString(0);
            double gradeValue = cursor.getDouble(1);
            grades.add(new Grade(moduleName, gradeValue));
        }
        cursor.close();
        return grades;
    }

    // Get all teachers
    public List<User> getAllTeachers() {
        List<User> teachers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_FULLNAME, COLUMN_GROUPS},
                COLUMN_ROLE + " = ?",
                new String[]{"teacher"},
                null, null, null);

        while (cursor.moveToNext()) {
            User teacher = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    "teacher",
                    cursor.getString(3));
            teachers.add(teacher);
        }
        cursor.close();
        return teachers;
    }
    public List<String> getAllModules() {
        List<String> modules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MODULES,
                new String[]{COLUMN_MODULE_NAME},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            modules.add(cursor.getString(0));
        }
        cursor.close();
        return modules;
    }
}