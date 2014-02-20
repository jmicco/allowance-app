package org.jmicco.allowanceapp;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ChildEntryAdapter extends ArrayAdapter<ChildRepository.ChildEntry> {

	private final int layoutResourceId;
	
	public ChildEntryAdapter(Context context, int layoutResourceId, List<ChildRepository.ChildEntry> children) {
		super(context, layoutResourceId, children);
		this.layoutResourceId = layoutResourceId;
	}
	
	@Override
	public View getView(int position, View row, ViewGroup parent) {
		Context context = getContext();
		ChildEntryHolder holder = null;
		
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			
			holder = new ChildEntryHolder(
					(TextView) row.findViewById(R.id.child_name_entry),
					(TextView) row.findViewById(R.id.child_balance_entry)
					);
			row.setTag(holder);			
		}
		else {
			holder = (ChildEntryHolder) row.getTag();
		}
		ChildRepository.ChildEntry childEntry = getItem(position);
		holder.nameView.setText(childEntry.getName());
		holder.balanceView.setText(childEntry.getFormattedBalance());
		
		return row;
	}
	
	static class ChildEntryHolder {
		final TextView nameView;
		final TextView balanceView;	
		
		ChildEntryHolder(TextView nameView, TextView balanceView) {
			this.nameView = nameView;
			this.balanceView = balanceView;
		}
	}
}
