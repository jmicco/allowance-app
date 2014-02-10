package org.jmicco.allowanceapp;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;

import android.app.Activity;
import android.os.Bundle;


public class ChildNewTransactionActivity extends Activity {

	private ChildEntry entry;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_new_transaction_activity);
		entry = (ChildEntry)getIntent().getSerializableExtra(MainActivity.EXTRA_CHILD_ENTRY);
	}

}
