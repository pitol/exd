package ppitol.exd.app.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import ppitol.exd.app.model.RateContract.RateColumns;

/**
 * DB helper for currency rates.
 */
public class RateDatabase extends SQLiteOpenHelper {

    private static final String TAG = RateDatabase.class.getSimpleName();

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "rate.db";

    public RateDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = String.format("create table %s (" +
                        "%s integer primary key autoincrement, " +
                        "%s text not null, " +
                        "%s int not null, " +
                        "%s int not null, " +
                        "%s int not null, " +
                        "unique (%s, %s))",
                RateContract.RATE_CONTENT,
                RateColumns.ID, RateColumns.CURRENCY_CODE, RateColumns.DATE, RateColumns.RATE_UNSCALED, RateColumns.RATE_SCALE,
                RateColumns.CURRENCY_CODE, RateColumns.DATE);
        db.execSQL(createTable);

        String createIndex = String.format("create unique index %s_%s_%s_idx on %s(%s, %s)",
                RateContract.RATE_CONTENT, RateColumns.CURRENCY_CODE, RateColumns.DATE,
                RateContract.RATE_CONTENT, RateColumns.CURRENCY_CODE, RateColumns.DATE);
        db.execSQL(createIndex);

        Log.d(TAG, "onCreate() - Created table for rates");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + RateContract.RATE_CONTENT);
        Log.d(TAG, "onUpgrade() - dropped rates table");
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    /**
     * Generic query on the rates database.
     *
     * @see android.database.sqlite.SQLiteQueryBuilder#query(android.database.sqlite.SQLiteDatabase, String[], String, String[], String, String, String)
     */
    public Cursor query(String tables, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(tables);
        return queryBuilder.query(
                getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
    }

    /**
     * Generic insert of values in a table in the rates database.
     * When conflicts occur any transaction is rolled back.
     *
     * @param table         the table receiving the values.
     * @param contentValues the values
     * @return the id of the new table record, or -1 if the insert failed.
     * @see android.database.sqlite.SQLiteDatabase#insertWithOnConflict(String, String, android.content.ContentValues, int)
     */
    public long insert(String table, ContentValues contentValues) {
        return getWritableDatabase().insertWithOnConflict(
                table,
                null,
                contentValues,
                SQLiteDatabase.CONFLICT_ROLLBACK);
    }

    /**
     * Generic update of records in a table in the rates database.
     *
     * @param table the table.
     * @param contentValues the update values.
     * @param selection the selection.
     * @param selectionArgs any arguments for selection.
     * @return the number of updated records.
     * @see android.database.sqlite.SQLiteDatabase#update(String, android.content.ContentValues, String, String[])
     */
    public int update(String table, ContentValues contentValues, String selection, String[] selectionArgs) {
        return getWritableDatabase().update(table, contentValues, selection, selectionArgs);
    }

    /**
     * Generic deletion of records in a table in the rates database.
     *
     * @param table the table.
     * @param selection the selection.
     * @param selectionArgs any arguments for selection.
     * @return the number of deleted records.
     * @see android.database.sqlite.SQLiteDatabase#delete(String, String, String[])
     */
    public int delete(String table, String selection, String[] selectionArgs) {
        return getWritableDatabase().delete(table, selection, selectionArgs);
    }
}
