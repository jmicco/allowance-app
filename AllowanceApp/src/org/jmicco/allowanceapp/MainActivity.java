package org.jmicco.allowanceapp;

import org.jmicco.allowanceapp.ChildRepository.ChildEntry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

	public static final String EXTRA_CHILD_ENTRY = "org.jmicco.myfirstapp.CHILD_ENTRY";
	ListView childList;
	private ChildRepository repository;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new ChildRepositorySqlLite(this); 
        repository.open();
        setContentView(R.layout.activity_main);
        System.out.println("onCreate");
        childList = (ListView) findViewById(R.id.child_list);
		System.out.println("onCreate");
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
		repository.close();
        System.out.println("onDestroy");
	}

	@Override
	protected void onResume() {
		super.onResume();
        ListAdapter adapter = new ChildEntryAdapter(this, R.layout.child_list_layout, repository.getChildren());
        childList.setOnItemClickListener(new ClickListener());
		childList.setAdapter(adapter);
        System.out.println("onResume");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_child:
			System.out.println("Add Child Selected");
	    	Intent intent = new Intent(this, AddChildActivity.class);
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
			System.out.println("item Clicked " + entry.getName());
	    	Intent intent = new Intent(MainActivity.this, ChildTransactionActivity.class);
	    	intent.putExtra(EXTRA_CHILD_ENTRY, entry);
	    	startActivity(intent);
		}
	}

}
