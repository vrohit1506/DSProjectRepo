package com.example.photojournal;

import java.io.Serializable;


public class Event implements Serializable {

	String name;
	long event_id;
	String publisher;
	long time_stamp;
	
	public Event(){}
	public Event(String name,long id,String publisher, long time_stamp) {
		this.publisher = publisher;
		this.name = name;
		this.event_id = id;
		
		this.time_stamp = time_stamp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getEvent_id() {
		return event_id;
	}

	public void setEvent_id(long event_id) {
		this.event_id = event_id;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public long getTime_stamp() {
		return time_stamp;
	}

	public void setTime_stamp(long time_stamp) {
		this.time_stamp = time_stamp;
	}
	
	
}
