package org.jmicco.allowanceapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;
import org.jmicco.allowanceapp.TransactionRepository.TransactionEntry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class ChildNewTransactionActivity extends Activity {
	private ChildEntry entry;
	private EditText dateEntry;
	private EditText amountEntry;
	private EditText itemEntry;
	private ChildRepository repository;
	private TransactionRepository transactionRepository;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_new_transaction_activity);
		entry = (ChildEntry)getIntent().getSerializableExtra(MainActivity.EXTRA_CHILD_ENTRY);
		dateEntry = (EditText) findViewById(R.id.transaction_date);
		amountEntry = (EditText) findViewById(R.id.transaction_amount);
		itemEntry = (EditText) findViewById(R.id.transaction_item);
        repository = new ChildRepositorySqlLite(this); 
        repository.open();
        transactionRepository = new TransactionRepositorySqlLite(this);
        transactionRepository.open();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		repository.close();
		transactionRepository.close();
	}

	public void earnMoney(View view) {
		addTransaction(true);
	}
	
	public void spendMoney(View view) {
		addTransaction(false);		
	}

	private void addTransaction(boolean earned) {
		double amount = Double.valueOf(amountEntry.getText().toString().trim());
		if (!earned) {
			amount = -amount;
		}
	
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date date;
		try {
			date = dateFormat.parse(dateEntry.getText().toString().trim());
		} catch (ParseException e) {
			dateEntry.setError(e.getLocalizedMessage());
			e.printStackTrace();
			return;
		}
		
		//
		// This should really be a single transaction - for referential integrity
		//
		TransactionEntry transaction = new TransactionEntry(0L, entry.getChildId(), date, itemEntry.getText().toString().trim(), amount);
		transactionRepository.addTransaction(transaction);
		entry.setBalance(entry.getBalance() + amount);
		repository.updateChild(entry);

    	Intent intent = new Intent(this, ChildTransactionActivity.class);
    	intent.putExtra(MainActivity.EXTRA_CHILD_ENTRY, entry);
    	startActivity(intent);
	}
}
