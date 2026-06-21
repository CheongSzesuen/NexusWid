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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.unread_message_num` | int | 未读消息总数 |
| `data.unread_count.comment` | int | 未读评论数 |
| `data.unread_count.like` | int | 未读点赞数 |
| `data.unread_count.message` | int | 未读私信数 |

---

## 未读消息检查（带参数）

**接口地址：** `GET https://afdian.com/api/my/check`

**功能描述：** 检查未读消息，支持 `local_new_msg_id` 参数用于增量检查。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `local_new_msg_id` | string | N | 本地最新消息 ID（用于增量检查） |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.has_new_msg` | int | 是否有新消息（0=无，1=有） |
| `data.unread_message_num` | int | 未读消息总数 |
| `data.unread_count.comment` | int | 未读评论数 |
| `data.unread_count.like` | int | 未读点赞数 |
| `data.unread_count.message` | int | 未读私信数 |
| `data.unread_post_num` | int | 未读帖子数 |
| `data.config.polling_interval` | int | 轮询间隔（秒） |
| `data.ip_info.ip` | string | 用户 IP 地址 |
| `data.ip_info.country` | string | 国家 |
| `data.ip_info.province` | string | 省份 |
| `data.ip_info.area` | string | IP 归属地 |
| `data.ip_info.is_abroad` | int | 是否海外（0=否，1=是） |
| `data.debug.uid` | string | 用户 ID |
| `data.debug.ua` | string | User-Agent |

---

## 对话列表

**接口地址：** `GET https://afdian.com/api/message/dialogs`

**功能描述：** 获取用户的对话列表，支持分页和未读过滤。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | int | N | 页码（从 1 开始） |
| `unread` | int | N | 是否只获取未读对话（0=全部） |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.total_count` | int | 对话总数 |
| `data.total_page` | int | 总页数 |
| `data.has_more` | int | 是否有更多数据（1=有，0=无） |
| `data.list` | array | 对话列表 |
| `latest_msg_id` | int | 最新消息 ID |
| `unread_count` | int | 未读消息数 |
| `total_count` | int | 该对话总消息数 |
| `status` | int | 对话状态 |
| `user.user_id` | string | 用户 ID |
| `user.name` | string | 用户名 |
| `user.avatar` | string | 用户头像 URL |
| `desc` | string | 对话描述 |
| `send_time` | int | 发送时间（Unix 时间戳） |

---

## 聊天消息内容

**接口地址：** `GET https://afdian.com/api/message/messages`

**功能描述：** 获取指定对话的聊天消息内容，支持分页加载。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user_id` | string | Y | 对方用户 ID |
| `type` | string | N | 加载方向，`"old"` 表示加载更早的消息，`"new"` 表示加载更新的消息 |
| `message_id` | int | N | 消息 ID（用于分页定位） |

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
| type 值 | 说明 | content 类型 |
|---------|------|-------------|
| `1` | 文本消息 | string |
| `2` | 订单消息 | object |
| `4` | 系统消息 | string |
| `6` | 群组邀请 | object |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.has_more` | int | 是否有更多数据（1=有，0=无） |
| `data.list` | array | 消息列表 |
| `type` | string | 消息方向，`"send"` 表示我发送的，`"receive"` 表示收到的 |
| `message.msg_id` | int | 消息 ID |
| `message.id` | int | 消息唯一标识 |
| `message.sender` | string | 发送者用户 ID |
| `message.r_status` | int | 读取状态（2=已读） |
| `message.type` | int | 消息类型（见上表） |
| `message.content` | string/object | 消息内容（根据 type 不同而不同） |
| `message.send_time` | int | 发送时间（Unix 时间戳） |
