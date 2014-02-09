package org.jmicco.allowanceapp;

import org.jmicco.allowanceapp.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * This is the main activity for the child Allowance app
 * 
 * @author John
 *
 */
public class MainActivity extends Activity {
	public static final String EXTRA_MESSAGE = "org.jmicco.myfirstapp.MESSAGE";
	int counter = 101;
	EditText childName;
	ListView childList;
	private ChildRepository repository;
	
	private void restoreState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
	       	counter = savedInstanceState.getInt("counter");
	       	String text = (String) savedInstanceState.get("text");
	       	childName.setText(text);
	    }
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new ChildRepositorySqlLite(this); 
        repository.open();
        setContentView(R.layout.activity_main);
        System.out.println("onCreate");
        childName = (EditText) findViewById(R.id.child_name);
        childList = (ListView) findViewById(R.id.child_list);
        restoreState(savedInstanceState);
		System.out.println("onCreate: " + counter + " text: " + childName.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void addChild(View view) {
    	String name = childName.getText().toString();  	
    	ChildRepository.ChildEntry entry = new ChildRepository.ChildEntry(0, name, 0.0);
    	repository.addChild(entry);
    	fillListView();    	
    }
    
    private void fillListView() {
    	ListAdapter adapter = new ChildEntryAdapter(this, R.layout.child_list_layout, repository.getChildren());
		childList.setAdapter(adapter);
	}

	public void sendMessage(View view) {
    	// Do something in response to button
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	intent.putExtra(EXTRA_MESSAGE, childName.getText().toString());
    	startActivity(intent);
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
		repository.close();
        System.out.println("onDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
        System.out.println("onPause");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
        restoreState(savedInstanceState);
		System.out.println("onRestoreInstanceState: " + counter + " text: " + childName.getText().toString());
	}

	@Override
	protected void onRestart() {
		super.onRestart();
        System.out.println("onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
        fillListView();
        System.out.println("onResume");
        counter++;
	}

	@Override
	protected void onStart() {
		super.onStart();
        System.out.println("onStart");
	}

	@Override
	protected void onStop() {
		super.onStop();
        System.out.println("onStop");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("counter", counter);
		String text = childName.getText().toString();
		outState.putString("text", text);
		System.out.println("onSaveInstanceState: " + counter + " text: " + text);
	}




}
