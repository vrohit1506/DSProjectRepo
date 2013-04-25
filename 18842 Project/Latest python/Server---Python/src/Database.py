'''
Created on Mar 26, 2013

@author: Ruchi Kandoi
'''
import sqlite3

class database():
    
    def init(self,filename):
        self.conn = sqlite3.connect(filename)
        self.c = self.conn.cursor()
        self.event_id = 1000
        
        self.c.execute("DROP table if exists user")
        self.c.execute("DROP table if exists event")
        # Create table
        self.c.execute('''CREATE TABLE user
            (name text, username text, password text, email text)''')    
        self.c.execute('''CREATE TABLE event
           (event_name text,publisher text, time_stamp real,event_id real)''')        
        self.conn.commit()
        
    def add_entry_user(self,username,password,name,email):
        user_data = (name,username,password,email)
        self.c.execute("INSERT INTO user VALUES (?,?,?,?)",user_data)
        self.conn.commit()

#event number and subscribers to be added    
    def add_event_entry(self,publisher,event_name):
        event_id = self.get_event_id()
        print(event_id)
        event_data = (event_name,publisher,0, event_id)
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
        self.event_id += 1
        return self.event_id
    
    def get_time_stamp(self,event_id):
        self.c.execute("SELECT * from event where event_id = ?",[(event_id)])
        time_stamp = self.c.fetchone()
        if time_stamp == None:
            return 0
        return time_stamp[2]
            
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
     
    