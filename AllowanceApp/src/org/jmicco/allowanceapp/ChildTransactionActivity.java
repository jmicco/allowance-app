package org.jmicco.allowanceapp;

import java.util.List;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;
import org.jmicco.allowanceapp.TransactionRepository.TransactionEntry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChildTransactionActivity extends Activity {

	private ChildEntry entry;
	private TextView childNameView;
	private TextView childBalanceView;
	private ListView transactionList;
	private TransactionRepository transactionRepository;
	private ChildRepository childRepository;
	private long childId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_transaction_activity);
		childId = getIntent().getLongExtra(ExtraTagConstants.EXTRA_CHILD_ID, 0);
		childNameView = (TextView) findViewById(R.id.child_name);
		childBalanceView = (TextView) findViewById(R.id.child_balance);
		transactionList = (ListView) findViewById(R.id.transaction_list);
		childRepository = new ChildRepositorySqlLite(this);
		childRepository.open();
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
		entry = childRepository.getChild(childId);
		childNameView.setText(entry.getName());
		childBalanceView.setText(entry.getFormattedBalance());
		List<TransactionEntry> transactions = transactionRepository.getTransactions(entry.getChildId());
        ListAdapter adapter = new TransactionEntryAdapter(this, R.layout.transaction_activity_list_layout, transactions);
        transactionList.setOnItemClickListener(new ClickListener());
		transactionList.setAdapter(adapter);
		for (TransactionEntry entry : transactions) {
			System.out.println("transaction: " + entry.toString());
		}
	}
	
	public void addTransaction(View view) {
		System.out.println("addTransaction");
		Intent intent = new Intent(this, ChildNewTransactionActivity.class);
    	intent.putExtra(ExtraTagConstants.EXTRA_CHILD_ID, entry.getChildId());
    	intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    	startActivity(intent);
	}
	
	public class ClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			TransactionEntryAdapter adapter = (TransactionEntryAdapter) parent.getAdapter();
			TransactionEntry entry = adapter.getItem(position);			
			System.out.println("item Clicked " + entry.getTransactionId());
			Intent intent = new Intent(ChildTransactionActivity.this, ChildEditTransactionActivity.class);
	    	intent.putExtra(ExtraTagConstants.EXTRA_CHILD_ID, entry.getChildId());
	    	intent.putExtra(ExtraTagConstants.EXTRA_TRANSACTION_ID, entry.getTransactionId());
	    	startActivity(intent);
		}
	}
}
