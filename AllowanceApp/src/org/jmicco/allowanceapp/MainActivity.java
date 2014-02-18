package org.jmicco.allowanceapp;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * This is the main activity for the child Allowance app
 * 
 * @author John
 *
 */
public class MainActivity extends Activity {
	private static final String LOG_TAG = MainActivity.class.getSimpleName();

	ListView childList;
	private ChildRepository childRepository;
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        childRepository = setupDatabase();
        childRepository.open();
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "onCreate");
        childList = (ListView) findViewById(R.id.child_list);
    }

	private ChildRepository setupDatabase() {		
        childRepository = new ChildRepositorySqlLite(this); 
        return childRepository;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		childRepository.close();
        Log.d(LOG_TAG, "onDestroy");
	}

	@Override
	protected void onResume() {
		super.onResume();
        ListAdapter adapter = new ChildEntryAdapter(this, R.layout.child_list_layout, childRepository.getChildren());
        childList.setOnItemClickListener(new ClickListener());
		childList.setAdapter(adapter);
        Log.d(LOG_TAG, "onResume");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_child:
			Log.d(LOG_TAG, "Add Child Selected");
	    	Intent intent = new Intent(this, AddChildActivity.class);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	    	startActivity(intent);
	    	return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public class ClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ChildEntryAdapter adapter = (ChildEntryAdapter) parent.getAdapter();
			ChildEntry entry = adapter.getItem(position);			
			Log.d(LOG_TAG, "item Clicked " + entry.getName());
	    	Intent intent = new Intent(MainActivity.this, ChildTransactionActivity.class);
	    	intent.putExtra(ExtraTagConstants.EXTRA_CHILD_ID, entry.getChildId());	    	
	    	startActivity(intent);
		}
	}

}
