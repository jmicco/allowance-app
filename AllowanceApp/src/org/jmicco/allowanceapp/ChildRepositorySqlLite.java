package org.jmicco.allowanceapp;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class ChildRepositorySqlLite extends ChildRepository {
	private DbHelper dbhelper;
	private SQLiteDatabase db;
	private final Context context;
	
	public static class Columns  implements BaseColumns {
		public static final String TABLE_NAME = "child";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_BALANCE = "balance";
		public static final String [] ALL_COLUMNS = {
			_ID,
			COLUMN_NAME,
			COLUMN_BALANCE
		};
	}
	
	public static class DbHelper extends SQLiteOpenHelper {
		public static final int DATABASE_VERSION = MainActivity.DATABASE_VERSION;
		public static final String DATABASE_NAME = "Allowance.db";
		private static String SQL_CREATE_DATABASE =
				"CREATE TABLE " + Columns.TABLE_NAME + " ("
				+ Columns._ID + " INTEGER PRIMARY KEY,"
				+ Columns.COLUMN_NAME + " TEXT,"
				+ Columns.COLUMN_BALANCE + " DOUBLE"
				+ ")";
		
		private static String SQL_DELETE_DATABASE =
				"DROP TABLE IF EXISTS " + Columns.TABLE_NAME;
		
		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			System.out.println("Creating the database");
			db.execSQL(SQL_CREATE_DATABASE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			System.out.println("Destroying the database for upgrade from " + oldVersion + " to " + newVersion);
			db.execSQL(SQL_DELETE_DATABASE);
			onCreate(db);
		}
		
		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onUpgrade(db, oldVersion, newVersion);
		}
	}
	
	public ChildRepositorySqlLite(Context context) {
		this.context = context;
		dbhelper = null;
		db = null;
	}
	
	@Override
	public List<ChildEntry> getChildren() {
		Cursor c = db.query(
		    Columns.TABLE_NAME,
		    Columns.ALL_COLUMNS,
		    null,
		    null,
		    null,
		    null,
		    Columns.COLUMN_NAME
	    );
		List<ChildEntry> list = new ArrayList<ChildEntry>();
		c.moveToFirst();
		while (!c.isAfterLast()) {
			list.add(makeChildEntryFromCursor(c));
			c.moveToNext();
		}
		c.close();
		return list;
	}

	private ChildEntry makeChildEntryFromCursor(Cursor c) {
		return new ChildEntry(
				c.getLong(c.getColumnIndexOrThrow(Columns._ID)), 
				c.getString(c.getColumnIndexOrThrow(Columns.COLUMN_NAME)), 
				c.getDouble(c.getColumnIndexOrThrow(Columns.COLUMN_BALANCE)));
	}

	@Override
	public void updateChild(ChildEntry entry) {
		ContentValues values = new ContentValues();
		values.put(Columns.COLUMN_NAME, entry.getName());
		values.put(Columns.COLUMN_BALANCE, entry.getBalance());
		db.update(Columns.TABLE_NAME, values, Columns._ID + " = " + entry.getChildId(), null);
	}

	@Override
	public long addChild(ChildEntry entry) {
		ContentValues values = new ContentValues();
		values.put(Columns.COLUMN_NAME, entry.getName());
		values.put(Columns.COLUMN_BALANCE, entry.getBalance());
		entry.setChildId(db.insert(Columns.TABLE_NAME, null, values));
		return entry.getChildId();
	}

	@Override
	public ChildEntry getChild(String name) {
		String [] args = { name };
		Cursor c = db.query(
			    Columns.TABLE_NAME,
			    Columns.ALL_COLUMNS,
			    Columns.COLUMN_NAME + " = ?", 
			    args,
			    null,
			    null,
			    Columns.COLUMN_NAME
		    );
		c.moveToFirst();
		if (c.isAfterLast()) {
			return null;			
		}
		return makeChildEntryFromCursor(c);
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



}
