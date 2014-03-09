package org.jmicco.allowanceapp;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Add a comment to see if a push to GIT works from Eclipse
 * @author jmicco
 *
 */
public class AddChildActivity extends Activity {
	@Override
	protected void onResume() {
		super.onResume();
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
	}

	EditText childName;
	private ChildRepository repository;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_child);
		childName = (EditText) findViewById(R.id.child_name);
        repository = MainActivity.getChildRepository();
	}
    
    public void addChild(View view) {
    	String name = childName.getText().toString().trim();
    	if (name.isEmpty()) {
    		childName.setError("Please enter a child's name");
    		return;
    	}
    	ChildRepository.ChildEntry entry = repository.getChild(name);
    	if (entry != null) {
    		childName.setError(String.format(Locale.getDefault(), "%s is the name of an existing child", name));
    		return;
    	}
    	entry = new ChildRepository.ChildEntry(0, name, 0.0);
    	repository.addChild(entry);
    	Intent intent = new Intent(this, MainActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
