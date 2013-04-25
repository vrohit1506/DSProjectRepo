package com.example.photojournal;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.EventLog.Event;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Home extends Activity {

	//Network Information
	protected int port;
	protected String ip;
	protected Socket client;
	protected ServerSocket server;
	protected BufferedReader in;
	protected PrintWriter out;
	protected String reply;
	protected JSONObject elements;
	protected boolean loginverified;
	
	//Event id
	protected int event_id;
	
	ListView listView;
	ArrayAdapter<String> listAdapter;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		listAdapter = new ArrayAdapter<String>(this, R.layout.activity_home);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(listAdapter);
		listAdapter.add("hello");
		listAdapter.add("how");
		listAdapter.add("are");
		listAdapter.add("khana khake jana");

		
//      ArrayList<Event> events = new ArrayList<Event>();
//		events = get_event_list();

	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_home, menu);
		return true;
	}
	
	//create event
	public void onClick(View view){
		Intent intent = new Intent(this, CreateEvent.class);
		startActivity(intent);
	}
	

}
