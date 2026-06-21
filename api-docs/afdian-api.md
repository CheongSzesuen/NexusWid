# 爱发电 API 接口文档

本文档记录了爱发电（afdian.com / ifdian.net）所有可用的 API 接口。

---

## 认证方式

### Cookie 认证（网页端接口）

所有网页端接口通过 Cookie 进行认证，需要在请求头中设置：
```
Cookie: auth_token=<your_token>
```

### API Token 签名认证（开发者接口）

开发者接口需要使用 API Token 生成签名进行认证。

**签名算法：**
```
base_string = 按 key 字母序拼接 "key=value&"
sign = MD5(base_string + token)
```

示例（Go）：
```go
func generateSign(params map[string]string, token string) string {
    keys := make([]string, 0, len(params))
    for k := range params { keys = append(keys, k) }
    sort.Strings(keys)
    var parts []string
    for _, k := range keys {
        parts = append(parts, fmt.Sprintf("%s=%s", k, params[k]))
    }
    base := strings.Join(parts, "&") + token
    return fmt.Sprintf("%x", md5.Sum([]byte(base)))
}
```

---

## 公共请求头

所有请求均包含以下公共头部：
```http
Content-Type: application/json
Cookie: auth_token=<token>  # Cookie认证时
User-Agent: Mozilla/5.0 (Windows) AppleWebKit/537.36 Chrome/122.0.0.0
referer: https://afdian.com/
```

---

## 1. OAuth2 授权

### 1.1 发起授权

**接口地址：** `GET https://ifdian.net/oauth2/authorize`

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `response_type` | string | Y | 固定 `code` |
| `scope` | string | Y | 固定 `basic` |
| `client_id` | string | Y | 申请 OAuth2 时获得的 clientID |
| `redirect_uri` | string | Y | 回调地址 |
| `state` | string | Y | CSRF 防护随机串，回调时原样返回 |

**流程说明：**
1. 用户浏览器访问此 URL → 跳转爱发电授权页面
2. 用户点击"授权" → 爱发电 302 到 `redirect_uri?code=xxx&state=yyy`
3. 服务端用 `code` 换取 `user_id`（见 1.2）

---

### 1.2 换取 user_id

**接口地址：** `POST https://ifdian.net/api/oauth2/access_token`

**Content-Type：** `application/x-www-form-urlencoded`

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `grant_type` | string | Y | 固定 `authorization_code` |
| `client_id` | string | Y | 申请时获得的 clientID |
| `client_secret` | string | Y | 申请时获得的 clientSecret |
| `code` | string | Y | 回调中收到的 authorization code |
| `redirect_uri` | string | Y | 与发起授权时一致 |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "user_id": "7918072ee88711efa93552540025c377",
        "user_private_id": "..."
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200表示成功 |
| `em` | string | 错误信息 |
| `data.user_id` | string | 爱发电用户 ID（公开） |
| `data.user_private_id` | string | 爱发电用户私有 ID |

---

## 2. 用户公开资料

### 2.1 获取用户资料

**接口地址：** `GET https://ifdian.net/api/user/get-profile`

**功能描述：** 通过 `user_id` 获取用户昵称、头像等公开信息。**无需鉴权，公开可调用。**

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user_id` | string | Y | 爱发电用户 ID（32 位 hex） |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "user": {
            "user_id": "7918072ee88711efa93552540025c377",
            "name": "WaiJade",
            "avatar": "https://pic1.afdiancdn.com/user/7918072ee88711efa93552540025c377/avatar/f12a067e9b9593ac7bd1233814c9131e_w600_h600_s51.jpeg",
            "cover": "",
            "url_slug": "waijade",
            "creator": {
                "user_id": "...",
                "category_id": "...",
                "type": 1,
                "doing": "个人简介",
                "detail": "详细介绍"
            }
        }
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200表示成功 |
| `data.user.user_id` | string | 用户 ID |
| `data.user.name` | string | 用户昵称 |
| `data.user.avatar` | string | 头像图片完整 URL |
| `data.user.cover` | string | 封面图片 URL |
| `data.user.url_slug` | string | 自定义 URL slug（如 `waijade` 对应 `ifdian.net/@waijade`） |
| `data.user.creator.doing` | string | 一句话简介 |
| `data.user.creator.detail` | string | 详细介绍 |

---

### 2.2 通过 Slug 获取用户资料

**接口地址：** `GET https://afdian.com/api/user/get-profile-by-slug`

**功能描述：** 通过 URL slug 获取用户公开资料。**无需鉴权，公开可调用。**

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `url_slug` | string | Y | 用户 URL 后缀（如 `zaona`） |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "user": {
            "user_id": "00fb401a588211ebb7db52540025c377",
            "name": "Zaona",
            "avatar": "https://pic1.afdiancdn.com/...",
            "cover": "https://pic1.afdiancdn.com/...",
            "url_slug": "zaona",
            "is_verified": 1,
            "verified_type": 2,
            "creator": {
                "doing": "小米手环/手表 表盘/快应用",
                "detail": "详细介绍...",
                "category": { "name": "数码" }
            },
            "is_sponsoring": 1
        }
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.user.user_id` | string | 用户 ID |
| `data.user.name` | string | 用户昵称 |
| `data.user.avatar` | string | 头像 URL |
| `data.user.cover` | string | 封面 URL |
| `data.user.url_slug` | string | URL 后缀 |
| `data.user.is_verified` | int | 是否认证 |
| `data.user.creator.doing` | string | 一句话简介 |
| `data.user.creator.detail` | string | 详细介绍 |
| `data.user.is_sponsoring` | int | 当前登录用户是否正在赞助该创作者 |

---

## 3. 开发者 API（需 API Token 签名）

开发者可在 [爱发电开发者页面](https://ifdian.net/dashboard/dev) 获取 `user_id` 和 `token`。

### 3.1 查询订单

**接口地址：** `POST https://ifdian.net/api/open/query-order`

**Content-Type：** `application/x-www-form-urlencoded`

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user_id` | string | Y | 开发者 user_id |
| `page` | string | Y | 页码（从 1 开始） |
| `sign` | string | Y | 签名（见签名算法） |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "list": [
            {
                "out_trade_no": "2021062321383710834540140626",
                "user_id": "ad8397fe8374811eaaeee52540025c377",
                "plan_id": "a45353328af911eb973052540025c377",
                "month": 1,
                "total_amount": "5.00",
                "show_amount": "5.00",
                "status": 2,
                "remark": "",
                "redeem_id": "",
                "product_type": 0,
                "discount": "0.00",
                "sku_detail": [],
                "address_person": "",
                "address_phone": "",
                "address_address": ""
            }
        ]
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `out_trade_no` | string | 订单号 |
| `user_id` | string | 赞助者 user_id |
| `plan_id` | string | 赞助档位 ID |
| `month` | int | 发电月数 |
| `total_amount` | string | 实际金额（元） |
| `show_amount` | string | 显示金额（元） |
| `status` | int | 1=待支付, 2=已支付 |
| `remark` | string | 留言 |
| `product_type` | int | 0=常规方案订阅, 1=售卖商品 |
| `sku_detail` | array | SKU 详情数组（售卖商品时有值） |

---

### 3.2 查询赞助者

**接口地址：** `POST https://ifdian.net/api/open/query-sponsor`

**Content-Type：** `application/x-www-form-urlencoded`

**请求参数：** 同查询订单（user_id, page, sign）

**响应数据结构：** 同查询订单

---

## 4. 网页端接口（需 Cookie 认证）

### 4.1 Dashboard API - 收入概览

**接口地址：** `GET https://afdian.com/api/my/dashboard`

**功能描述：** 获取创作者的收入概览数据，包括总收入和本月收入。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "summary": {
            "all_sum_amount": "1234.56",
            "month_amount": "567.89",
            "all_sponsor_count": 42,
            "month_sponsor_count": 8
        }
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.summary.all_sum_amount` | string | 总收入金额（单位：元） |
| `data.summary.month_amount` | string | 本月收入金额（单位：元） |
| `data.summary.all_sponsor_count` | int | 总赞助人数 |
| `data.summary.month_sponsor_count` | int | 本月赞助人数 |

---

### 4.2 Check API - 未读消息检查

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
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.unread_message_num` | int | 未读消息总数 |
| `data.unread_count.comment` | int | 未读评论数 |
| `data.unread_count.like` | int | 未读点赞数 |
| `data.unread_count.message` | int | 未读私信数 |

---

### 4.3 Plans API - 获取所有计划

**接口地址：** `GET https://afdian.com/api/creator/all-plans`

**功能描述：** 获取创作者的所有赞助计划，包括普通计划和销售计划。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "list": [
            {
                "plan_id": "plan_123",
                "name": "基础赞助",
                "status": 1,
                "price": "9.90",
                "total_amount": "990.00",
                "sponsor_count": 100,
                "pic": "https://example.com/image.jpg"
            }
        ],
        "sale_list": [
            {
                "plan_id": "sale_456",
                "name": "限时优惠",
                "status": 1,
                "price": "5.00",
                "total_amount": "500.00",
                "sponsor_count": 100,
                "pic": "https://example.com/sale.jpg"
            }
        ]
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 普通计划列表 |
| `data.sale_list` | array | 销售计划列表 |
| `plan_id` | string | 计划ID |
| `name` | string | 计划名称 |
| `status` | int | 计划状态（1表示启用） |
| `price` | string | 计划价格（单位：元） |
| `total_amount` | string | 该计划总收入（单位：元） |
| `sponsor_count` | int | 该计划赞助人数 |
| `pic` | string | 计划封面图片URL |

---

### 4.4 Stat API - 统计数据

**接口地址：** `GET https://afdian.com/api/my/stat`

**功能描述：** 获取按日统计的收入数据，支持分页。

**请求参数：**
- `page`: 页码（从1开始）
- `type`: 统计类型，固定为 `"day"`

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "list": [
            {
                "date_str": 20260621,
                "uv": 150,
                "paid_order_real_amount": "89.10",
                "paid_order_count": 5,
                "paid_user_count": 3,
                "paid_old_user_count": 1
            }
        ],
        "has_more": 1
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 统计数据列表 |
| `data.has_more` | int | 是否有更多数据（1表示有，0表示无） |
| `date_str` | int | 日期（格式：YYYYMMDD） |
| `uv` | int | 页面访问量 |
| `paid_order_real_amount` | string | 实际支付金额（单位：元） |
| `paid_order_count` | int | 支付订单数 |
| `paid_user_count` | int | 支付用户数 |
| `paid_old_user_count` | int | 老用户支付数 |

---

### 4.5 Dialogs API - 对话列表

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
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.total_count` | int | 对话总数 |
| `data.total_page` | int | 总页数 |
| `data.has_more` | int | 是否有更多数据（1表示有，0表示无） |
| `data.list` | array | 对话列表 |
| `latest_msg_id` | int | 最新消息ID |
| `unread_count` | int | 未读消息数 |
| `total_count` | int | 该对话总消息数 |
| `status` | int | 对话状态 |
| `user.user_id` | string | 用户ID |
| `user.name` | string | 用户名 |
| `user.avatar` | string | 用户头像URL |
| `desc` | string | 对话描述 |
| `send_time` | int | 发送时间（Unix时间戳） |

---

### 4.6 Messages API - 聊天消息内容

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
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.has_more` | int | 是否有更多数据（1表示有，0表示无） |
| `data.list` | array | 消息列表 |
| `type` | string | 消息方向，`"send"` 表示我发送的，`"receive"` 表示收到的 |
| `message.msg_id` | int | 消息ID |
| `message.id` | int | 消息唯一标识 |
| `message.sender` | string | 发送者用户ID |
| `message.r_status` | int | 读取状态（2表示已读） |
| `message.type` | int | 消息类型 |
| `message.content` | string | 消息内容 |
| `message.send_time` | int | 发送时间（Unix时间戳） |

---

## 5. Webhook（被动接收订单通知）

### 5.1 配置

在开发者页面配置 Webhook URL，爱发电会在有新订单时 POST 到此 URL。

### 5.2 请求格式

爱发电 POST 的 JSON 格式：

```json
{
    "ec": 200,
    "em": "ok",
    "data": {
        "type": "order",
        "order": {
            "out_trade_no": "2021062321383710834540140626",
            "user_id": "ad8397fe8374811eaaeee52540025c377",
            "plan_id": "a45353328af911eb973052540025c377",
            "month": 1,
            "total_amount": "5.00",
            "show_amount": "5.00",
            "status": 2,
            "remark": "",
            "redeem_id": "",
            "product_type": 0,
            "discount": "0.00",
            "sku_detail": [],
            "address_person": "",
            "address_phone": "",
            "address_address": ""
        }
    }
}
```

**订单字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `out_trade_no` | string | 订单号 |
| `user_id` | string | 赞助者 user_id |
| `plan_id` | string | 赞助档位 ID |
| `month` | int | 发电月数 |
| `total_amount` | string | 实际金额（元） |
| `show_amount` | string | 显示金额（元） |
| `status` | int | 1=待支付, 2=已支付 |
| `remark` | string | 留言 |
| `product_type` | int | 0=常规方案订阅, 1=售卖商品 |
| `sku_detail` | array | SKU 详情数组 |

### 5.3 响应要求

URL 必须返回 JSON，且 `ec` 字段为 `200`，否则爱发电认为请求失败。

```json
{
    "ec": 200,
    "em": ""
}
```

---

## 6. 前端嵌入

### 6.1 嵌入页面

**嵌入代码：**
```html
<iframe src="https://ifdian.net/leaflet?slug={slug}" width="640" scrolling="no" height="200" frameborder="0"></iframe>
```

其中 `{slug}` 替换为创作者个人主页后缀（如 `waijade` 对应 `ifdian.net/@waijade`）。

**手机端优化代码：**
```html
<iframe id="afdian_leaflet_{slug}" src="https://ifdian.net/leaflet?slug={slug}" width="100%" scrolling="no" height="200" frameborder="0"></iframe>
<script>document.body.clientWidth < 700 ? document.getElementById("afdian_leaflet_{slug}").width = "100%" : document.getElementById("afdian_leaflet_{slug}").width = "640"</script>
```

### 6.2 赞助按钮

```html
<a href="https://ifdian.net/a/{slug}"><img width="200" src="https://pic1.afdiancdn.com/static/img/welcome/button-sponsorme.png" alt=""></a>
```

---

## 7. 订单创建 URL 参数

在下单页面的 URL 中增加参数，实现自动化效果：

| 参数 | 说明 |
|------|------|
| `remark` | 留言 |
| `month` | 选择发电的月数 |
| `custom_order_id` | 自定义订单号 |
| `custom_price` | 自选金额发电的金额数 |

**示例：**
```
https://ifdian.net/order/create?plan_id={plan_id}&product_type=0&month=3&remark=支持爱发电
```

---

## 8. 商品 SKU 数据结构

### 8.1 product_type 说明

| 值 | 说明 |
|----|------|
| `0` | 常规方案订阅 |
| `1` | 售卖商品 |

### 8.2 SKU 详情（sku_detail）

当 `product_type=1` 时，订单会包含 `sku_detail` 数组：

```json
{
    "sku_detail": [
        {
            "sku_id": "982649c0448c11f1990852540025c377",
            "count": 1,
            "name": "一季度（3个月）",
            "album_id": ""
        }
    ]
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `sku_id` | string | SKU ID |
| `count` | int | 购买数量 |
| `name` | string | SKU 名称 |
| `album_id` | string | 专辑 ID（通常为空） |

### 8.3 SKU 列表示例

| sku_id | name | 月数 | 价格 |
|--------|------|------|------|
| `982649c0448c11f1990852540025c377` | 一季度 | 3 | 3.00 |
| `982df558448c11f1821552540025c377` | 两季度 | 6 | 5.40 |
| `9835c468448c11f1be9e52540025c377` | 三季度 | 9 | 7.50 |
| `983d0d90448c11f186dd52540025c377` | 一年 | 12 | 8.40 |

---

## 9. 网页端扩展接口（需 Cookie 认证）

### 9.1 Check API - 未读消息检查（带参数）

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
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.has_new_msg` | int | 是否有新消息（0=无，1=有） |
| `data.unread_message_num` | int | 未读消息总数 |
| `data.unread_count.comment` | int | 未读评论数 |
| `data.unread_count.like` | int | 未读点赞数 |
| `data.unread_count.message` | int | 未读私信数 |
| `data.unread_post_num` | int | 未读帖子数 |
| `data.config.polling_interval` | int | 轮询间隔（秒） |
| `data.ip_info` | object | IP地址信息 |

---

### 9.2 Profile API - 用户个人资料

**接口地址：** `GET https://afdian.com/api/my/profile`

**功能描述：** 获取当前登录用户的详细个人资料。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "user": {
            "user_id": "7918072ee88711efa93552540025c377",
            "status": 1,
            "name": "WaiJade",
            "gender": 1,
            "birthday": "20090129",
            "avatar": "https://pic1.afdiancdn.com/user/7918072ee88711efa93552540025c377/avatar/f12a067e9b9593ac7bd1233814c9131e_w600_h600_s51.jpeg",
            "cover": "",
            "url_slug": "waijade",
            "is_verified": 1,
            "verified_type": 2,
            "is_not_rec": 0,
            "show_sponsoring": 1,
            "default_page": 1,
            "show_badge": 1,
            "is_reject": 0,
            "is_block": 0,
            "has_mark": 0,
            "creator": {
                "user_id": "7918072ee88711efa93552540025c377",
                "status": 2,
                "category_id": "f62d3e58c39211e88abd52540025c377",
                "type": 1,
                "doing": "小米VelaOS快应用",
                "detail": "详细介绍",
                "pic": "",
                "create_time": 1752474877,
                "custom_plan": 1,
                "custom_plan_desc": "随缘打赏",
                "is_tic": 0,
                "show_album": 1,
                "show_shop": 1,
                "homepage_product_count": -1,
                "can_copy_text": 1,
                "can_copy_pic": 1,
                "watermark": 2,
                "monthly_fans": 54,
                "monthly_income": "232.00",
                "privacy_public_income": 0,
                "privacy_public_sponsor": 0,
                "discount_option": 0
            }
        },
        "ip_info": { ... }
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.user.user_id` | string | 用户ID |
| `data.user.name` | string | 用户昵称 |
| `data.user.gender` | int | 性别（0=未知，1=男，2=女） |
| `data.user.birthday` | string | 生日 |
| `data.user.avatar` | string | 头像URL |
| `data.user.cover` | string | 封面URL |
| `data.user.url_slug` | string | 自定义URL后缀 |
| `data.user.is_verified` | int | 是否认证 |
| `data.user.verified_type` | int | 认证类型 |
| `data.user.creator.status` | int | 创作者状态（1=正常，2=认证） |
| `data.user.creator.type` | int | 创作者类型（1=个人，2=组织） |
| `data.user.creator.doing` | string | 一句话简介 |
| `data.user.creator.detail` | string | 详细介绍 |
| `data.user.creator.monthly_fans` | int | 月粉丝数 |
| `data.user.creator.monthly_income` | string | 月收入 |

---

### 9.3 Account API - 账户信息

**接口地址：** `GET https://afdian.com/api/my/account`

**功能描述：** 获取账户登录信息和第三方绑定状态。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "ok",
    "data": {
        "login": {
            "email": "",
            "phone": "197***0117"
        },
        "oauth": {
            "wect": {
                "nickname": "WaiJade ",
                "headimgurl": "https://pic1.afdiancdn.com/user/user_upload_osl/2e53c7cc943e9e03623a528c029da24b_w132_h132_s8.jpeg"
            }
        },
        "user_private_id": "70fcfce6b9647f84f5f91dec42b267c240741afd"
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.login.email` | string | 邮箱（脱敏） |
| `data.login.phone` | string | 手机号（脱敏） |
| `data.oauth` | object | 第三方绑定信息 |
| `data.user_private_id` | string | 用户私有ID |

---

### 9.4 Rec List API - 推荐动态列表

**接口地址：** `GET https://afdian.com/api/post/get-rec-list`

**功能描述：** 获取推荐的动态/帖子列表。

**请求参数：**
- `publish_sn`: 发布序号（用于分页，空表示第一页）
- `type`: 类型，`"old"` 表示加载更早的内容
- `all`: 是否获取全部（1=是）

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "ok",
    "data": {
        "list": [
            {
                "post_id": "76e15c5a6cb411f194ee52540025c377",
                "user_id": "ccf8f720197011f1a83a52540025c377",
                "title": "《开始播放！出租车推理事件簿》近期开发进度",
                "preview_text": "",
                "group_id": "",
                "type": 0,
                "cover": "",
                "content": "动态内容...",
                "pics": ["https://pic1.afdiancdn.com/..."],
                "audio": "",
                "video": "",
                "is_public": 1,
                "min_price": "0.00",
                "plan_ids": [],
                "status": 1,
                "like_count": 29,
                "comment_count": 2,
                "publish_time": 1781965776,
                "publish_sn": 17819657769590,
                "cate": "pic",
                "user": {
                    "user_id": "ccf8f720197011f1a83a52540025c377",
                    "name": "373 STUDIO",
                    "avatar": "https://pic1.afdiancdn.com/...",
                    "url_slug": "373STUDIO"
                }
            }
        ],
        "has_more": 1
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 动态列表 |
| `data.has_more` | int | 是否有更多数据 |
| `post_id` | string | 动态ID |
| `title` | string | 标题 |
| `content` | string | 内容 |
| `pics` | array | 图片列表 |
| `video` | string | 视频URL |
| `is_public` | int | 是否公开 |
| `min_price` | string | 最低价格（付费内容） |
| `like_count` | int | 点赞数 |
| `comment_count` | int | 评论数 |
| `publish_time` | int | 发布时间 |
| `cate` | string | 类型（pic=图片, video=视频, article=文章） |

---

### 9.5 Banners API - 首页轮播图

**接口地址：** `GET https://afdian.com/api/welcome/banners`

**功能描述：** 获取首页轮播图数据。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "list": [
            {
                "image": "https://pic1.afdiancdn.com/static/img/banner/banner-merchservice.png",
                "url": "https://ztitw2o1he.feishu.cn/share/base/form/shrcnHIVUUploJopf5IBmM0s40g",
                "new_tab": 0
            },
            {
                "image": "https://pic1.afdiancdn.com/static/img/banner/banner-afdguide.png",
                "url": "https://ifdian.net/album/248b4bb2c9b111e9a9bb52540025c377/92aa33b68b4a11e8aa6952540025c377",
                "new_tab": 0
            }
        ]
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 轮播图列表 |
| `image` | string | 图片URL |
| `url` | string | 跳转链接 |
| `new_tab` | int | 是否新窗口打开（0=否，1=是） |

---

### 9.6 Sponsoring API - 我赞助的列表

**接口地址：** `GET https://afdian.com/api/my/sponsoring`

**功能描述：** 获取当前用户赞助的创作者列表及订单信息。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "sponsoring": [
            {
                "user": {
                    "user_id": "df00daf0a8e911ed82a95254001e7c00",
                    "name": "JIFENG",
                    "avatar": "https://pic1.afdiancdn.com/...",
                    "url_slug": "jifengvela",
                    "creator": {
                        "doing": "小米VelaOS快应用",
                        "detail": "详细介绍"
                    }
                },
                "sale_list": [
                    {
                        "out_trade_no": "202510191636141015310220246",
                        "plan_id": "d0d7db40604d11f0a09252540025c377",
                        "title": "发电商品 ¥2.00",
                        "month": 1,
                        "total_amount": "2.00",
                        "show_amount": "2.00",
                        "status": 2,
                        "remark": "备注内容",
                        "product_type": 1,
                        "sku_detail": "[{...}]",
                        "create_time": 1760862974,
                        "plan": { ... }
                    }
                ],
                "new_post_count": 0,
                "latest_post_update_time": 1775665561
            }
        ],
        "sponsoring_expired": []
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.sponsoring` | array | 赞助中的创作者列表 |
| `data.sponsoring_expired` | array | 已过期的赞助列表 |
| `user` | object | 创作者信息 |
| `sale_list` | array | 订单列表 |
| `new_post_count` | int | 新动态数量 |
| `latest_post_update_time` | int | 最新动态时间 |

---

### 9.7 Joined Group API - 加入的电圈

**接口地址：** `GET https://afdian.com/api/my/joined-group`

**功能描述：** 获取当前用户加入的电圈列表。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "list": [
            {
                "group_id": "7cd515b4703711ef909952540025c377",
                "user_id": "8b0469de60db11efa76652540025c377",
                "title": "森罗物语",
                "cover": "https://pic1.afdiancdn.com/...",
                "content": "",
                "status": 1,
                "open": 1,
                "type": 1,
                "price": "0.00",
                "post_count": 1,
                "comment_count": 2,
                "member_count": 6620,
                "create_time": 1726056909,
                "update_time": 1766319788,
                "latest_post_time": 1777670846,
                "latest_comment_time": 1777976977,
                "join_group_type": 1,
                "selected_plan": "",
                "new_post_count": 1,
                "is_owner": 0
            }
        ]
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 电圈列表 |
| `group_id` | string | 电圈ID |
| `title` | string | 电圈名称 |
| `cover` | string | 封面图片 |
| `status` | int | 状态 |
| `open` | int | 是否开放 |
| `type` | int | 类型（1=免费，2=付费） |
| `price` | string | 价格 |
| `post_count` | int | 帖子数 |
| `member_count` | int | 成员数 |
| `new_post_count` | int | 新帖子数 |
| `is_owner` | int | 是否是圈主 |

---

### 9.8 Who Sponsored Me API - 赞助我的人列表

**接口地址：** `POST https://afdian.com/api/my/who-sponsored-me`

**功能描述：** 获取赞助当前创作者的用户列表，支持分页。

**请求方式：** POST（body 为空或 `{}`）

**请求参数：** 无（通过 Cookie 鉴权）

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "total_count": 1120,
        "total_page": 112,
        "list": [
            {
                "sponsor_plans": [
                    {
                        "plan_id": "751c4cb48ffa11f08f9f5254001e7c00",
                        "name": "星露谷钓鱼",
                        "pic": "https://pic1.afdiancdn.com/...",
                        "price": "4.00",
                        "show_price": "4.00",
                        "pay_month": 1,
                        "product_type": 1,
                        "sku_processed": [
                            {
                                "sku_id": "275ef95a903511f0859b52540025c377",
                                "price": "4.00",
                                "count": 1,
                                "name": "通用"
                            }
                        ]
                    }
                ],
                "current_plan": { ... },
                "all_sum_amount": "4.00",
                "create_time": 1782036943,
                "last_pay_time": 1782036943,
                "user": {
                    "user_id": "9fa4113c365911f1919252540025c377",
                    "name": "萌厨🍝",
                    "avatar": "https://pic1.afdiancdn.com/...",
                    "url_slug": ""
                }
            }
        ]
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.total_count` | int | 赞助总人数 |
| `data.total_page` | int | 总页数 |
| `data.list` | array | 赞助记录列表 |
| `sponsor_plans` | array | 该用户购买的计划列表 |
| `current_plan` | object | 当前生效的计划 |
| `all_sum_amount` | string | 该用户累计赞助金额（单位：元） |
| `create_time` | int | 首次赞助时间（Unix 时间戳） |
| `last_pay_time` | int | 最近赞助时间（Unix 时间戳） |
| `user` | object | 赞助者用户信息 |

---

## 10. 发现与分类接口

### 10.1 Creator List API - 创作者列表

**接口地址：** `GET https://afdian.com/api/creator/list`

**功能描述：** 获取创作者列表，支持分类筛选和排序。

**请求参数：**
- `page`: 页码（从1开始）
- `type`: 排序类型（`hot`=热门）
- `category_id`: 分类ID（可选，空表示全部）
- `q`: 搜索关键词（可选）

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "rec",
    "data": {
        "list": [
            {
                "user_id": "c0327b101a3111e9a26952540025c377",
                "status": 1,
                "name": "惊面兔",
                "gender": 1,
                "birthday": "19910820",
                "avatar": "https://pic1.afdiancdn.com/...",
                "cover": "https://pic1.afdiancdn.com/...",
                "url_slug": "jingmiantu",
                "is_verified": 1,
                "verified_type": 2,
                "creator": {
                    "user_id": "c0327b101a3111e9a26952540025c377",
                    "status": 2,
                    "category_id": "2195c2acb92a11eaa8be52540025c377",
                    "type": 1,
                    "doing": "画漫画。新作中篇漫画《女友是触手怪》正在更新",
                    "detail": "详细介绍",
                    "monthly_fans": "**",
                    "monthly_income": "**",
                    "category": {
                        "category_id": "2195c2acb92a11eaa8be52540025c377",
                        "name": "动漫",
                        "pic": "https://pic.afdiancdn.com/..."
                    }
                }
            }
        ],
        "has_more": 1,
        "exists": 1
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 创作者列表 |
| `data.has_more` | int | 是否有更多数据 |
| `data.exists` | int | 是否存在结果 |
| `user.user_id` | string | 用户ID |
| `user.name` | string | 用户昵称 |
| `user.avatar` | string | 头像URL |
| `user.url_slug` | string | 自定义URL后缀 |
| `user.is_verified` | int | 是否认证 |
| `user.creator.doing` | string | 一句话简介 |
| `user.creator.detail` | string | 详细介绍 |
| `user.creator.monthly_fans` | string | 月粉丝数（`**`表示隐藏） |
| `user.creator.monthly_income` | string | 月收入（`**`表示隐藏） |
| `user.creator.category` | object | 所属分类 |

---

### 10.2 Category List API - 分类列表

**接口地址：** `GET https://afdian.com/api/category/list`

**功能描述：** 获取所有创作者分类列表。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "list": [
            {
                "category_id": "2195c2acb92a11eaa8be52540025c377",
                "name": "动漫",
                "pic": "https://pic.afdiancdn.com/user/27f7cea2370d11e8ae8852540025c377/common/9a2175c15ce07e1fb22bc7179c93d4fe_w200_h200_s3.jpg",
                "rank": 999
            },
            {
                "category_id": "f89c99b22e6f11e8940c52540025c377",
                "name": "音乐",
                "pic": "https://pic.afdiancdn.com/...",
                "rank": 999
            }
        ],
        "is_hg": false
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 分类列表 |
| `data.is_hg` | boolean | 是否海外版 |
| `category_id` | string | 分类ID |
| `name` | string | 分类名称 |
| `pic` | string | 分类图标URL |
| `rank` | int | 排序权重 |

**常见分类ID：**
| category_id | 名称 |
|-------------|------|
| `2195c2acb92a11eaa8be52540025c377` | 动漫 |
| `f89c99b22e6f11e8940c52540025c377` | 音乐 |
| `1d2c1ac230dd11e88a2052540025c377` | 绘画 |
| `68cf9fc630dd11e8aca852540025c377` | 视频 |
| `9db3776230dd11e89c6c52540025c377` | 写作 |
| `ed45455e30dc11e89fd452540025c377` | 游戏 |
| `5378451a30dd11e8bd4f52540025c377` | 播客 |
| `f62d3e58c39211e88abd52540025c377` | 技术 |
| `e4f952e865cc11e98fb252540025c377` | 虚拟偶像 |
| `67498b10837711e99f0652540025c377` | 动画 |
| `d6163d8c837611e98ac352540025c377` | 数码 |

---

## 11. 赞助账单接口

### 11.1 Sponsored Bill Filter API - 赞助账单筛选

**接口地址：** `GET https://afdian.com/api/my/sponsored-bill-filter`

**功能描述：** 筛选和查询赞助账单记录。

**请求参数：**
- `page`: 页码
- `sort_field`: 排序字段（`update_time`）
- `sort_value`: 排序方向（`desc`）
- `is_redeem`: 是否兑换码订单（0=否）
- `plan_id`: 计划ID筛选
- `sign_status`: 签约状态
- `has_remark`: 是否有备注（0=全部）
- `status`: 订单状态
- `order_id`: 订单号搜索
- `nick_name`: 用户昵称搜索
- `user_id`: 用户ID搜索
- `remark`: 备注搜索
- `order_remark`: 订单备注搜索
- `express_no`: 快递单号搜索
- `last_cart_order_id`: 上次购物车订单ID（分页用）
- `last_order_id`: 上次订单ID（分页用）
- `begin_time`: 开始时间
- `end_time`: 结束时间

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "last_order_id": 66426040,
        "last_cart_order_id": "",
        "has_more": 1,
        "list": [
            {
                "out_trade_no": "202606211815375755559724106",
                "is_upgrade": 0,
                "user_id": "9fa4113c365911f1919252540025c377",
                "plan_id": "751c4cb48ffa11f08f9f5254001e7c00",
                "remote_id": "7918072ee88711efa93552540025c377",
                "title": "发电商品 ¥4.00",
                "month": 1,
                "total_amount": "4.00",
                "show_amount": "4.00",
                "discount": "0.00",
                "status": 2,
                "remark": "",
                "create_time": 1782036937,
                "update_time": 1782036943,
                "pay_success_sn": 17820369433658,
                "redeem_id": "",
                "product_type": 1,
                "sku_detail": "[{...}]",
                "sku_count": 1,
                "sign_status": 0,
                "user": {
                    "user_id": "9fa4113c365911f1919252540025c377",
                    "name": "萌厨🍳",
                    "avatar": "https://pic1.afdiancdn.com/..."
                },
                "plan": {
                    "plan_id": "751c4cb48ffa11f08f9f5254001e7c00",
                    "name": "星露谷钓鱼",
                    "price": "4.00",
                    "product_type": 1
                },
                "time_range": {
                    "begin_time": 1781971200,
                    "end_time": 1784649599
                },
                "sku_processed": [
                    {
                        "sku_id": "275ef95a903511f0859b52540025c377",
                        "price": "4.00",
                        "count": 1,
                        "name": "通用"
                    }
                ],
                "total_amount_g": "4.00",
                "py_type": 2
            }
        ],
        "total_count": 0
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.last_order_id` | int | 最后一个订单ID（用于下一页分页） |
| `data.has_more` | int | 是否有更多数据 |
| `data.list` | array | 订单列表 |
| `out_trade_no` | string | 订单号 |
| `user_id` | string | 赞助者用户ID |
| `plan_id` | string | 计划ID |
| `remote_id` | string | 创作者用户ID |
| `title` | string | 订单标题 |
| `month` | int | 月数 |
| `total_amount` | string | 实际支付金额 |
| `show_amount` | string | 显示金额 |
| `status` | int | 订单状态（1=待支付，2=已支付） |
| `remark` | string | 用户备注 |
| `create_time` | int | 创建时间 |
| `update_time` | int | 更新时间 |
| `product_type` | int | 产品类型（0=订阅，1=商品） |
| `sku_detail` | string | SKU详情JSON字符串 |
| `sign_status` | int | 签约状态 |
| `py_type` | int | 支付方式（1=支付宝，2=微信） |

---

## 12. 收入账单接口

### 12.1 Income API - 月度收入账单

**接口地址：** `GET https://afdian.com/api/my/income`

**功能描述：** 获取创作者的月度收入账单数据，按年月分组。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "monthly_bill": [
            {
                "year": 2026,
                "data": [
                    {
                        "month": 5,
                        "data": {
                            "total_amount": "296.00",
                            "creator_amount": "278.24",
                            "sponsor_count": 73,
                            "create_time": 1780261118
                        }
                    },
                    {
                        "month": 4,
                        "data": {
                            "total_amount": "428.00",
                            "creator_amount": "402.32",
                            "sponsor_count": 104,
                            "create_time": 1777582899
                        }
                    }
                ]
            },
            {
                "year": 2025,
                "data": [
                    {
                        "month": 12,
                        "data": {
                            "total_amount": "284.00",
                            "creator_amount": "266.96",
                            "sponsor_count": 67,
                            "create_time": 1767213446
                        }
                    }
                ]
            }
        ]
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.monthly_bill` | array | 按年分组的月度账单列表 |
| `year` | int | 年份 |
| `data` | array | 该年份的月度数据列表 |
| `month` | int | 月份 |
| `data.total_amount` | string | 总金额（单位：元） |
| `data.creator_amount` | string | 创作者实际收入（扣除平台手续费后） |
| `data.sponsor_count` | int | 赞助人数 |
| `data.create_time` | int | 账单创建时间（Unix时间戳） |

**注意事项：**
- 数据按年份倒序排列，每月一条记录
- `creator_amount` 是扣除平台手续费后的实际收入
- 月收入为0的月份也会返回

---

## 13. 赞助账单筛选接口

### 13.1 Sponsored Bill Filter API - 赞助账单筛选

**接口地址：** `GET https://afdian.com/api/my/sponsored-bill-filter`

**功能描述：** 筛选和查询我赞助他人的账单记录（与 Sponsored Bill Out Filter 类似）。

**请求参数：**
- `page`: 页码
- `sort_field`: 排序字段（`update_time`）
- `sort_value`: 排序方向（`desc`）
- `is_redeem`: 是否兑换码订单（0=否）
- `plan_id`: 计划ID筛选
- `sign_status`: 签约状态
- `has_remark`: 是否有备注（0=全部）
- `status`: 订单状态
- `order_id`: 订单号搜索
- `nick_name`: 用户昵称搜索
- `user_id`: 用户ID搜索
- `remark`: 备注搜索
- `order_remark`: 订单备注搜索
- `express_no`: 快递单号搜索
- `last_cart_order_id`: 上次购物车订单ID（分页用）
- `last_order_id`: 上次订单ID（分页用）
- `begin_time`: 开始时间
- `end_time`: 结束时间

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "sponsored_count": 1,
        "payment_count": "6.99",
        "has_more": 0,
        "list": [
            {
                "out_trade_no": "202606091733169999484942727",
                "user_id": "7918072ee88711efa93552540025c377",
                "plan_id": "32ceb526405911f1a5ea52540025c377",
                "remote_id": "bbae243cd3f711ef8b0d5254001e7c00",
                "title": "发电商品 ¥6.99",
                "month": 1,
                "total_amount": "6.99",
                "show_amount": "6.99",
                "status": 2,
                "remark": "e4b501ad2d9a7e0011a300aa71383842",
                "create_time": 1780997596,
                "update_time": 1780997606,
                "product_type": 1,
                "sku_detail": "[{...}]",
                "user": {
                    "user_id": "bbae243cd3f711ef8b0d5254001e7c00",
                    "name": "OrPudding",
                    "avatar": "https://pic1.afdiancdn.com/..."
                },
                "plan": {
                    "plan_id": "32ceb526405911f1a5ea52540025c377",
                    "name": "NeoMusic捐赠版",
                    "price": "2.99"
                },
                "time_range": {
                    "begin_time": 1780934400,
                    "end_time": 1783612799
                },
                "sku_processed": [
                    {
                        "sku_id": "32d8dc7c405911f1adb352540025c377",
                        "price": "6.99",
                        "count": 1,
                        "name": "Pro"
                    }
                ],
                "total_amount_g": "6.99",
                "py_type": 1
            }
        ],
        "total_count": 25
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.sponsored_count` | int | 赞助数量 |
| `data.payment_count` | string | 支付总额 |
| `data.has_more` | int | 是否有更多数据 |
| `data.list` | array | 订单列表 |
| `data.total_count` | int | 总订单数 |
| `out_trade_no` | string | 订单号 |
| `user_id` | string | 我的用户ID |
| `remote_id` | string | 创作者用户ID |
| `title` | string | 订单标题 |
| `total_amount` | string | 实际支付金额 |
| `show_amount` | string | 显示金额 |
| `status` | int | 订单状态（2=已支付） |
| `remark` | string | 用户备注 |
| `product_type` | int | 产品类型（0=订阅，1=商品） |
| `py_type` | int | 支付方式（1=支付宝，2=微信，3=兑换码，4=VIP） |

---

## 14. 收藏创作者接口

### 14.1 Marked API - 收藏的创作者列表

**接口地址：** `GET https://afdian.com/api/my/marked`

**功能描述：** 获取当前用户收藏的创作者列表。

**请求参数：** 无

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "list": [
            {
                "id": 18492313,
                "user_id": "7918072ee88711efa93552540025c377",
                "remote_id": "0c930fd0962a11f09e6852540025c377",
                "status": 1,
                "time": 1760707006,
                "user": {
                    "user_id": "0c930fd0962a11f09e6852540025c377",
                    "status": 1,
                    "name": "AstralSight Studios",
                    "gender": 0,
                    "avatar": "https://pic1.afdiancdn.com/...",
                    "cover": "https://pic1.afdiancdn.com/...",
                    "url_slug": "astralsight",
                    "is_verified": 1,
                    "verified_type": 2,
                    "creator": {
                        "user_id": "0c930fd0962a11f09e6852540025c377",
                        "status": 2,
                        "category_id": "f62d3e58c39211e88abd52540025c377",
                        "type": 2,
                        "doing": "第三方开源玩环小工具「AstroBox」",
                        "detail": "详细介绍"
                    }
                }
            }
        ],
        "has_more": 0
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 收藏列表 |
| `data.has_more` | int | 是否有更多数据 |
| `id` | int | 收藏记录ID |
| `user_id` | string | 我的用户ID |
| `remote_id` | string | 被收藏的创作者用户ID |
| `status` | int | 收藏状态（1=有效） |
| `time` | int | 收藏时间（Unix时间戳） |
| `user` | object | 创作者用户信息 |
| `user.name` | string | 创作者昵称 |
| `user.avatar` | string | 创作者头像 |
| `user.url_slug` | string | 创作者URL后缀 |
| `user.creator.doing` | string | 创作者简介 |

---

## 15. 默认头像

未设置头像的用户可使用爱发电默认头像：

```
https://pic1.afdiancdn.com/default/avatar/avatar-purple.png?imageView2/1/
https://pic1.afdiancdn.com/default/avatar/avatar-blue.png?imageView2/1/
https://pic1.afdiancdn.com/default/avatar/avatar-orange.png?imageView2/1/
https://pic1.afdiancdn.com/default/avatar/avatar-yellow.png?imageView2/1/
```

### 头像 URL 结构

用户头像 URL 格式：
```
https://pic1.afdiancdn.com/user/{user_id}/avatar/{hash}_w{width}_h{height}_s{quality}.jpeg?imageView2/1/
```

| 部分 | 说明 |
|------|------|
| `pic1.afdiancdn.com` | 爱发电 CDN 域名 |
| `{user_id}` | 32 位 hex 用户 ID |
| `{hash}` | 32 位 hex 文件哈希（不可预测，需通过 API 获取） |
| `w600_h600` | 图片尺寸 |
| `s51` | 图片质量参数 |

**结论：** 头像 URL 的 hash 部分不可推导，必须通过获取用户资料接口获取完整 URL。

---

## 16. 获取相册帖子

**接口地址：** `GET https://afdian.com/api/user/get-album-post`

**功能描述：** 获取指定相册下的帖子列表。**无需鉴权，公开可调用。**

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `album_id` | string | Y | 相册 ID（32 位 hex） |
| `lastRank` | string | N | 上一页最后一条的 rank 值（用于分页） |
| `rankOrder` | string | N | 排序方向，`asc` 或 `desc` |
| `rankField` | string | N | 排序字段，如 `rank` |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "ok",
    "data": {
        "list": [
            {
                "post_id": "944e8304819011f0bbed52540025c377",
                "user_id": "00fb401a588211ebb7db52540025c377",
                "title": "简明天气 - 更好看好用的米环米表天气应用",
                "cover": "https://pic1.afdiancdn.com/...",
                "content": "📖 使用教程：https://www.yuque.com/zaona/weather",
                "pics": ["https://pic1.afdiancdn.com/..."],
                "is_public": 1,
                "min_price": "0.00",
                "like_count": 7,
                "comment_count": 2,
                "publish_time": 1756111841,
                "albums": [
                    {
                        "album_id": "7fe723baafe611f0911d52540025c377",
                        "title": "米环米表快应用",
                        "post_count": 2
                    }
                ],
                "unlock_plan_ids": ["695ac5fc609011f0a7e452540025c377"],
                "user": {
                    "user_id": "00fb401a588211ebb7db52540025c377",
                    "name": "Zaona",
                    "avatar": "https://pic1.afdiancdn.com/...",
                    "url_slug": "zaona"
                },
                "rank": 1
            }
        ],
        "has_more": 0
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `data.list` | array | 帖子列表 |
| `data.has_more` | int | 是否有更多数据 |
| `post_id` | string | 帖子 ID |
| `title` | string | 标题 |
| `cover` | string | 封面图 URL |
| `content` | string | 内容摘要 |
| `pics` | array | 图片列表 |
| `is_public` | int | 是否公开 |
| `min_price` | string | 最低价格（付费内容） |
| `like_count` | int | 点赞数 |
| `comment_count` | int | 评论数 |
| `publish_time` | int | 发布时间（Unix 时间戳） |
| `albums` | array | 所属相册列表 |
| `unlock_plan_ids` | array | 可解锁该帖子的计划 ID 列表 |
| `rank` | int | 在相册中的排序值（用于分页） |

---

## 17. 日志收集

**接口地址：** `POST https://afdian.com/api/log/collect`

**功能描述：** 上报用户访问日志，用于统计分析。

**请求方式：** POST

**请求参数：** 无（通过 Cookie 鉴权）

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "ok",
    "data": {
        "stat_id": "3ca2f1de458b11f1a54852540025c377",
        "user_id": "7918072ee88711efa93552540025c377",
        "creator_id": "00fb401a588211ebb7db52540025c377",
        "uri": "https://ifdian.net/a/zaona?tab=feed",
        "path": "/a/zaona",
        "host": "ifdian.net",
        "platform": "GNU/Linux",
        "browser": "Chrome",
        "browser_v": "149.0.0.0",
        "is_login": 1,
        "is_mobile": 0,
        "ip": "103.151.172.84",
        "ip_area": "中国香港 特别行政区",
        "time": 1782065097,
        "is_creator": 1,
        "is_verified": 1,
        "has_income": 1
    }
}
```

**字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `stat_id` | string | 统计记录 ID |
| `user_id` | string | 当前用户 ID |
| `creator_id` | string | 被访问的创作者 ID |
| `uri` | string | 完整访问 URL |
| `path` | string | 访问路径 |
| `host` | string | 访问域名 |
| `platform` | string | 操作系统 |
| `browser` | string | 浏览器 |
| `is_login` | int | 是否已登录 |
| `is_mobile` | int | 是否移动端 |
| `ip` | string | 用户 IP |
| `ip_area` | string | IP 归属地 |
| `time` | int | 访问时间（Unix 时间戳） |
| `is_creator` | int | 是否为创作者 |
| `is_verified` | int | 是否认证用户 |
| `has_income` | int | 是否有收入 |

---

## 错误处理

所有接口的错误响应格式：
```json
{
    "ec": 400,
    "em": "错误信息",
    "data": null
}
```

常见错误码：
- `ec = 200`: 成功
- `ec = 400`: 请求参数错误
- `ec = 401`: 未授权或 Cookie/Token 无效
- `ec = 403`: 禁止访问
- `ec = 404`: 资源不存在
- `ec = 500`: 服务器内部错误

---

## 注意事项

1. Cookie 认证接口需要有效的 `auth_token`
2. 开发者接口需要使用 API Token 生成签名
3. 金额字段均为字符串类型，需要转换为数值类型使用
4. 统计数据接口支持分页，需要循环获取所有数据
5. 未读消息接口用于检查是否有新的互动
6. 对话列表接口可用于检测投诉等特殊消息
7. 聊天消息接口支持双向分页：`type=old` 加载历史消息，`type=new` 加载新消息
8. 消息内容结构根据 `type` 字段动态变化，解析时需注意类型判断
9. `product_type=1` 时为售卖商品，需要解析 `sku_detail` 获取具体购买内容
10. Webhook 响应必须返回 `{"ec": 200}` 否则会被认为请求失败