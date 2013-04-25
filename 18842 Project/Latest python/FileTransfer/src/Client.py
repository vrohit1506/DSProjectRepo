'''
Created on Mar 28, 2013

@author: Anuja
'''
import socket
import base64
import binascii
import json
import os

# MAXLINESIZE = 76 # Excluding the CRLF
# MAXBINSIZE = (MAXLINESIZE//4)*3
# 
# 
# def decode1(input, output):
#     """Decode a file."""
#     while True:
#         line = input.readline()
#         if not line:
#             break
#         s = binascii.a2b_base64(line)
#         output.write(s)
# 
# 
# def encode1(input, output):
#     """Encode a file."""
#     while True:
#         s = input.read(MAXBINSIZE)
#         if not s:
#             break
#         while len(s) < MAXBINSIZE:
#             ns = input.read(MAXBINSIZE-len(s))
#             if not ns:
#                 break
#             s += ns
#         line = binascii.b2a_base64(s)
#         output.send(line)


s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
host = socket.gethostname()
port = 1234





print 'Connecting to ', host, port
s.connect(("192.168.1.106",port))

## NEW_EVENT

# request = {'request':'new_event'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)
# 
# request = {'event_name':'CMU_Trip', 'publisher':'Anuja'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)




## ADD IMAGE

# request = {'request':'add_image'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)
#  
# request = {'event_id':'1001', 'time_stamp':'1','tag_line':'My photo tag line'}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)
#  
# size = os.stat('anujas.jpg').st_size
# print(size)
# request = {'size':size}
# a = json.dumps(request, sort_keys = False, separators = (',',':'))
# print (a)
# s.send(a)
#  
# fd = open('anujas.jpg','rb')
# data = fd.read()
# s.send(data)




## ADD COMMENT TO EXISTING IMAGE
request = {'request':'add_comment'}
a = json.dumps(request, sort_keys = False, separators = (',',':'))
print (a)
s.send(a)
  
request = {'event_id':'1001', 'time_stamp':'1','count':'3', 'comment':'This trip was soooooooooo much fun!!!'}
a = json.dumps(request, sort_keys = False, separators = (',',':'))
print (a)
s.send(a)



## END OF EVENT
response = s.recv(1024)
print response
print('finished')
s.close()


# fd = open("img2.jpg", "rb")
# 
# msg = raw_input('Ready?')
# 
# line = fd.read(1024)
# while (line):
#     s.send(line)
#     print line
#     line = fd.read(1024)


# encode1(fd, s)

# line = fd.read(1024)
# while (line):
#     s.send(line)
#     print line
#     line = fd.read(1024)
#     msg = raw_input('Client >> ')
#     s.send(msg)
#     msg = s.recv(1024)
#     print 'SERVER >> ', msg

# s.close()




    