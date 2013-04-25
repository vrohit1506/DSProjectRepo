'''
Created on Mar 24, 2013

'''
import socket 
from Request import *
from Database import *
import base64
import os
#import ast
#import pickle


#class server:
    
        
def main():
    s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    host = socket.gethostname()
    port  = 1234
    #host = socket.gethostbyname(host)
    print(host)
    print "server_started" 
    s.bind(("128.237.225.6",port))
    s.listen(5)
    db = database()
    db.init("Database.txt") 

    while True:
        print'--------------------------------------------------'
        print "waiting for clients..."
        socket_name,addr  = s.accept()
        print "Connected to {}".format(addr)
        
        msg = socket_name.recv(1024)
        print(msg)
        result = eval(msg)
 
        Req = Request(db, socket_name)
         
        Req.handle_request(result, addr)
        print "\nfinished\n"
        print '----------------------------------------------------'
        
if __name__ == "__main__" :
    main()



