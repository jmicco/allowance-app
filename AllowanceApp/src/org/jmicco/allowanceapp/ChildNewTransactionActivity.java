package org.jmicco.allowanceapp;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;
import org.jmicco.allowanceapp.TransactionRepository.TransactionEntry;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;


public class ChildNewTransactionActivity extends Activity {
	private ChildEntry entry;
	private Button dateEntry;
	private EditText amountEntry;
	private EditText itemEntry;
	private ChildRepository repository;
	private TransactionRepository transactionRepository;
	Calendar calendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.child_new_transaction_activity);
		entry = (ChildEntry)getIntent().getSerializableExtra(ExtraTagConstants.EXTRA_CHILD_ENTRY);
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
		updateCalendarText();
	}

	private void updateCalendarText() {
		DateFormat dateFormat = DateFormat.getDateInstance();
		Date date = new Date(calendar.getTimeInMillis());
		dateEntry.setText(dateFormat.format(date));
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
	
		Date date = new Date(calendar.getTimeInMillis());		
		//
		// This should really be a single transaction - for referential integrity
		//
		TransactionEntry transaction = new TransactionEntry(0L, entry.getChildId(), date, itemEntry.getText().toString().trim(), amount);
		transactionRepository.addTransaction(transaction);
		entry.setBalance(entry.getBalance() + amount);
		repository.updateChild(entry);

    	Intent intent = new Intent(this, ChildTransactionActivity.class);
    	intent.putExtra(ExtraTagConstants.EXTRA_CHILD_ENTRY, entry);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
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

	public static class DatePickerFragment extends DialogFragment
    implements OnDateSetListener {
		
		private Calendar calendar = GregorianCalendar.getInstance();
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {			
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year , month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			calendar.set(year, month, day);
			ChildNewTransactionActivity activity = (ChildNewTransactionActivity) getActivity();
			activity.setCalendar(calendar);
			activity.getFragmentManager().popBackStack();
		}

		public void setCalendar(Calendar calendar) {
			this.calendar = calendar;
		}

	}
}
