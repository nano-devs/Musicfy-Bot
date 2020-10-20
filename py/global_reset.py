import os
import datetime
import mysql.connector

user = os.environ['MYSQL_USER']
password = ''

connection = mysql.connector.connect(user=user,
                                     password=password,
                                     host='127.0.0.1',
                                     database='nano')
cursor = connection.cursor()

# RESET DAILY QUOTA
query = """
    UPDATE USER
    SET DAILY_QUOTA = 1
    WHERE DAILY_QUOTA = 0;
"""
cursor.execute(query)

# CREATE HISTORY
query = """
    INSERT INTO RESET_HISTORY (RESET_TIME)
    VALUES ('{}')
"""
time_now = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
cursor.execute(query.format(time_now))

# Commit data to database
connection.commit()

cursor.close()
connection.close()

print('RESET DAILY QUOTA AT', time_now)
