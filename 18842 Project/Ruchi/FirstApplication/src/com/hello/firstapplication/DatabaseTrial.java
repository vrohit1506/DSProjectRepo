package com.hello.firstapplication;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DatabaseTrial extends Activity {

	ArrayAdapter<String> listAdapter;
	ListView listView;
	MySQLiteHelper db;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_trial);
        
        listAdapter = new ArrayAdapter<String>(this, R.layout.device_name, R.id.TextView1);
		listView = (ListView) findViewById(R.id.listView2);
		listView.setAdapter(listAdapter);
		db = new MySQLiteHelper(getApplicationContext());

		
		   ArrayList<Event> events = db.get_event_list(getApplicationContext());
			
			for(int i = 0;i<events.size();i++)
				listAdapter.add(events.get(i).getName());
	    
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_database_trial, menu);
        return true;
    }
}
