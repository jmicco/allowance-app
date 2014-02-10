package org.jmicco.allowanceapp;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ChildTransactionActivity extends Activity {

	private ChildEntry entry;
	private TextView childNameView;
	private TextView childBalanceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_transaction_activity);
		entry = (ChildEntry)getIntent().getSerializableExtra(MainActivity.EXTRA_CHILD_ENTRY);
		childNameView = (TextView) findViewById(R.id.child_name);
		childBalanceView = (TextView) findViewById(R.id.child_balance);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		childNameView.setText(entry.getName());
		childBalanceView.setText(entry.getFormattedBalance());
	}
	
	public void addTransaction(View view) {
		System.out.println("addTransaction");
		Intent intent = new Intent(this, ChildNewTransactionActivity.class);
    	intent.putExtra(MainActivity.EXTRA_CHILD_ENTRY, entry);
    	startActivity(intent);
	}
}
