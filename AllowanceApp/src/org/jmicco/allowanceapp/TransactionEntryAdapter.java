package org.jmicco.allowanceapp;

import java.text.DateFormat;
import java.util.List;

import org.jmicco.allowanceapp.TransactionRepository.TransactionEntry;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TransactionEntryAdapter extends ArrayAdapter<TransactionEntry> {

	private final int layoutResourceId;
	
	public TransactionEntryAdapter(Context context, int layoutResourceId, List<TransactionRepository.TransactionEntry> transactions) {
		super(context, layoutResourceId, transactions);
		this.layoutResourceId = layoutResourceId;
	}
	
	@Override
	public View getView(int position, View row, ViewGroup parent) {
		Context context = getContext();
		TransactionEntryHolder holder = null;
		
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			
			holder = new TransactionEntryHolder(
					(TextView) row.findViewById(R.id.date),
					(TextView) row.findViewById(R.id.description),
					(TextView) row.findViewById(R.id.amount)
					);
			row.setTag(holder);			
		}
		else {
			holder = (TransactionEntryHolder) row.getTag();
		}
		TransactionRepository.TransactionEntry transactionEntry = getItem(position);
		DateFormat dateFormat = DateFormat.getDateInstance();
		holder.dateView.setText(dateFormat.format(transactionEntry.getDate()));
		holder.amountView.setText(transactionEntry.getFormattedAmount());
		holder.descriptionView.setText(transactionEntry.getDescription());
		
		return row;
	}
	
	static class TransactionEntryHolder {
		final TextView dateView;
		final TextView descriptionView;
		final TextView amountView;
		
		TransactionEntryHolder(TextView dateView, TextView descriptionView, TextView amountView) {
			this.dateView = dateView;
			this.descriptionView = descriptionView;
			this.amountView = amountView;
		}
	}
}
