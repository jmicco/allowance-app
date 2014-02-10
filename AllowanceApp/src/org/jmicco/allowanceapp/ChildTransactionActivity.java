package org.jmicco.allowanceapp;

import java.util.List;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;
import org.jmicco.allowanceapp.MainActivity.ClickListener;
import org.jmicco.allowanceapp.TransactionRepository.TransactionEntry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChildTransactionActivity extends Activity {

	private ChildEntry entry;
	private TextView childNameView;
	private TextView childBalanceView;
	private ListView transactionList;
	private TransactionRepository transactionRepository;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_transaction_activity);
		entry = (ChildEntry)getIntent().getSerializableExtra(MainActivity.EXTRA_CHILD_ENTRY);
		childNameView = (TextView) findViewById(R.id.child_name);
		childBalanceView = (TextView) findViewById(R.id.child_balance);
		transactionList = (ListView) findViewById(R.id.transaction_list);
		transactionRepository = new TransactionRepositorySqlLite(this);
		transactionRepository.open();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		transactionRepository.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
		childNameView.setText(entry.getName());
		childBalanceView.setText(entry.getFormattedBalance());
		List<TransactionEntry> transactions = transactionRepository.getTransactions(entry.getChildId());
        ListAdapter adapter = new TransactionEntryAdapter(this, R.layout.transaction_activity_list_layout, transactions);
		transactionList.setAdapter(adapter);
		for (TransactionEntry entry : transactions) {
			System.out.println("transaction: " + entry.toString());
		}
	}
	
	public void addTransaction(View view) {
		System.out.println("addTransaction");
		Intent intent = new Intent(this, ChildNewTransactionActivity.class);
    	intent.putExtra(MainActivity.EXTRA_CHILD_ENTRY, entry);
    	startActivity(intent);
	}
}
