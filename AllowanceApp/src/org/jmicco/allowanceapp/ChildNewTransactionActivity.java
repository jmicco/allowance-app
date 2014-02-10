package org.jmicco.allowanceapp;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;

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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		repository.close();
	}

	public void earnMoney(View view) {
		addTransaction(true);
	}
	
	public void spendMoney(View view) {
		addTransaction(false);		
	}

	private void addTransaction(boolean earned) {
		double amount = Double.parseDouble(amountEntry.getText().toString());
		if (!earned) {
			amount = -amount;
		}
		
		entry.setBalance(entry.getBalance() + amount);
		repository.updateChild(entry);
		
    	Intent intent = new Intent(this, ChildTransactionActivity.class);
    	intent.putExtra(MainActivity.EXTRA_CHILD_ENTRY, entry);
    	startActivity(intent);
	}
}
