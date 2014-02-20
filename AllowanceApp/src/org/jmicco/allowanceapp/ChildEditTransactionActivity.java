package org.jmicco.allowanceapp;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;
import org.jmicco.allowanceapp.DatePickerFragment.ActivityWithSetCalendar;
import org.jmicco.allowanceapp.TransactionRepository.TransactionEntry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChildEditTransactionActivity extends ActivityWithSetCalendar {
	private long transactionId;
	private Button dateEntry;
	private EditText amountEntry;
	private EditText itemEntry;
	private ChildRepository repository;
	private TransactionRepository transactionRepository;
	Calendar calendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_edit_transaction_activity);
		transactionId = getIntent().getLongExtra(ExtraTagConstants.EXTRA_TRANSACTION_ID, 0);
		dateEntry = (Button) findViewById(R.id.transaction_date);
		amountEntry = (EditText) findViewById(R.id.transaction_amount);
		itemEntry = (EditText) findViewById(R.id.transaction_item);
        repository = new ChildRepositorySqlLite(this); 
        repository.open();
        transactionRepository = new TransactionRepositorySqlLite(this);
        transactionRepository.open();
        calendar = GregorianCalendar.getInstance();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		repository.close();
		transactionRepository.close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TransactionEntry transactionEntry = transactionRepository.getTransaction(transactionId);
		calendar.setTime(transactionEntry.getDate());
		amountEntry.setText(String.format("%5.2f", transactionEntry.getAmount()));
		itemEntry.setText(transactionEntry.getDescription());
		updateCalendarText();
	}

	private void updateCalendarText() {
		DateFormat dateFormat = DateFormat.getDateInstance();
		Date date = new Date(calendar.getTimeInMillis());
		dateEntry.setText(dateFormat.format(date));
	}

	public void saveTransaction(View view) {
		TransactionEntry transactionEntry = transactionRepository.getTransaction(transactionId);
		
		double amount = 0.0;
		try {
			amount = Double.valueOf(amountEntry.getText().toString().trim());
		} catch (NumberFormatException e) {
			amountEntry.setError(e.getMessage());
			return;
		}
			
		Date date = new Date(calendar.getTimeInMillis());		
		//
		// This should really be a single transaction - for referential integrity
		//
		double adjust = amount - transactionEntry.getAmount();
		transactionEntry.setAmount(amount);
		transactionEntry.setDate(date);
		transactionEntry.setDescription(itemEntry.getText().toString());
		transactionRepository.updateTransaction(transactionEntry);
		
		ChildEntry entry = repository.getChild(transactionEntry.getChildId());
		entry.setBalance(entry.getBalance() + adjust);
		repository.updateChild(entry);

    	goBack(entry);
	}

	private void goBack(ChildEntry entry) {
		Intent intent = new Intent(this, ChildTransactionActivity.class);
    	intent.putExtra(ExtraTagConstants.EXTRA_CHILD_ID, entry.getChildId());
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
	}
	
	public void deleteTransaction(View view) {
		TransactionEntry transactionEntry = transactionRepository.getTransaction(transactionId);
		transactionRepository.deleteTransaction(transactionEntry);
		ChildEntry entry = repository.getChild(transactionEntry.getChildId());
		entry.setBalance(entry.getBalance() - transactionEntry.getAmount());
		repository.updateChild(entry);
		goBack(entry);
	}
	
	public void showDatePickerDialog(View v) {
		DatePickerFragment newFragment = new DatePickerFragment();
	    newFragment.setCalendar(calendar);
	    newFragment.show(getFragmentManager(), "datePicker");
	}
	
	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
		updateCalendarText();
	}
}
