package com.example.studentgrademanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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
    private static final String DATABASE_NAME = "StudentGrades.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_USERS = "users";
    private static final String TABLE_MODULES = "modules";
    private static final String TABLE_GRADES = "grades";
    private static final String TABLE_TEACHER_MODULES = "teacher_modules";
    private static final String TABLE_TEACHER_GROUPS = "teacher_groups";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_FULLNAME = "fullname";
    private static final String COLUMN_MODULE_NAME = "module_name";
    private static final String COLUMN_MODULE_CODE = "module_code";
    private static final String COLUMN_STUDENT_ID = "student_id";
    private static final String COLUMN_GRADE = "grade";
    private static final String COLUMN_TEACHER_ID = "teacher_id";
    private static final String COLUMN_MODULE_ID = "module_id";
    private static final String COLUMN_GROUP_NAME = "group_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_USERNAME + " TEXT UNIQUE," + COLUMN_PASSWORD + " TEXT," + COLUMN_ROLE + " TEXT," + COLUMN_FULLNAME + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_MODULES + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_MODULE_NAME + " TEXT," + COLUMN_MODULE_CODE + " TEXT UNIQUE)");
        db.execSQL("CREATE TABLE " + TABLE_GRADES + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_STUDENT_ID + " INTEGER," + COLUMN_MODULE_ID + " INTEGER," + COLUMN_GRADE + " REAL,FOREIGN KEY(" + COLUMN_STUDENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),FOREIGN KEY(" + COLUMN_MODULE_ID + ") REFERENCES " + TABLE_MODULES + "(" + COLUMN_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_TEACHER_MODULES + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TEACHER_ID + " INTEGER," + COLUMN_MODULE_ID + " INTEGER,FOREIGN KEY(" + COLUMN_TEACHER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),FOREIGN KEY(" + COLUMN_MODULE_ID + ") REFERENCES " + TABLE_MODULES + "(" + COLUMN_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_TEACHER_GROUPS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TEACHER_ID + " INTEGER," + COLUMN_GROUP_NAME + " TEXT,FOREIGN KEY(" + COLUMN_TEACHER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))");

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, "admin");
        values.put(COLUMN_PASSWORD, "admin123");
        values.put(COLUMN_ROLE, "admin");
        values.put(COLUMN_FULLNAME, "Administrator");
        db.insert(TABLE_USERS, null, values);

        fetchModulesFromAPI(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER_GROUPS);
        onCreate(db);
    }

    private void fetchModulesFromAPI(SQLiteDatabase db) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://num.univ-biskra.dz/psp/formations/get_modules_json?sem=1&spec=184")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("DEBUG: Failed to fetch modules - " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(jsonResponse);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject module = jsonArray.getJSONObject(i);
                            String moduleName = module.getString("Nom_module");
                            String moduleCode = module.getString("id_module");

                            ContentValues values = new ContentValues();
                            values.put(COLUMN_MODULE_NAME, moduleName);
                            values.put(COLUMN_MODULE_CODE, moduleCode);

                            db.insertWithOnConflict(TABLE_MODULES, null, values,
                                    SQLiteDatabase.CONFLICT_IGNORE);
                        }
                    } catch (JSONException e) {
                        System.out.println("DEBUG: JSON Parsing Error - " + e.getMessage());
                    }
                }
            }
        });
    }

    public void fetchModulesFromAPI() {
        fetchModulesFromAPI(getWritableDatabase());
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{username, password}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ROLE}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            return role;
        }
        cursor.close();
        return "";
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    public boolean addUser(String username, String password, String role, String fullName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, role);
        values.put(COLUMN_FULLNAME, fullName);
        return db.insert(TABLE_USERS, null, values) != -1;
    }

    public boolean assignModuleToTeacher(String teacherUsername, String moduleCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        int teacherId = getUserId(teacherUsername);
        int moduleId = getModuleId(moduleCode);
        if (teacherId == -1 || moduleId == -1) return false;
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEACHER_ID, teacherId);
        values.put(COLUMN_MODULE_ID, moduleId);
        return db.insert(TABLE_TEACHER_MODULES, null, values) != -1;
    }

    private int getModuleId(String moduleCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MODULES, new String[]{COLUMN_ID}, COLUMN_MODULE_CODE + "=?", new String[]{moduleCode}, null, null, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }

    public List<String> getAllModules() {
        List<String> modules = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MODULES, new String[]{COLUMN_MODULE_NAME, COLUMN_MODULE_CODE}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                modules.add(cursor.getString(0) + " (" + cursor.getString(1) + ")");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return modules;
    }

    public boolean assignGroupsToTeacher(int teacherId, List<String> groups) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (String group : groups) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TEACHER_ID, teacherId);
                values.put(COLUMN_GROUP_NAME, group.trim());
                db.insert(TABLE_TEACHER_GROUPS, null, values);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addTeacher(Teacher teacher) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues userValues = new ContentValues();
            userValues.put(COLUMN_USERNAME, teacher.getUsername());
            userValues.put(COLUMN_PASSWORD, teacher.getPassword());
            userValues.put(COLUMN_FULLNAME, teacher.getFullName());
            userValues.put(COLUMN_ROLE, "teacher");
            long userId = db.insert(TABLE_USERS, null, userValues);
            if (userId == -1) return false;
            for (String module : teacher.getModules()) {
                String moduleCode = module.substring(module.indexOf("(") + 1, module.indexOf(")"));
                assignModuleToTeacher(teacher.getUsername(), moduleCode);
            }
            assignGroupsToTeacher((int)userId, teacher.getGroups());
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<ModuleGrade> getModuleGradesForStudent(String studentUsername) {
        List<ModuleGrade> moduleGrades = new ArrayList<>();
        int studentId = getUserId(studentUsername);
        if (studentId == -1) return moduleGrades;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT m." + COLUMN_MODULE_NAME + ", g." + COLUMN_GRADE + " FROM " + TABLE_MODULES + " m LEFT JOIN " + TABLE_GRADES + " g ON m." + COLUMN_ID + " = g." + COLUMN_MODULE_ID + " AND g." + COLUMN_STUDENT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});
        if (cursor.moveToFirst()) {
            do {
                String moduleName = cursor.getString(0);
                Double grade = cursor.isNull(1) ? null : cursor.getDouble(1);
                moduleGrades.add(new ModuleGrade(moduleName, grade));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return moduleGrades;
    }

    public String getTeacherModuleCode(String teacherUsername) {
        SQLiteDatabase db = this.getReadableDatabase();
        String moduleCode = "";
        int teacherId = getUserId(teacherUsername);
        if (teacherId == -1) return moduleCode;
        String query = "SELECT m." + COLUMN_MODULE_CODE + " FROM " + TABLE_MODULES + " m INNER JOIN " + TABLE_TEACHER_MODULES + " tm ON m." + COLUMN_ID + " = tm." + COLUMN_MODULE_ID + " WHERE tm." + COLUMN_TEACHER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(teacherId)});
        if (cursor.moveToFirst()) {
            moduleCode = cursor.getString(0);
        }
        cursor.close();
        return moduleCode;
    }

    public List<Student> getStudentsForModule(String moduleCode) {
        List<Student> students = new ArrayList<>();
        int moduleId = getModuleId(moduleCode);
        if (moduleId == -1) return students;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u." + COLUMN_ID + ", u." + COLUMN_USERNAME + ", u." + COLUMN_FULLNAME + ", g." + COLUMN_GRADE + " FROM " + TABLE_USERS + " u LEFT JOIN " + TABLE_GRADES + " g ON u." + COLUMN_ID + " = g." + COLUMN_STUDENT_ID + " AND g." + COLUMN_MODULE_ID + " = ? WHERE u." + COLUMN_ROLE + " = 'student'";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(moduleId)});
        if (cursor.moveToFirst()) {
            do {
                int studentId = cursor.getInt(0);
                String username = cursor.getString(1);
                String fullName = cursor.getString(2);
                Double grade = cursor.isNull(3) ? null : cursor.getDouble(3);
                students.add(new Student(studentId, username, fullName, grade));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return students;
    }

    public boolean updateStudentGrade(int studentId, double gradeValue, String moduleCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        int moduleId = getModuleId(moduleCode);
        if (moduleId == -1) return false;
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_ID, studentId);
        values.put(COLUMN_MODULE_ID, moduleId);
        values.put(COLUMN_GRADE, gradeValue);
        Cursor cursor = db.query(TABLE_GRADES, new String[]{COLUMN_ID}, COLUMN_STUDENT_ID + "=? AND " + COLUMN_MODULE_ID + "=?", new String[]{String.valueOf(studentId), String.valueOf(moduleId)}, null, null, null);
        boolean success;
        if (cursor.moveToFirst()) {
            int gradeId = cursor.getInt(0);
            success = db.update(TABLE_GRADES, values, COLUMN_ID + "=?", new String[]{String.valueOf(gradeId)}) > 0;
        } else {
            success = db.insert(TABLE_GRADES, null, values) != -1;
        }
        cursor.close();
        return success;
    }
}