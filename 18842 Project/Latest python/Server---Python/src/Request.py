'''
Created on Mar 24, 2013

@author: Ruchi Kandoi
'''
from Server import *
from Database import *
import json
import socket
import os

class Request:
    def __init__(self,db,socket_name):
        self.socket_name = socket_name
        self.db = db
     
    def send_response (self,resp):
        print ("send_response  {}".format(resp))
        response = {"response":resp}
        a = json.dumps(response, sort_keys = False, separators = (',',':'))
        print (a)
        self.socket_name.send(a)
        self.socket_name.send("\n")
        return
    
    def handle_request(self,request):
        print("In Handle request: request = {}".format(request))
        if request == None:
            return
        elif request == "signin":
            self.handle_signin()
        elif request == "signup":
            self.handle_signup()
        elif request == "new_event":
            self.handle_new_event()
        elif request == "get_time_stamp":
            self.handle_get_time_stamp()
        elif request == "add_comment":
            self.add_comment_helper() 
        elif request == "add_image":
            self.add_image_helper()            
        return 
    
    def handle_new_event(self):
        print("New Event")
        #receive the evant details and create an entry in the database
        msg = self.socket_name.recv(1024)
        print(msg)
        result = eval(msg)
        event_name = result["event_name"]
        publisher = result["publisher"]
        event_id = self.db.add_event_entry(publisher,event_name)
        print("Event Id: {}".format(event_id))
        
        #send the event id back to the client
        self.send_response(str(event_id))
        
        #make a directory to store the data files
        os.makedirs(str(event_id))
        return    
    
    def handle_get_time_stamp(self):
        print("Get Time Stamp")
        
        #receive event details to fetch the time_stamp
        msg = self.socket_name.recv(1024)
        result = eval(msg)
        event_id = result["event_id"]
        time_stamp = self.db.get_time_stamp(event_id)  
        print("Time Stamp: {}".format(time_stamp))
        
        #send the time stamp of that particular event
        self.send_response(time_stamp)
        return
    
    def handle_request_file(self,time_stamp,event_id):
        file_name = str(event_id) + ".txt"
        print(file_name)
        fp = open(file_name,"rb")
        offset = time_stamp % 1000
        for i, line in enumerate(fp):
            if i == offset:
                break
        fp.close()
        fd = open(line,"rb")
        data = fd.read()
        self.socket_name.send(data)
        return
    
    def add_image_helper(self):
        msg = self.socket_name.recv(1024)
        result = eval(msg)
        event_id = result["event_id"]
        time_stamp = result["time_stamp"]
        tag_line = result["tag_line"]
        self.handle_add_image(event_id,time_stamp,tag_line)
    
    def handle_add_image(self,event_id,time_stamp, tag_line):
        path = str(event_id)+"\\"+str(time_stamp)
        print(path)
        os.makedirs(path)
         
        print('add_image') 
        filename = path+"\\1.jpg"
        print(filename)
          
        fd = open(filename,"wb")
        msg = self.socket_name.recv(1024)
        print(msg)
        result = eval(msg)
        size = result["size"]
        data = self.socket_name.recv(int(size))
        fd.write(data)
        fd.close()
         
        # Adding tag_line to a file with name 2.txt
        taglineFile = path+"\\2.txt"
        print(taglineFile) 
        fd = open(taglineFile,"wb")
        fd.write(tag_line)
        fd.close      
         
        self.send_response("Yes")
        return
    
    def add_comment_helper(self):
        msg = self.socket_name.recv(1024)
        result = eval(msg)
        event_id = result["event_id"]
        time_stamp = result["time_stamp"]
        count = result["count"]
        comment = result["comment"]
        self.handle_add_comment(event_id,time_stamp, count, comment)
    
    def handle_add_comment(self,event_id,time_stamp, count, comment):
        path = str(event_id)+"\\"+str(time_stamp)
         
        print('Entered add_comment') 
        filename = path+"\\"+str(count)+".txt"
        print(filename)
        print(filename)
                  
        # creating a file to store the comment
        fd = open(filename,"wb")
        fd.write(comment)
        fd.close      
         
        self.send_response("Yes")
        return
    
    def handle_signin(self):
        print ("Handle_signin")
        msg = self.socket_name.recv(1024)
        print(msg)
        result = eval(msg)
        username = result["username"]
        password = result["password"]
        verify = self.db.verify_password(username, password)
        print("password verified {}".format(verify))
        if verify == True:
            self.send_response("Yes")
        else:
            self.send_response("No")
        #self.send_response("Yes")
        return
    
    def handle_signup(self):
        print "handle sign_up"
        msg = self.socket_name.recv(1024)
        print(msg)
        result = eval(msg)
        username = result["username"]
        password = result["password"]
        name = result["name"]
        email = result["email"]
        check = self.db.check_username(username)
        print ("Username check: {}".format(check))
        if check == True:
            self.db.add_entry_user(username, password, name, email)
            self.send_response("Yes")
        else:
            self.send_response("Exists")
        return
