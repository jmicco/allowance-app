package org.jmicco.allowanceapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Allowance.db";
	
	private final static String [] createTables = { 
        ChildRepositorySqlLite.Columns.SQL_CREATE_CHILD_TABLE,
        TransactionRepositorySqlLite.Columns.SQL_CREATE_TRANSACTION_TABLE
	};
	private final static String [] deleteTables = {
        ChildRepositorySqlLite.Columns.SQL_DELETE_CHILD_TABLE,
        TransactionRepositorySqlLite.Columns.SQL_DELETE_TRANSACTION_TABLE        		
    };
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		System.out.println("Creating the transactions database");
		for (String createTable : createTables) {
			db.execSQL(createTable);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("Destroying the transactions database for upgrade from " + oldVersion + " to " + newVersion);
		for (String deleteTable: deleteTables) {
			db.execSQL(deleteTable);
		}
		onCreate(db);
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}