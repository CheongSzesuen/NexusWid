# 用户相关

## 获取用户资料

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
- `ec`: 状态码，200表示成功
- `data.user.user_id`: 用户 ID
- `data.user.name`: 用户昵称
- `data.user.avatar`: 头像图片完整 URL
- `data.user.cover`: 封面图片 URL
- `data.user.url_slug`: 自定义 URL slug（如 `waijade` 对应 `ifdian.net/@waijade`）
- `data.user.creator.doing`: 一句话简介
- `data.user.creator.detail`: 详细介绍

---

## 用户个人资料

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
- `data.user.user_id`: 用户ID
- `data.user.name`: 用户昵称
- `data.user.gender`: 性别（0=未知，1=男，2=女）
- `data.user.birthday`: 生日
- `data.user.avatar`: 头像URL
- `data.user.cover`: 封面URL
- `data.user.url_slug`: 自定义URL后缀
- `data.user.is_verified`: 是否认证
- `data.user.verified_type`: 认证类型
- `data.user.creator.status`: 创作者状态（1=正常，2=认证）
- `data.user.creator.type`: 创作者类型（1=个人，2=组织）
- `data.user.creator.doing`: 一句话简介
- `data.user.creator.detail`: 详细介绍
- `data.user.creator.monthly_fans`: 月粉丝数
- `data.user.creator.monthly_income`: 月收入

---

## 账户信息

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
- `data.login.email`: 邮箱（脱敏）
- `data.login.phone`: 手机号（脱敏）
- `data.oauth`: 第三方绑定信息
- `data.user_private_id`: 用户私有ID

---

## 我赞助的列表

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
- `data.sponsoring`: 赞助中的创作者列表
- `data.sponsoring_expired`: 已过期的赞助列表
- `user`: 创作者信息
- `sale_list`: 订单列表
- `new_post_count`: 新动态数量
- `latest_post_update_time`: 最新动态时间

---

## 加入的电圈

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
- `data.list`: 电圈列表
- `group_id`: 电圈ID
- `title`: 电圈名称
- `cover`: 封面图片
- `status`: 状态
- `open`: 是否开放
- `type`: 类型（1=免费，2=付费）
- `price`: 价格
- `post_count`: 帖子数
- `member_count`: 成员数
- `new_post_count`: 新帖子数
- `is_owner`: 是否是圈主

---

## 收藏的创作者列表

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
- `data.list`: 收藏列表
- `data.has_more`: 是否有更多数据
- `id`: 收藏记录ID
- `user_id`: 我的用户ID
- `remote_id`: 被收藏的创作者用户ID
- `status`: 收藏状态（1=有效）
- `time`: 收藏时间（Unix时间戳）
- `user`: 创作者用户信息
- `user.name`: 创作者昵称
- `user.avatar`: 创作者头像
- `user.url_slug`: 创作者URL后缀
- `user.creator.doing`: 创作者简介