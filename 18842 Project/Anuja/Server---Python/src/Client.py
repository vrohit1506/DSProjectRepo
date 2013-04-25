'''
Created on Mar 28, 2013

@author: Anuja
'''
import socket
import base64
import binascii
import json
import os


s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
host = socket.gethostname()
port = 1234



print 'Connecting to ', host, port
s.connect(("128.237.233.230",port))


# # REGISTER USER
# request = {'request':'signup','username': 'Anuja','password':'pass@123','name':"Anuja","email":"anuja@gmail.com"}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)

 
# ## SIGN_IN
# request = {'request':'signin','username': 'Anuja','password':'pass@123'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)
 
 
# # NEW_EVENT          
# request = {'request':'new_event','event_name':'CMU_Trip1', 'publisher':'Anuja'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)
               



# # ADD SUBSCRIBER       
# request = {'request':'add_subscriber','event_id':'1001', 'subscriber':'Rohit'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)
# response = s.recv(1024)


# # JOINING EVENT
# request = {'request':'joining_event','event_id':'1001', 'username':'Rohit'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)


## GET SUBSCRIBER 
# request = {'request':'get_subscriber','event_id':'1001'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)

####  
#response = s.recv(1024)
#print response
#subs = json.loads(response)
#print (subs)

## ADD IMAGE

## GET TIME STAMP
# 
# request = {'request':'get_time_stamp','event_id':'1001'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)

###receive time stamp----increment and use further

# 
# size = os.stat('anujas.jpg').st_size
# request = {'request':'add_image','event_id':'1001', 'time_stamp':'1','tag_line':'My photo tag line','size':size}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)
#  
#  
# print(s.recv(1024))
# print(s.recv(1024))
#    
# fd = open('anujas.jpg','rb')
# data = fd.read()
# s.send(data)




## ADD COMMENT TO EXISTING IMAGE
# 
# request = {'request':'add_comment','event_id':'1001', 'time_stamp':'1', 'comment':'Anuja is MAD!!!'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)



## END OF EVENT
response = s.recv(1024)
print response
print('finished')
s.close()