# 消息相关

## 未读消息检查

**接口地址：** `GET https://afdian.com/api/my/check`

**功能描述：** 获取用户的未读消息数量，包括评论、点赞和私信。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "unread_message_num": 5,
        "unread_count": {
            "comment": 2,
            "like": 1,
            "message": 2
        }
    }
}
```

**字段说明：**
- `data.unread_message_num`: 未读消息总数
- `data.unread_count.comment`: 未读评论数
- `data.unread_count.like`: 未读点赞数
- `data.unread_count.message`: 未读私信数

---

## 未读消息检查（带参数）

**接口地址：** `GET https://afdian.com/api/my/check`

**功能描述：** 检查未读消息，支持 `local_new_msg_id` 参数用于增量检查。

**请求参数：**
- `local_new_msg_id`: 本地最新消息ID（可选，用于增量检查）

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "has_new_msg": 0,
        "unread_message_num": 227,
        "unread_count": {
            "comment": 1,
            "like": 1,
            "message": 225
        },
        "unread_post_num": 0,
        "notice_bar_key": "",
        "config": {
            "polling_interval": 480
        },
        "ip_info": {
            "ip": "103.151.172.84",
            "country": "中国",
            "province": "香港",
            "city": "",
            "county": "",
            "area": "中国香港 特别行政区",
            "isp": "",
            "is_abroad": 1,
            "is_gui": 0
        },
        "debug": {
            "uid": "7918072ee88711efa93552540025c377",
            "ua": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
        }
    }
}
```

**字段说明：**
- `data.has_new_msg`: 是否有新消息（0=无，1=有）
- `data.unread_message_num`: 未读消息总数
- `data.unread_count.comment`: 未读评论数
- `data.unread_count.like`: 未读点赞数
- `data.unread_count.message`: 未读私信数
- `data.unread_post_num`: 未读帖子数
- `data.config.polling_interval`: 轮询间隔（秒）
- `data.ip_info`: IP地址信息

---

## 对话列表

**接口地址：** `GET https://afdian.com/api/message/dialogs`

**功能描述：** 获取用户的对话列表，支持分页和未读过滤。

**请求参数：**
- `page`: 页码（从1开始）
- `unread`: 是否只获取未读对话（0表示全部）

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "total_count": 50,
        "total_page": 5,
        "has_more": 1,
        "list": [
            {
                "latest_msg_id": 12345,
                "unread_count": 2,
                "total_count": 10,
                "status": 1,
                "user": {
                    "user_id": "user_789",
                    "name": "用户名",
                    "avatar": "https://example.com/avatar.jpg"
                },
                "desc": "对话描述内容",
                "send_time": 1719000000
            }
        ]
    }
}
```

**字段说明：**
- `data.total_count`: 对话总数
- `data.total_page`: 总页数
- `data.has_more`: 是否有更多数据（1表示有，0表示无）
- `data.list`: 对话列表
- `latest_msg_id`: 最新消息ID
- `unread_count`: 未读消息数
- `total_count`: 该对话总消息数
- `status`: 对话状态
- `user.user_id`: 用户ID
- `user.name`: 用户名
- `user.avatar`: 用户头像URL
- `desc`: 对话描述
- `send_time`: 发送时间（Unix时间戳）

---

## 聊天消息内容

**接口地址：** `GET https://afdian.com/api/message/messages`

**功能描述：** 获取指定对话的聊天消息内容，支持分页加载。

**请求参数：**
- `user_id`: 对方用户ID（必填）
- `type`: 加载方向，`"old"` 表示加载更早的消息，`"new"` 表示加载更新的消息
- `message_id`: 消息ID（可选，用于分页定位）

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "has_more": 1,
        "list": [
            {
                "type": "send",
                "message": {
                    "msg_id": 173266857,
                    "id": 1762583807,
                    "sender": "7918072ee88711efa93552540025c377",
                    "r_status": 2,
                    "type": 1,
                    "content": "消息内容",
                    "send_time": 1762583807
                }
            }
        ]
    }
}
```

**消息类型（type）：**
- `1`: 文本消息 - `content` 为字符串
- `2`: 订单消息 - `content` 为订单对象
- `4`: 系统消息 - `content` 为字符串
- `6`: 群组邀请 - `content` 包含 `group` 对象

**订单消息（type=2）content 结构：**
```json
{
    "out_trade_no": "202511081436471024998936507",
    "show_amount": "1.00",
    "total_amount": "0.00",
    "remark": "",
    "plan": {
        "plan_id": "3dac3b82bc4a11f0810452540025c377",
        "name": "FlowChat",
        "price": "3.99"
    }
}
```

**群组邀请（type=6）content 结构：**
```json
{
    "group": {
        "group_id": "3c7ab6e4e16611f0a77d52540025c377",
        "title": "快应用抢先体验",
        "content": "快应用抢先体验通知电圈",
        "member_count": 3,
        "price": "20.00"
    }
}
```

**字段说明：**
- `data.has_more`: 是否有更多数据（1表示有，0表示无）
- `data.list`: 消息列表
- `type`: 消息方向，`"send"` 表示我发送的，`"receive"` 表示收到的
- `message.msg_id`: 消息ID
- `message.id`: 消息唯一标识
- `message.sender`: 发送者用户ID
- `message.r_status`: 读取状态（2表示已读）
- `message.type`: 消息类型
- `message.content`: 消息内容
- `message.send_time`: 发送时间（Unix时间戳）