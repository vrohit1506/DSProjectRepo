'''
Created on Apr 24, 2013

@author: Anuja
'''
import threading
from Database import *
import socket

class Subscriber(threading.Thread):
    def __init__(self, event_id, subscriber_string):
        threading.Thread.__init__(self)
        self.event_id = event_id
        self.subscriber_string = subscriber_string
        self.db = database()
        self.db.init("Database.txt") 
                

    def run(self):
        for ctr in self.subscriber_string:                     
            print ("Username =" + self.subscriber_string[ctr])
            details = self.subscriber_string[ctr].split('#')            
#             ip = self.db.get_ip(details[0]) 
            ip = details[1]
            print("ip = " + ip)  
            
            thread1 = SendList(self.event_id, self.subscriber_string, ip) 
            thread1.start()


class SendList(threading.Thread):
    def __init__(self, event_id, subscriber_string, ip, event_name):
        threading.Thread.__init__(self)
        self.event_id = event_id
        self.subscriber_string = subscriber_string
        self.ip = ip
        self.event_name = event_name;
                

    def run(self):
        s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        port  = 4321
        
        print 'Connecting to ', self.ip, port
        s.connect((self.ip,port))
        
        response = {"subscriber_list":self.subscriber_string,"event_id":self.event_id,"event_name":self.event_name,"request":"subscriber_list"}
        a = json.dumps(response, sort_keys = False, separators = (',',':'))
        print (a)
        s.send(a)
        s.send("\n")
        
        print('finished')
        s.close()