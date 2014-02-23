package org.jmicco.allowanceapp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class TransactionRepositorySqlLite extends TransactionRepository {
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
		public static String SQL_CREATE_TRANSACTION_TABLE =
		    "CREATE TABLE " + TABLE_NAME + " ("
		    + Columns._ID + " INTEGER PRIMARY KEY,"
		    + COLUMN_CHILD_ID + " INTEGER,"
		    + COLUMN_DATE + " INTEGER,"
		    + COLUMN_DESCRIPTION + " TEXT,"
		    + COLUMN_AMOUNT + " DOUBLE"
		    + ")";
		
		public static String SQL_DELETE_TRANSACTION_TABLE =
				"DROP TABLE IF EXISTS " + Columns.TABLE_NAME;
	}
		
	public TransactionRepositorySqlLite(SQLiteDatabase db) {
		this.db = db;
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
	public TransactionEntry getTransaction(long transactionId) {
		String [] args = { Long.toString(transactionId) };
		Cursor c = db.query(
			    Columns.TABLE_NAME,
			    Columns.ALL_COLUMNS,
			    Columns._ID + " = ?", 
			    args,
			    null,
			    null,
			    null
		    );
		c.moveToFirst();
		if (c.isAfterLast()) {
			return null;			
		}
		return makeTransactionEntryFromCursor(c);
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

	@Override
	public void updateTransaction(TransactionEntry entry) {
		ContentValues values = new ContentValues();
		values.put(Columns.COLUMN_CHILD_ID, entry.getChildId());
		values.put(Columns.COLUMN_DATE, entry.getDate().getTime());
		values.put(Columns.COLUMN_DESCRIPTION, entry.getDescription());
		values.put(Columns.COLUMN_AMOUNT, entry.getAmount());
		db.update(Columns.TABLE_NAME, values, Columns._ID + " = " + entry.getTransactionId(), null);
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
