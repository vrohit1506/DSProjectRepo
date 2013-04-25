'''
Created on Mar 26, 2013

@author: Ruchi Kandoi
'''
import sqlite3
import json
import os

class database():
    
    def init(self,filename):
        self.conn = sqlite3.connect(filename, check_same_thread = False)
        self.c = self.conn.cursor()
        self.event_id = 1000
#         
#         self.c.execute("DROP table if exists user")
#         self.c.execute("DROP table if exists event")
#         # Create table
        
        self.c.execute('''CREATE TABLE IF NOT EXISTS user
            (name text, username text, password text, email text, ip text)''')    
        self.c.execute('''CREATE TABLE IF NOT EXISTS event
           (event_name text,publisher text, time_stamp real,event_id real, subscribers text)''')        
        self.conn.commit()
        
    def add_entry_user(self,username,password,name,email,ip):
        user_data = (name,username,password,email,ip)
        self.c.execute("INSERT INTO user VALUES (?,?,?,?,?)",user_data)
        self.conn.commit()

#event number and subscribers to be added    
    def add_event_entry(self,publisher,event_name):
        event_id = self.get_event_id()
        print(event_id)
        publisher = publisher + "#"+str(self.get_ip(publisher))
        s = {"0":publisher}
        subscriber_null = json.dumps(s)
        event_data = (event_name,publisher,0, event_id,subscriber_null)
        self.c.execute("INSERT INTO event VALUES {}".format(event_data))
        self.conn.commit()
        return event_id

    
    def verify_password(self,username,password):
        self.c.execute("SELECT * from user where username = ?",[(username)])
        row = self.c.fetchone()
        if row == None:
            return False
        retval = True if (row[2] == password) else False
        return retval

    def check_username(self,username):
        self.c.execute("SELECT * from user where username = ?",[(username)])
        data = self.c.fetchone()
        if data == None:
            return True
        return False
    
    def get_event_id(self):
        path = "."
        num_events = sum(os.path.isdir(os.path.join(path, f)) for f in os.listdir(path))
        self.event_id = num_events + 1001
        return self.event_id
    
    def get_time_stamp(self,event_id):
        self.c.execute("SELECT * from event where event_id = ?",[(event_id)])
        time_stamp = self.c.fetchone()
        if time_stamp == None:
            return 0
        return time_stamp[2]
    
    def update_time_stamp(self,event_id):
        self.c.execute("SELECT * from event where event_id = ?",[(event_id)])
        event = self.c.fetchone()
        if event == None:
            return 0
        event1 = (str(event[0]),str(event[1]),int(event[2]) + 1,int(event[3]),str(event[4]))
        print event1
    
        self.c.execute("delete from event where event_id = ?",[(event_id)])
        self.conn.commit()
        self.c.execute("INSERT INTO event VALUES {}".format(event1))
        self.conn.commit()
        
        return
        
    def add_subscriber(self,event_id,username,ip):
        print("Add Subscriber")
        self.c.execute("SELECT * from event where event_id = ?",[(event_id)])
        event = self.c.fetchone()
        if event == None:
            return 0
        subscriber_string = event[4]
        print(subscriber_string)
        subscriber = json.loads(subscriber_string)
        count = len(subscriber)
        print(count)
        new_string = username + "#"+str(ip)
        print(new_string)
        subscriber.update({str(count):new_string})
        print(subscriber)
        subscriber_string = json.dumps(subscriber)
        print (subscriber_string)
        event1 = (str(event[0]),str(event[1]),int(event[2]),int(event[3]),subscriber_string)
        print event1
        self.c.execute("delete from event where event_id = ?",[(event_id)])
        self.conn.commit()
        self.c.execute("INSERT INTO event VALUES {}".format(event1))
        self.conn.commit()      
        return subscriber
    
    
    
    def get_ip(self, username):
        self.c.execute("SELECT * from user where username = ?",[(username)])
        user = self.c.fetchone()
        if user == None:
            return 0
        ip = user[4]
        print("Ip read from database is " + ip)        
        return ip




    def get_subscriber(self,event_id):
        print("Get Subscriber")
        self.c.execute("SELECT * from event where event_id = ?",[(event_id)])
        event = self.c.fetchone()
        if event == None:
            return 0
        return event[4]

          
          
# Insert a row of data
#c.execute("INSERT INTO user VALUES ('RuchiKandoi','pass@123')")

#db = database()
#db.init("example.txt")

# Larger example that inserts many records at a time
#names = [('AnujaShah','anuja','pass@123','aj@g.com'),
#         ('Arjun','arjun', 'pass@123','aa.@a.com'),
#         ('Rohit', 'rohit','pass@123','rv@a.com'),
#        ]

#db.add_entry_user("AnujaShah", "pass@123", "Anuja", "aa@gmail.com")
#check = db.check_username("AnujaShah")
#print (check)
#c.executemany('INSERT INTO user VALUES (?,?)', names)
# Save (commit) the changes
#conn.commit()

#print t
# We can also close the connection if we are done with it.
# Just be sure any changes have been committed or they will be lost.

#for row in c.execute('SELECT * FROM user'):
#        print row
     
    