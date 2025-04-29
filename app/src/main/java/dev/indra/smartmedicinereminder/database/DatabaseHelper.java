package dev.indra.smartmedicinereminder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import dev.indra.smartmedicinereminder.models.Medication;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medicine_reminder.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MEDICATIONS = "medications";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DOSAGE = "dosage";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_MINUTE = "minute";
    private static final String COLUMN_ACTIVE = "is_active";
    private static final String COLUMN_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEDICATIONS_TABLE = "CREATE TABLE " + TABLE_MEDICATIONS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_DOSAGE + " TEXT,"
                + COLUMN_HOUR + " INTEGER,"
                + COLUMN_MINUTE + " INTEGER,"
                + COLUMN_ACTIVE + " INTEGER,"
                + COLUMN_NOTES + " TEXT"
                + ")";
        db.execSQL(CREATE_MEDICATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        onCreate(db);
    }

    public long addMedication(Medication medication) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, medication.getName());
        values.put(COLUMN_DOSAGE, medication.getDosage());
        values.put(COLUMN_HOUR, medication.getHour());
        values.put(COLUMN_MINUTE, medication.getMinute());
        values.put(COLUMN_ACTIVE, medication.isActive() ? 1 : 0);
        values.put(COLUMN_NOTES, medication.getNotes());

        long id = db.insert(TABLE_MEDICATIONS, null, values);
        db.close();
        return id;
    }

    public Medication getMedication(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_MEDICATIONS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

        cursor.moveToFirst();

        Medication medication = new Medication();
        medication.setId(cursor.getLong(0));
        medication.setName(cursor.getString(1));
        medication.setDosage(cursor.getString(2));
        medication.setHour(cursor.getInt(3));
        medication.setMinute(cursor.getInt(4));
        medication.setActive(cursor.getInt(5) == 1);
        medication.setNotes(cursor.getString(6));

        cursor.close();
        return medication;
    }

    public List<Medication> getAllMedications() {
        List<Medication> medicationList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_MEDICATIONS + " ORDER BY " + COLUMN_HOUR + ", " + COLUMN_MINUTE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Medication medication = new Medication();
                medication.setId(cursor.getLong(0));
                medication.setName(cursor.getString(1));
                medication.setDosage(cursor.getString(2));
                medication.setHour(cursor.getInt(3));
                medication.setMinute(cursor.getInt(4));
                medication.setActive(cursor.getInt(5) == 1);
                medication.setNotes(cursor.getString(6));

                medicationList.add(medication);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return medicationList;
    }

    public void updateMedication(Medication medication) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, medication.getName());
        values.put(COLUMN_DOSAGE, medication.getDosage());
        values.put(COLUMN_HOUR, medication.getHour());
        values.put(COLUMN_MINUTE, medication.getMinute());
        values.put(COLUMN_ACTIVE, medication.isActive() ? 1 : 0);
        values.put(COLUMN_NOTES, medication.getNotes());

        db.update(TABLE_MEDICATIONS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(medication.getId())});
    }

    public void deleteMedication(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDICATIONS, COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    public int getMedicationsCount() {
        String countQuery = "SELECT * FROM " + TABLE_MEDICATIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}