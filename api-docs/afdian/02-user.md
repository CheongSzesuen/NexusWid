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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.user.user_id` | string | 用户 ID |
| `data.user.name` | string | 用户昵称 |
| `data.user.avatar` | string | 头像图片完整 URL |
| `data.user.cover` | string | 封面图片 URL |
| `data.user.url_slug` | string | 自定义 URL slug（如 `waijade` 对应 `ifdian.net/@waijade`） |
| `data.user.creator.doing` | string | 一句话简介 |
| `data.user.creator.detail` | string | 详细介绍 |

---

## 通过 Slug 获取用户资料

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
            "status": 1,
            "name": "Zaona",
            "gender": 0,
            "avatar": "https://pic1.afdiancdn.com/user/00fb401a588211ebb7db52540025c377/avatar/920952973ef41b9eea82b373278ed5ca_w409_h409_s217.png",
            "cover": "https://pic1.afdiancdn.com/user/00fb401a588211ebb7db52540025c377/common/dd3b03c39b0f8b4a1f462c385363f7cc_w1558_h400_s891.png",
            "url_slug": "zaona",
            "is_verified": 1,
            "verified_type": 2,
            "creator": {
                "user_id": "00fb401a588211ebb7db52540025c377",
                "status": 2,
                "category_id": "d6163d8c837611e98ac352540025c377",
                "type": 1,
                "doing": "小米手环/手表 表盘/快应用",
                "detail": "表盘\n· Ultra\n· 点阵Dot\n· 青丘云镜\n\n快应用\n· 简明天气\n· FlowChat\n· 更好的五子棋",
                "category": {
                    "category_id": "d6163d8c837611e98ac352540025c377",
                    "name": "数码"
                }
            },
            "is_sponsoring": 1
        }
    }
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.user.user_id` | string | 用户 ID |
| `data.user.name` | string | 用户昵称 |
| `data.user.avatar` | string | 头像 URL |
| `data.user.cover` | string | 封面 URL |
| `data.user.url_slug` | string | URL 后缀 |
| `data.user.is_verified` | int | 是否认证（0=否，1=是） |
| `data.user.verified_type` | int | 认证类型 |
| `data.user.creator.doing` | string | 一句话简介 |
| `data.user.creator.detail` | string | 详细介绍 |
| `data.user.creator.category.name` | string | 所属分类名称 |
| `data.user.is_sponsoring` | int | 当前登录用户是否正在赞助该创作者 |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.user.user_id` | string | 用户 ID |
| `data.user.name` | string | 用户昵称 |
| `data.user.gender` | int | 性别（0=未知，1=男，2=女） |
| `data.user.birthday` | string | 生日（格式：YYYYMMDD） |
| `data.user.avatar` | string | 头像 URL |
| `data.user.cover` | string | 封面 URL |
| `data.user.url_slug` | string | 自定义 URL 后缀 |
| `data.user.is_verified` | int | 是否认证 |
| `data.user.verified_type` | int | 认证类型 |
| `data.user.creator.status` | int | 创作者状态（1=正常，2=认证） |
| `data.user.creator.type` | int | 创作者类型（1=个人，2=组织） |
| `data.user.creator.doing` | string | 一句话简介 |
| `data.user.creator.detail` | string | 详细介绍 |
| `data.user.creator.monthly_fans` | int | 月粉丝数 |
| `data.user.creator.monthly_income` | string | 月收入（单位：元） |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.login.email` | string | 邮箱（脱敏） |
| `data.login.phone` | string | 手机号（脱敏） |
| `data.oauth` | object | 第三方绑定信息 |
| `data.user_private_id` | string | 用户私有 ID |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.sponsoring` | array | 赞助中的创作者列表 |
| `data.sponsoring_expired` | array | 已过期的赞助列表 |
| `user.user_id` | string | 创作者用户 ID |
| `user.name` | string | 创作者昵称 |
| `user.avatar` | string | 创作者头像 URL |
| `user.url_slug` | string | 创作者 URL 后缀 |
| `user.creator.doing` | string | 创作者简介 |
| `sale_list` | array | 订单列表 |
| `sale_list[].out_trade_no` | string | 订单号 |
| `sale_list[].plan_id` | string | 计划 ID |
| `sale_list[].title` | string | 订单标题 |
| `sale_list[].month` | int | 月数 |
| `sale_list[].total_amount` | string | 实际支付金额（单位：元） |
| `sale_list[].show_amount` | string | 显示金额（单位：元） |
| `sale_list[].status` | int | 订单状态（2=已支付） |
| `sale_list[].product_type` | int | 产品类型（0=订阅，1=商品） |
| `new_post_count` | int | 新动态数量 |
| `latest_post_update_time` | int | 最新动态时间（Unix 时间戳） |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 电圈列表 |
| `group_id` | string | 电圈 ID |
| `title` | string | 电圈名称 |
| `cover` | string | 封面图片 URL |
| `content` | string | 电圈描述 |
| `status` | int | 状态 |
| `open` | int | 是否开放（0=否，1=是） |
| `type` | int | 类型（1=免费，2=付费） |
| `price` | string | 价格（单位：元） |
| `post_count` | int | 帖子数 |
| `comment_count` | int | 评论数 |
| `member_count` | int | 成员数 |
| `create_time` | int | 创建时间（Unix 时间戳） |
| `update_time` | int | 更新时间（Unix 时间戳） |
| `latest_post_time` | int | 最新帖子时间（Unix 时间戳） |
| `latest_comment_time` | int | 最新评论时间（Unix 时间戳） |
| `join_group_type` | int | 加入类型 |
| `new_post_count` | int | 新帖子数 |
| `is_owner` | int | 是否是圈主（0=否，1=是） |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 收藏列表 |
| `data.has_more` | int | 是否有更多数据（0=否，1=是） |
| `id` | int | 收藏记录 ID |
| `user_id` | string | 我的用户 ID |
| `remote_id` | string | 被收藏的创作者用户 ID |
| `status` | int | 收藏状态（1=有效） |
| `time` | int | 收藏时间（Unix 时间戳） |
| `user.user_id` | string | 创作者用户 ID |
| `user.name` | string | 创作者昵称 |
| `user.avatar` | string | 创作者头像 URL |
| `user.cover` | string | 创作者封面 URL |
| `user.url_slug` | string | 创作者 URL 后缀 |
| `user.is_verified` | int | 是否认证 |
| `user.creator.doing` | string | 创作者简介 |
