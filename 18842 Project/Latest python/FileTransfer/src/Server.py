'''
Created on Mar 28, 2013

@author: Anuja
'''
import socket
import base64
import binascii
import os

# MAXLINESIZE = 76 # Excluding the CRLF
# MAXBINSIZE = (MAXLINESIZE//4)*3
# 
# 
# def decode1(input, output):
#     """Decode a file."""
#     while True:
#         line = input.recv(1024)
#         if not line:
#             break
#         s = binascii.a2b_base64(line)
#         output.write(s)


s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
host = socket.gethostname()
print host
port = 12345

print 'Server Started!'
print 'Waiting for clients...'

s.bind(("128.237.244.232",port))
s.listen(5)
clientSocket, address = s.accept()
print 'Connection made to ', address



fd = open('img2New.jpg','wb')
line = clientSocket.recv(1024)
while(line):
    fd.write(line)
    line = clientSocket.recv(1024)

fd.close() 
clientSocket.close() 
# fd.write(base64.decodestring(line))

# decode1(clientSocket, fd)

# line =  clientSocket.recv(1024)
# while(line):
#     line1 = None
#     base64.decode(line, line1)
#     fd.write(line1)
#     line =  clientSocket.recv(1024)


#print line

    
# fd1 = open('image.jpg','wb')
# image_line = fd1.read(1024)
# while(image_line):
#     fd.write(image_line)
#     image_line = fd1.read(1024)
 
# fd1.close()
    


    
