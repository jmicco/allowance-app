package org.jmicco.allowanceapp;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		OnDateSetListener {

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
		ActivityWithSetCalendar activity = (ActivityWithSetCalendar) getActivity();
		activity.setCalendar(calendar);
		activity.getFragmentManager().popBackStack();
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}
	
	public static abstract class ActivityWithSetCalendar extends Activity {
		public abstract void setCalendar(Calendar calendar);
	}
}
