package org.jmicco.allowanceapp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class TransactionRepositorySqlLite extends TransactionRepository {
	private Context context;
	private DbHelper dbhelper;
	private SQLiteDatabase db;

	public static class Columns  implements BaseColumns {
		public static final String TABLE_NAME = "transactions";
		public static final String COLUMN_CHILD_ID = "child_id";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_DESCRIPTION = "description";
		public static final String COLUMN_AMOUNT = "amount";
		public static final String [] ALL_COLUMNS = {
			_ID,
			COLUMN_CHILD_ID,
			COLUMN_DATE,
			COLUMN_DESCRIPTION,
			COLUMN_AMOUNT
		};
	}
	
	public static class DbHelper extends SQLiteOpenHelper {
		public static final int DATABASE_VERSION = MainActivity.DATABASE_VERSION;
		public static final String DATABASE_NAME = "Allowance.db";
		private static String SQL_CREATE_DATABASE =
				"CREATE TABLE " + Columns.TABLE_NAME + " ("
				+ Columns._ID + " INTEGER PRIMARY KEY,"
				+ Columns.COLUMN_CHILD_ID + " INTEGER,"
				+ Columns.COLUMN_DATE + " INTEGER,"
				+ Columns.COLUMN_DESCRIPTION + " TEXT,"
				+ Columns.COLUMN_AMOUNT + " DOUBLE"
				+ ")";
		
		private static String SQL_DELETE_DATABASE =
				"DROP TABLE IF EXISTS " + Columns.TABLE_NAME;
		
		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("Creating the transactions database");
			db.execSQL(SQL_CREATE_DATABASE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			System.out.println("Destroying the transactions database for upgrade from " + oldVersion + " to " + newVersion);
			db.execSQL(SQL_DELETE_DATABASE);
			onCreate(db);
		}
		
		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onUpgrade(db, oldVersion, newVersion);
		}
	}

	
	public TransactionRepositorySqlLite(Context context) {
		this.context = context;
		dbhelper = null;
		db = null;
	}
	
	@Override
	public void open() {
		dbhelper = new DbHelper(context);
		db = dbhelper.getWritableDatabase();
	}

	@Override
	public void close() {
		dbhelper.close();
		dbhelper = null;
		db = null;
	}

	@Override
	public List<TransactionEntry> getTransactions(long childId) {
		Cursor c = db.query(
			    Columns.TABLE_NAME,
			    Columns.ALL_COLUMNS,
			    Columns.COLUMN_CHILD_ID + " = " + childId,
			    null,
			    null,
			    null,
			    Columns.COLUMN_DATE + " DESC"
		    );
		List<TransactionEntry> list = new ArrayList<TransactionEntry>();
		c.moveToFirst();
		while (!c.isAfterLast()) {
			list.add(makeTransactionEntryFromCursor(c));
			c.moveToNext();
		}
		c.close();
		return list;
	}

	@Override
	public long addTransaction(TransactionEntry entry) {
		ContentValues values = new ContentValues();
		values.put(Columns.COLUMN_CHILD_ID, entry.getChildId());
		values.put(Columns.COLUMN_DATE, entry.getDate().getTime());
		values.put(Columns.COLUMN_DESCRIPTION, entry.getDescription());
		values.put(Columns.COLUMN_AMOUNT, entry.getAmount());
		entry.setChildId(db.insert(Columns.TABLE_NAME, null, values));
		return entry.getChildId();
	}

	@Override
	public void deleteTransaction(TransactionEntry entry) {
		db.delete(Columns.TABLE_NAME, Columns._ID + " = " + entry.getTransactionId(), null);
	}

	private TransactionEntry makeTransactionEntryFromCursor(Cursor c) {
		return new TransactionEntry(
				c.getLong(c.getColumnIndexOrThrow(Columns._ID)), 
				c.getLong(c.getColumnIndexOrThrow(Columns.COLUMN_CHILD_ID)),
				new Date(c.getLong(c.getColumnIndexOrThrow(Columns.COLUMN_DATE))), 
				c.getString(c.getColumnIndexOrThrow(Columns.COLUMN_DESCRIPTION)), 
				c.getDouble(c.getColumnIndexOrThrow(Columns.COLUMN_AMOUNT)));
	}
}
