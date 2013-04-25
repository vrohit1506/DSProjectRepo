'''
Created on Mar 24, 2013

@author: Ruchi Kandoi
'''
from Server import *
from Database import *
from Subscriber import *
import json
import socket
import os
import threading

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
    
    def handle_request(self,result, addr):
        request = result["request"]
        print("In Handle request: request = {}".format(request))
        if request == None:
            return
        elif request == "signin":
            self.handle_signin(result)
        elif request == "signup":
            self.handle_signup(result,addr)
        elif request == "new_event":
            self.handle_new_event(result)
        elif request == "get_time_stamp":
            self.handle_get_time_stamp(result)
        elif request == "add_comment":
            self.add_comment_helper(result) 
        elif request == "add_image":
            self.add_image_helper(result)            
        elif request == "add_subscriber":
            self.add_subscriber(result)
        elif request == "get_subscriber":
            self.get_subscriber(result)
        elif request == "joining_event":
            self.joining_event(result)
        else:
            self.send_response("No")
        return 



    def handle_new_event(self,result):
        print("New Event")
        #receive the evant details and create an entry in the database
       
        event_name = result["event_name"]
        publisher = result["publisher"]
        event_id = self.db.add_event_entry(publisher,event_name)
        print("Event Id: {}".format(event_id))
        
        #send the event id back to the client
        self.send_response(str(event_id))
        
        #make a directory to store the data files and a log file
        file_name = str(event_id)+"\\log.txt"
        os.makedirs(str(event_id))
        fd = open(file_name,"wb")
        fd.close
        return    
    
    
    
    def handle_get_time_stamp(self,result):
        print("Get Time Stamp")
        
        #receive event details to fetch the time_stamp       
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
   
   
   
    
    def add_image_helper(self,result):
        print("add image helper")
        event_id = result["event_id"]
        photo_id = result["time_stamp"]
        tag_line = result["tag_line"]
        size = result["size"]
        self.handle_add_image(event_id,photo_id,tag_line,size)
    
    
    
    
    def handle_add_image(self,event_id,photo_id, tag_line,size):        
        self.send_response("Yes")        
        path = str(event_id)+"\\"+str(photo_id)
        print(path)
        os.makedirs(path)
         
        print('add_image') 
        filename = path+"\\1.jpg"
        print(filename)
        
        count = long(size)
        print count        
        fd = open(filename,"wb")
        
        while (count > 0):
            data = self.socket_name.recv(1)
            fd.write(data)
            count = count - 1
            
        fd.close()
         
        # Adding tag_line to a file with name 2.txt
        taglineFile = path+"\\2.txt"
        print(taglineFile) 
        fd = open(taglineFile,"wb")
        fd.write(tag_line)
        fd.close      
         
        self.send_response("Yes")        
        self.db.update_time_stamp(event_id)    
        return
    
    
    
    
    def add_comment_helper(self,result):
        event_id = result["event_id"]
        time_stamp = result["time_stamp"]
        comment = result["comment"]
        self.handle_add_comment(event_id,time_stamp, comment)
    
    
    
    
    def handle_add_comment(self,event_id,time_stamp, comment):
        path = str(event_id)+"\\"+str(time_stamp)
         
        # Generating count
        num_files = sum(os.path.isfile(os.path.join(path, f)) for f in os.listdir(path))
        count = num_files + 1         
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
    
    
    
    
    def handle_signin(self,result):
        print ("Handle_signin")
        username = result["username"]
        password = result["password"]
        verify = self.db.verify_password(username, password)
        print("password verified {}".format(verify))
        if verify == True:
            self.send_response("Yes")
        else:
            self.send_response("No")
        return
    
    
    
    
    def handle_signup(self,result, addr):
        print "handle sign_up"
        username = result["username"]
        password = result["password"]
        name = result["name"]
        email = result["email"]
        ip = addr[0]
        print ("received ip " + str(ip))
        check = self.db.check_username(username)
        print ("Username check: {}".format(check))
        if check == True:
            self.db.add_entry_user(username, password, name, email, str(ip))
            print("Ip of this user " + str(ip))
            self.send_response("Yes")
        else:
            self.send_response("Exists")
        return


    def joining_event(self, result):
        print "joining_event"
        event_id = result["event_id"]
        username = result["username"]  
        
        ip = self.db.get_ip(username)        
        if (ip == 0):                
            subscriber_string = self.db.add_subscriber(event_id, username, ip)
            self.send_response("Yes")
            
            # Now multicast the updated subscriber List = "subscriber_string" to all users
            if (subscriber_string != 0):
                print("Subscriber string")
                print(subscriber_string)  
                count = len(subscriber_string)
                print ("count " + str(count))                        
                            
                # Create threads and for each thread create a connection with that particular ip and send the subscriber list to it                    
                thread1 = Subscriber(event_id, subscriber_string) 
                thread1.start()                  
        return



    def add_subscriber(self,result):
        print "add_subscriber"
        new_subscriber = result["subscriber"]       
        # Check if this person exists, if not send No, otherwise send Yes and ip of this person
        ip = self.db.get_ip(new_subscriber)        
        if (ip == 0):
            self.send_response("No")
        else:            
            print("IP received from database is " + ip)
            self.send_response("Yes")
            self.send_response(ip)
        return
    
    
    
    
    def get_subscriber(self,result):
        print "get_subscriber"
        event_id = result["event_id"]
        
        subscriber_list = self.db.get_subscriber(event_id)
        self.send_response(subscriber_list)
        return