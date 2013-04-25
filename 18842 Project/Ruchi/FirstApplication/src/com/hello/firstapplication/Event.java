package com.hello.firstapplication;

public class Event {

	String name;
	int event_id;
	String publisher;
	int time_stamp;
	String path;
	
	public Event(String name,int id,String publisher, int time_stamp) {
		this.name = name;
		this.event_id = id;
		this.publisher = publisher;
		this.time_stamp = time_stamp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getEvent_id() {
		return event_id;
	}

	public void setEvent_id(int event_id) {
		this.event_id = event_id;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public int getTime_stamp() {
		return time_stamp;
	}

	public void setTime_stamp(int time_stamp) {
		this.time_stamp = time_stamp;
	}
	
	
}
