package com.example.studentgrademanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("https://num.univ-biskra.dz/psp/formations/get_modules_json?sem=1&spec=184")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    System.out.println("DEBUG: db oncreate - "+ e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        System.out.println("DEBUG: API Response = " + jsonResponse);
                        try {
                            JSONArray jsonArray = new JSONArray(jsonResponse);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                ContentValues values =new ContentValues();
                                JSONObject module = jsonArray.getJSONObject(i);
                                values.put(COLUMN_MODULE_NAME,   module.getString("Nom_module"));
                                values.put(COLUMN_MODULE_CODE, module.getString("id_module"));
                                db.insert(TABLE_MODULES,null,values);
                            }
                        } catch (JSONException e) {
                            System.out.println("DEBUG: JSON Parsing Error - " + e.getMessage());
                        }
                    } else {
                        System.out.println("DEBUG: Response Failed - Code: " + response.code());
                    }
                }
            });
    }

    public void resetDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER_MODULES);
        onCreate(db); // Recreate tables
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

    public boolean assignModuleToTeacher(String teacherUsername, String moduleCode) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First get teacher ID
        int teacherId = getUserId(teacherUsername);
        System.out.println("DEBUG: assignmoduletoteacher - teacherid: "+teacherId);
        if (teacherId == -1) return false;

        // Then get module ID
        int moduleId = getModuleId(moduleCode);
        System.out.println("DEBUG: assignmoduletoteacher - moduleId: " + moduleId);
        if (moduleId == -1) return false;

        // Insert into teacher_modules table
        ContentValues values = new ContentValues();
        System.out.println("DEBUG: assignmoduletoteacher - moduleId,modulecod: " + moduleId +","+moduleCode + " teacherid:"+teacherId);
        values.put(COLUMN_TEACHER_ID, teacherId);
        values.put(COLUMN_MODULE_ID, moduleId);

        long result = db.insert(TABLE_TEACHER_MODULES, null, values);
        return result != -1;
    }

    private int getModuleId(String moduleCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MODULES,
                new String[]{COLUMN_ID},
                COLUMN_MODULE_CODE + "=?",
                new String[]{moduleCode},
                null, null, null);

        if (cursor.moveToFirst()) {
            System.out.println("DEBUG: getmoduleid - cursor: "+cursor.getString(0));
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        System.out.println("DEBUG: getmoduleid - cursor: none");
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

    public String getTeacherModuleCode(String teacherUsername) {
        int teacherId = getUserId(teacherUsername);
        if (teacherId == -1) {
            System.out.println("DEBUG: getTeacherModuleCode - Teacher not found for username: " + teacherUsername);
            return "";
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TEACHER_MODULES, new String[]{COLUMN_MODULE_ID}, COLUMN_TEACHER_ID + "=?", new String[]{String.valueOf(teacherId)}, null, null, null);
        String moduleCode = "";
        if (cursor.moveToFirst()) {
            int moduleId = cursor.getInt(0);
            cursor.close();
            Cursor cursor2 = db.query(TABLE_MODULES, new String[]{COLUMN_MODULE_CODE}, COLUMN_ID + "=?", new String[]{String.valueOf(moduleId)}, null, null, null);
            if (cursor2.moveToFirst()) {
                moduleCode = cursor2.getString(0);
                System.out.println("DEBUG: getTeacherModuleCode - Found module code: " + moduleCode);
            } else {
                System.out.println("DEBUG: getTeacherModuleCode - No module found for moduleId: " + moduleId);
            }
            cursor2.close();
        } else {
            System.out.println("DEBUG: getTeacherModuleCode - No teacher module record found for teacherId: " + teacherId);
            cursor.close();
        }
        return moduleCode;
    }

    public List<Student> getStudentsForModule(String moduleCode) {
        List<Student> studentList = new ArrayList<>();
        int moduleId = getModuleId(moduleCode);
        System.out.println("DEBUG: getStudentsForModule - moduleCode: " + moduleCode + ", moduleId: " + moduleId);
        if (moduleId == -1) return studentList;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_FULLNAME},
                COLUMN_ROLE + "=?", new String[]{"student"}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int studentId = cursor.getInt(0);
                String username = cursor.getString(1);
                String fullName = cursor.getString(2);
                Double grade = null;
                Cursor gradeCursor = db.query(TABLE_GRADES, new String[]{COLUMN_GRADE},
                        COLUMN_STUDENT_ID + "=? AND " + COLUMN_MODULE_ID + "=?",
                        new String[]{String.valueOf(studentId), String.valueOf(moduleId)}, null, null, null);
                if (gradeCursor.moveToFirst()) {
                    grade = gradeCursor.getDouble(0);
                }
                gradeCursor.close();
                studentList.add(new Student(studentId, username, fullName, grade));
                System.out.println("DEBUG: getStudentsForModule - Added student: " + username + ", grade: " + grade);
            } while (cursor.moveToNext());
        }
        cursor.close();
        System.out.println("DEBUG: getStudentsForModule - Total students loaded: " + studentList.size());
        return studentList;
    }

    public boolean updateStudentGrade(int studentId, double gradeValue, String moduleCode) {
        int moduleId = getModuleId(moduleCode);
        if (moduleId == -1) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_GRADES, new String[]{COLUMN_ID},
                COLUMN_STUDENT_ID + "=? AND " + COLUMN_MODULE_ID + "=?",
                new String[]{String.valueOf(studentId), String.valueOf(moduleId)}, null, null, null);
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_ID, studentId);
        values.put(COLUMN_MODULE_ID, moduleId);
        values.put(COLUMN_GRADE, gradeValue);
        boolean success;
        if (cursor.moveToFirst()) {
            int gradeId = cursor.getInt(0);
            success = db.update(TABLE_GRADES, values, COLUMN_ID + "=?", new String[]{String.valueOf(gradeId)}) > 0;
            System.out.println("DEBUG: updateStudentGrade - Updated grade for studentId: " + studentId);
        } else {
            success = db.insert(TABLE_GRADES, null, values) != -1;
            System.out.println("DEBUG: updateStudentGrade - Inserted grade for studentId: " + studentId);
        }
        cursor.close();
        return success;
    }
}