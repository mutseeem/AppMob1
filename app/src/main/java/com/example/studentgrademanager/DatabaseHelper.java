package com.example.studentgrademanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StudentGradeManager.db";
    private static final int DATABASE_VERSION = 3;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FULLNAME = "fullName";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_GROUP = "user_group";
    private static final String COLUMN_GROUPS = "user_groups";
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

    // Teacher modules table
    private static final String TABLE_TEACHER_MODULES = "teacher_modules";
    private static final String COLUMN_TEACHER_ID = "teacher_id";

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

        // Create teacher_modules table
        String CREATE_TEACHER_MODULES_TABLE = "CREATE TABLE " + TABLE_TEACHER_MODULES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TEACHER_ID + " INTEGER,"
                + COLUMN_MODULE_ID + " TEXT,"
                + COLUMN_MODULE_GROUP + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_TEACHER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
                + "FOREIGN KEY(" + COLUMN_MODULE_ID + ") REFERENCES " + TABLE_MODULES + "(" + COLUMN_MODULE_ID + "))";
        db.execSQL(CREATE_TEACHER_MODULES_TABLE);

        // Insert sample data
        insertSampleData(db);
    }
    public void resetdb(){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER_MODULES);
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Only insert admin user
        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USERNAME, "admin");
        adminValues.put(COLUMN_PASSWORD, "admin123"); // In production, use hashed passwords
        adminValues.put(COLUMN_FULLNAME, "Administrator");
        adminValues.put(COLUMN_ROLE, "admin");
        db.insert(TABLE_USERS, null, adminValues);
    }
    public boolean insertModulesFromApi(JSONArray modulesJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        try {
            // Clear existing modules
            db.delete(TABLE_MODULES, null, null);

            for (int i = 0; i < modulesJson.length(); i++) {
                JSONObject module = modulesJson.getJSONObject(i);
                ContentValues values = new ContentValues();

                values.put(COLUMN_MODULE_ID, module.getString("id_module"));
                values.put(COLUMN_MODULE_NAME, module.getString("Nom_module"));
                // You might want to add group information here if available
                values.put(COLUMN_MODULE_GROUP, "G1"); // Default group or get from API

                db.insert(TABLE_MODULES, null, values);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }
    public void fetchAndInsertModules() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://num.univ-biskra.dz/psp/formations/get_modules_json?sem=2&spec=184")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONArray modulesArray = new JSONArray(jsonData);
                        insertModulesFromApi(modulesArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER_MODULES);
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

    // Assign module to teacher
    public boolean assignModuleToTeacher(int teacherId, String moduleId, String group) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEACHER_ID, teacherId);
        values.put(COLUMN_MODULE_ID, moduleId);
        values.put(COLUMN_MODULE_GROUP, group);
        long result = db.insert(TABLE_TEACHER_MODULES, null, values);
        return result != -1;
    }

    // Get teacher's modules
    public List<Module> getTeacherModules(int teacherId) {
        List<Module> modules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT m." + COLUMN_MODULE_ID + ", m." + COLUMN_MODULE_NAME + ", tm." + COLUMN_MODULE_GROUP +
                " FROM " + TABLE_TEACHER_MODULES + " tm" +
                " JOIN " + TABLE_MODULES + " m ON tm." + COLUMN_MODULE_ID + " = m." + COLUMN_MODULE_ID +
                " WHERE tm." + COLUMN_TEACHER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teacherId)});

        while (cursor.moveToNext()) {
            Module module = new Module(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2));
            modules.add(module);
        }
        cursor.close();
        return modules;
    }

    public List<User> getStudentsByModuleAndGroup(String moduleId, String group) {
        List<User> students = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Modified query to include grade information if available
        String query = "SELECT u." + COLUMN_ID + ", u." + COLUMN_USERNAME + ", u." + COLUMN_FULLNAME +
                ", g." + COLUMN_GRADE +
                " FROM " + TABLE_USERS + " u" +
                " LEFT JOIN " + TABLE_GRADES + " g ON u." + COLUMN_ID + " = g." + COLUMN_STUDENT_ID +
                " AND g." + COLUMN_MODULE_ID + " = ?" +
                " WHERE u." + COLUMN_ROLE + " = ? AND u." + COLUMN_GROUP + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{moduleId, "student", group});

        while (cursor.moveToNext()) {
            User student = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    "student",
                    group);
            students.add(student);
        }
        cursor.close();
        return students;
    }

    // Get students by module and group

    // Get student's grades
    public List<ModuleGrade> getStudentGrades(int studentId) {
        List<ModuleGrade> grades = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT m." + COLUMN_MODULE_NAME + ", g." + COLUMN_GRADE +
                " FROM " + TABLE_GRADES + " g" +
                " INNER JOIN " + TABLE_MODULES + " m ON g." + COLUMN_MODULE_ID + " = m." + COLUMN_MODULE_ID +
                " WHERE g." + COLUMN_STUDENT_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});

        while (cursor.moveToNext()) {
            String moduleName = cursor.getString(0);
            Double grade = cursor.isNull(1) ? null : cursor.getDouble(1);
            grades.add(new ModuleGrade(moduleName, grade));
        }
        cursor.close();
        return grades;
    }

    // Add or update grade
    public boolean addOrUpdateGrade(int studentId, String moduleId, double grade) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if grade exists
        Cursor cursor = db.query(TABLE_GRADES,
                new String[]{COLUMN_GRADE_ID},
                COLUMN_STUDENT_ID + " = ? AND " + COLUMN_MODULE_ID + " = ?",
                new String[]{String.valueOf(studentId), moduleId},
                null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            // Update existing grade
            cursor.moveToFirst();
            int gradeId = cursor.getInt(0);
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COLUMN_GRADE, grade);
            int rowsAffected = db.update(TABLE_GRADES, values,
                    COLUMN_GRADE_ID + " = ?", new String[]{String.valueOf(gradeId)});
            return rowsAffected > 0;
        } else {
            // Insert new grade
            if (cursor != null) cursor.close();
            ContentValues values = new ContentValues();
            values.put(COLUMN_STUDENT_ID, studentId);
            values.put(COLUMN_MODULE_ID, moduleId);
            values.put(COLUMN_GRADE, grade);
            long result = db.insert(TABLE_GRADES, null, values);
            return result != -1;
        }
    }

    // Get user by username
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_FULLNAME, COLUMN_ROLE, COLUMN_GROUP, COLUMN_GROUPS},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5) != null ? cursor.getString(5) : cursor.getString(6));
            cursor.close();
            return user;
        }
        return null;
    }

    // Check user credentials
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?",
                new String[]{username, password},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    // Get user role
    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ROLE},
                COLUMN_USERNAME + " = ?",
                new String[]{username},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            return role;
        }
        return null;
    }

    // Get all modules
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

    // Get all groups (G1-G12)
    public List<String> getAllGroups() {
        List<String> groups = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            groups.add("G" + i);
        }
        return groups;
    }

    private boolean isValidGroup(String group) {
        return group.matches("G[1-9]|G1[0-2]");
    }
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
}