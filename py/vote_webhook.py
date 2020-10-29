import os
import flask
from flask import Flask
import mysql.connector

user = os.environ['MYSQL_USER']
password = ''

connection = mysql.connector.connect(user=user,
                                     password=password,
                                     host='127.0.0.1',
                                     database='nano')
cursor = connection.cursor()
insert_vote_query = """
    INSERT INTO vote (user_id, weekend)
    VALUES ({}, {});
"""
update_rewards_query = """
    UPDATE USER
    SET RECOMMENDATION_QUOTA = RECOMMENDATION_QUOTA + 1
    WHERE ID = {};
"""
update_rewards_weekend_query = """
    UPDATE USER
    SET RECOMMENDATION_QUOTA = RECOMMENDATION_QUOTA + 3
    WHERE ID = {};
"""

VOTE_AUTH_TOKEN = os.environ["VOTE_AUTH_NANO"]

app = Flask(__name__)

@app.route('/', methods = ['POST'])
def vote_post():
    
    headers = dict(flask.request.headers)
    # Invalid Authorization code
    if headers["Authorization"] != VOTE_AUTH_TOKEN:
        return 'error', 401
    
    data = flask.request.get_json(force=True)

    user_id = data['user']
    bot_id = data['bot']
    is_weekend = data['isWeekend']
    request_type = data["type"]
    query = data['query']

    if request_type == 'upvote':
        print("Upvote", data)
        cursor.execute(insert_vote_query.format(user_id, int(is_weekend)))
        cursor.execute(update_rewards_query.format(user_id))
        connection.commit()
    else:
        print("Test", data)

        # Vote history
        cursor.execute(insert_vote_query.format(user_id, int(is_weekend)))

        # Give more rewards if weekend
        if !is_weekend:
            cursor.execute(update_rewards_query.format(user_id))
        else:
            cursor.execute(update_rewards_weekend_query.format(user_id))
            
        connection.commit()
    
    return 'success', 200

if __name__ == '__main__':
    app.run(host="0.0.0.0", port="5000")
    print("End")
    
cursor.close()
connection.close()
