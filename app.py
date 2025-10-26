from flask import Flask, request, jsonify
from flask_cors import CORS
from pymongo import MongoClient
from datetime import datetime
from bson import ObjectId
import certifi

app = Flask(__name__)
CORS(app)
ca = certifi.where()

# MongoDB 連接設定
MONGO_URI = "mongodb+srv://Amy:123@cluster0.g54wj9s.mongodb.net/?appName=Cluster0"
DB_NAME = "tripDemo-shan"

client = MongoClient(MONGO_URI, tlsCAFile=ca)
db = client[DB_NAME]
client.admin.command('ping')
print("✅ 成功連接到 MongoDB Atlas！")

users_collection = db['users']

def serialize_doc(doc):
    if not doc:
        return doc
    for key, value in list(doc.items()):
        if isinstance(value, ObjectId):
            doc[key] = str(value)
        elif isinstance(value, bytes):
            doc[key] = f"<binary data: {len(value)} bytes>"
        elif isinstance(value, datetime):
            doc[key] = value.isoformat()
        elif isinstance(value, dict):
            doc[key] = serialize_doc(value)
        elif isinstance(value, list):
            doc[key] = [
                serialize_doc(item) if isinstance(item, dict) 
                else str(item) if isinstance(item, (ObjectId, bytes)) 
                else item 
                for item in value
            ]
    return doc

# 1. 建立使用者
@app.route('/users', methods=['POST'])
def create_user():
    data = request.json
    username = data.get('username')
    email = data.get('email')
    
    if not username or not email:
        return jsonify({'error': '請提供 username 和 email'}), 400
    
    if users_collection.find_one({'$or': [{'username': username}, {'email': email}]}):
        return jsonify({'error': '使用者名稱或 email 已存在'}), 400
    
    user_doc = {
        'username': username,
        'email': email,
        'friends': [],
        'pendingRequests': [],
        'sentRequests': [],
        'created_at': datetime.utcnow()
    }
    
    result = users_collection.insert_one(user_doc)
    
    return jsonify({
        'message': '使用者建立成功',
        'user_id': str(result.inserted_id),
        'username': username
    }), 201

# 2. 取得所有使用者
@app.route('/users', methods=['GET'])
def get_all_users():
    try:
        users = list(users_collection.find())
        users = [serialize_doc(user) for user in users]
        
        return jsonify({
            'users': users,
            'count': len(users)
        }), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# 3. 發送好友邀請
@app.route('/friend-requests', methods=['POST'])
def send_friend_request():
    data = request.json
    sender_id = data.get('sender_id')
    receiver_id = data.get('receiver_id')
    
    if not sender_id or not receiver_id:
        return jsonify({'error': '請提供 sender_id 和 receiver_id'}), 400
    
    if sender_id == receiver_id:
        return jsonify({'error': '不能加自己為好友'}), 400
    
    try:
        sender_obj_id = ObjectId(sender_id)
        receiver_obj_id = ObjectId(receiver_id)
    except:
        return jsonify({'error': '無效的使用者 ID'}), 400
    
    sender = users_collection.find_one({'_id': sender_obj_id})
    receiver = users_collection.find_one({'_id': receiver_obj_id})
    
    if not sender or not receiver:
        return jsonify({'error': '使用者不存在'}), 404
    
    # 檢查是否已經是好友
    if receiver_obj_id in sender.get('friends', []):
        return jsonify({'error': '你們已經是好友了'}), 400
    
    # 檢查是否已發送邀請
    receiver_pending = receiver.get('pendingRequests', [])
    for req in receiver_pending:
        if req.get('fromUserId') == sender_obj_id:
            return jsonify({'error': '已經發送過邀請，請等待對方回應'}), 400
    
    # 建立邀請物件
    request_obj = {
        'fromUserId': sender_obj_id,
        'timestamp': datetime.utcnow()
    }
    
    # 更新接收者的 pendingRequests
    users_collection.update_one(
        {'_id': receiver_obj_id},
        {'$push': {'pendingRequests': request_obj}}
    )
    
    # 更新發送者的 sentRequests
    users_collection.update_one(
        {'_id': sender_obj_id},
        {'$push': {'sentRequests': receiver_obj_id}}
    )
    
    return jsonify({
        'message': '好友邀請已發送',
        'sender': sender['username'],
        'receiver': receiver['username']
    }), 201

# 4. 查看收到的好友邀請
@app.route('/friend-requests/received/<user_id>', methods=['GET'])
def get_received_requests(user_id):
    try:
        user_obj_id = ObjectId(user_id)
    except:
        return jsonify({'error': '無效的使用者 ID'}), 400
    
    user = users_collection.find_one({'_id': user_obj_id})
    if not user:
        return jsonify({'error': '使用者不存在'}), 404
    
    pending_requests = user.get('pendingRequests', [])
    
    # 取得發送者的詳細資訊
    requests_with_info = []
    for req in pending_requests:
        sender_id = req.get('fromUserId')
        sender = users_collection.find_one({'_id': sender_id})
        if sender:
            requests_with_info.append({
                'sender_id': str(sender_id),
                'sender_username': sender.get('username'),
                'sender_email': sender.get('email'),
                'timestamp': req.get('timestamp').isoformat() if req.get('timestamp') else None
            })
    
    return jsonify({
        'received_requests': requests_with_info,
        'count': len(requests_with_info)
    }), 200

# 5. 查看發送的好友邀請
@app.route('/friend-requests/sent/<user_id>', methods=['GET'])
def get_sent_requests(user_id):
    try:
        user_obj_id = ObjectId(user_id)
    except:
        return jsonify({'error': '無效的使用者 ID'}), 400
    
    user = users_collection.find_one({'_id': user_obj_id})
    if not user:
        return jsonify({'error': '使用者不存在'}), 404
    
    sent_request_ids = user.get('sentRequests', [])
    
    # 取得接收者的詳細資訊
    requests_with_info = []
    for receiver_id in sent_request_ids:
        receiver = users_collection.find_one({'_id': receiver_id})
        if receiver:
            requests_with_info.append({
                'receiver_id': str(receiver_id),
                'receiver_username': receiver.get('username'),
                'receiver_email': receiver.get('email')
            })
    
    return jsonify({
        'sent_requests': requests_with_info,
        'count': len(requests_with_info)
    }), 200

# 6. 接受或拒絕好友邀請
@app.route('/friend-requests/respond', methods=['PUT'])
def respond_to_request():
    data = request.json
    user_id = data.get('user_id')  # 接收邀請的人
    sender_id = data.get('sender_id')  # 發送邀請的人
    action = data.get('action')  # 'accept' 或 'reject'
    
    if not user_id or not sender_id or action not in ['accept', 'reject']:
        return jsonify({'error': '請提供正確的參數'}), 400
    
    try:
        user_obj_id = ObjectId(user_id)
        sender_obj_id = ObjectId(sender_id)
    except:
        return jsonify({'error': '無效的使用者 ID'}), 400
    
    user = users_collection.find_one({'_id': user_obj_id})
    sender = users_collection.find_one({'_id': sender_obj_id})
    
    if not user or not sender:
        return jsonify({'error': '使用者不存在'}), 404
    
    # 從 pendingRequests 中移除這個邀請
    users_collection.update_one(
        {'_id': user_obj_id},
        {'$pull': {'pendingRequests': {'fromUserId': sender_obj_id}}}
    )
    
    # 從發送者的 sentRequests 中移除
    users_collection.update_one(
        {'_id': sender_obj_id},
        {'$pull': {'sentRequests': user_obj_id}}
    )
    
    if action == 'accept':
        # 雙方互相加為好友
        users_collection.update_one(
            {'_id': user_obj_id},
            {'$addToSet': {'friends': sender_obj_id}}
        )
        users_collection.update_one(
            {'_id': sender_obj_id},
            {'$addToSet': {'friends': user_obj_id}}
        )
        
        return jsonify({
            'message': '已接受好友邀請',
            'status': 'accepted'
        }), 200
    else:
        return jsonify({
            'message': '已拒絕好友邀請',
            'status': 'rejected'
        }), 200

# 7. 查看好友列表
@app.route('/friends/<user_id>', methods=['GET'])
def get_friends(user_id):
    try:
        user_obj_id = ObjectId(user_id)
    except:
        return jsonify({'error': '無效的使用者 ID'}), 400
    
    user = users_collection.find_one({'_id': user_obj_id})
    if not user:
        return jsonify({'error': '使用者不存在'}), 404
    
    friend_ids = user.get('friends', [])
    
    friends_info = []
    for friend_id in friend_ids:
        friend = users_collection.find_one({'_id': friend_id})
        if friend:
            friends_info.append({
                'id': str(friend['_id']),
                'username': friend.get('username'),
                'email': friend.get('email')
            })
    
    return jsonify({
        'friends': friends_info,
        'count': len(friends_info)
    }), 200

# 8. 刪除好友
@app.route('/friends/<user_id>/<friend_id>', methods=['DELETE'])
def remove_friend(user_id, friend_id):
    try:
        user_obj_id = ObjectId(user_id)
        friend_obj_id = ObjectId(friend_id)
    except:
        return jsonify({'error': '無效的使用者 ID'}), 400
    
    # 雙方互相從好友列表中移除
    result1 = users_collection.update_one(
        {'_id': user_obj_id},
        {'$pull': {'friends': friend_obj_id}}
    )
    
    result2 = users_collection.update_one(
        {'_id': friend_obj_id},
        {'$pull': {'friends': user_obj_id}}
    )
    
    if result1.modified_count == 0 and result2.modified_count == 0:
        return jsonify({'error': '找不到此好友關係'}), 404
    
    return jsonify({'message': '已刪除好友'}), 200

if __name__ == '__main__':
    print("\n=== 好友系統 API 已啟動（嵌入式版本）===")
    print(f"資料庫: {DB_NAME}")
    print("資料結構: 所有資料存在 users collection 內")
    print("\n可用的 API 端點：")
    print("POST   /users - 建立使用者")
    print("GET    /users - 取得所有使用者")
    print("POST   /friend-requests - 發送好友邀請")
    print("GET    /friend-requests/received/<user_id> - 查看收到的邀請")
    print("GET    /friend-requests/sent/<user_id> - 查看發送的邀請")
    print("PUT    /friend-requests/respond - 回應邀請")
    print("GET    /friends/<user_id> - 查看好友列表")
    print("DELETE /friends/<user_id>/<friend_id> - 刪除好友")
    print("=" * 50)
    app.run(debug=True, port=5000)