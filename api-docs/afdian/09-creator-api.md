# 创作者公开API

## 获取赞助者排行榜

**接口地址：** `GET https://afdian.com/api/creator/get-top-sponsors`

**功能描述：** 获取指定创作者的赞助者排行榜列表。**无需鉴权，公开可调用。**

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user_id` | string | Y | 创作者用户 ID（32 位 hex） |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "has_more": 1,
        "list": [
            {
                "user_id": "6a27a7942ff611f1940e52540025c377",
                "status": 1,
                "name": "爱发电用户_mpf8",
                "gender": 0,
                "birthday": "",
                "avatar": "https://pic1.afdiancdn.com/default/avatar/avatar-orange.png",
                "cover": "",
                "url_slug": "",
                "is_verified": 0,
                "verified_type": 0,
                "is_not_rec": 0,
                "show_sponsoring": 1,
                "default_page": 1,
                "show_badge": 1,
                "has_mark": 0,
                "creator": [],
                "badge": []
            }
        ]
    }
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.has_more` | int | 是否有更多数据（1=有，0=无） |
| `data.list` | array | 赞助者列表 |
| `user_id` | string | 赞助者用户 ID |
| `name` | string | 赞助者昵称 |
| `avatar` | string | 赞助者头像 URL |
| `url_slug` | string | 赞助者 URL 后缀 |
| `is_verified` | int | 是否认证（0=否，1=是） |
| `creator` | array | 如果赞助者也是创作者，包含创作者信息 |

**注意事项：**
- 此接口返回的是赞助者排行榜，按赞助金额排序
- 默认返回前 10 名赞助者
- `has_more` 为 1 表示还有更多赞助者

---

## 获取创作者计划列表（公开）

**接口地址：** `GET https://afdian.com/api/creator/get-plans`

**功能描述：** 获取指定创作者的所有赞助计划。**无需鉴权，公开可调用。**

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user_id` | string | Y | 创作者用户 ID（32 位 hex） |
| `album_id` | string | N | 相册 ID（可选） |
| `unlock_plan_ids` | string | N | 解锁计划 ID（可选） |
| `diy` | string | N | 自定义参数（可选） |
| `affiliate_code` | string | N | 推广码（可选） |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "renew_info": {},
        "list": [
            {
                "plan_id": "dee253f08ffa11f089a652540025c377",
                "rank": 0,
                "user_id": "7918072ee88711efa93552540025c377",
                "status": 1,
                "name": "加入致谢名单",
                "pic": "",
                "desc": "加入网站的致谢名单",
                "price": "10.00",
                "update_time": 1758421707,
                "pay_month": 1,
                "show_price": "10.00",
                "product_type": 0,
                "sponsor_count": "",
                "has_vip": 1
            }
        ],
        "sale_has_more": 0,
        "sale_list": [
            {
                "plan_id": "981d84ca448c11f1a91d52540025c377",
                "rank": 1,
                "user_id": "7918072ee88711efa93552540025c377",
                "status": 1,
                "name": "测试软件，请勿购买",
                "pic": "",
                "desc": "测试软件，请勿购买",
                "price": "1.00",
                "update_time": 1779478348,
                "pay_month": 1,
                "show_price": "1.00",
                "product_type": 1,
                "floor_price": "1.00"
            }
        ],
        "limit_show_product_count": -1,
        "planIdCountMap": {
            "all_sponsor_can_read": "0",
            "981d84ca448c11f1a91d52540025c377": "0"
        }
    }
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 普通赞助计划列表 |
| `data.sale_list` | array | 商品销售计划列表 |
| `data.sale_has_more` | int | 商品列表是否有更多 |
| `plan_id` | string | 计划 ID |
| `name` | string | 计划名称 |
| `price` | string | 计划价格（元） |
| `desc` | string | 计划描述 |
| `pic` | string | 计划封面图 URL |
| `product_type` | int | 产品类型（0=订阅，1=商品） |
| `sponsor_count` | string | 赞助人数 |
| `floor_price` | string | 最低价格 |
| `has_vip` | int | 是否有 VIP 价格 |

---

## 获取创作者帖子列表（公开）

**接口地址：** `GET https://afdian.com/api/post/get-list`

**功能描述：** 获取指定创作者的帖子/动态列表。**无需鉴权，公开可调用。**

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user_id` | string | Y | 创作者用户 ID（32 位 hex） |
| `type` | string | N | 加载类型，`old` 表示加载更早内容 |
| `publish_sn` | string | N | 发布序号（用于分页） |
| `per_page` | int | N | 每页数量（默认 10） |
| `group_id` | string | N | 电圈 ID（可选） |
| `all` | int | N | 是否获取全部（1=是） |
| `is_public` | int | N | 是否只获取公开帖子（1=是） |
| `plan_id` | string | N | 计划 ID 筛选 |
| `title` | string | N | 标题搜索 |
| `name` | string | N | 名称搜索 |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "ok",
    "data": {
        "list": [
            {
                "post_id": "70546a28736b11f0a7a95254001e7c00",
                "user_id": "7918072ee88711efa93552540025c377",
                "title": "正在创作新的快应用游戏...",
                "preview_text": "正在创作新的快应用游戏...",
                "content": "正在创作新的快应用游戏...",
                "pics": [],
                "is_public": 1,
                "min_price": "0.00",
                "like_count": 2,
                "comment_count": 0,
                "publish_time": 1754556573,
                "publish_sn": 17545565731761,
                "cate": "normal",
                "user": {
                    "user_id": "7918072ee88711efa93552540025c377",
                    "name": "WaiJade",
                    "avatar": "https://pic1.afdiancdn.com/...",
                    "url_slug": "waijade"
                }
            }
        ],
        "has_more": 0,
        "bread_tab": [
            {
                "key": "all",
                "value": "1",
                "name": "全部"
            },
            {
                "key": "is_public",
                "value": "1",
                "name": "公开",
                "__count": 1
            }
        ]
    }
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 帖子列表 |
| `data.has_more` | int | 是否有更多数据 |
| `post_id` | string | 帖子 ID |
| `title` | string | 帖子标题 |
| `content` | string | 帖子内容 |
| `pics` | array | 图片列表 |
| `is_public` | int | 是否公开 |
| `min_price` | string | 最低价格（付费内容） |
| `like_count` | int | 点赞数 |
| `comment_count` | int | 评论数 |
| `publish_time` | int | 发布时间（Unix 时间戳） |
| `cate` | string | 类型（normal=普通, pic=图片, video=视频） |

---

## 获取我赞助的创作者列表

**接口地址：** `GET https://afdian.com/api/user/get-sponsoring`

**功能描述：** 获取指定用户正在赞助的创作者列表。**无需鉴权，公开可调用。**

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user_id` | string | Y | 用户 ID（32 位 hex） |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "sponsoring": [
            {
                "user": {
                    "user_id": "bbae243cd3f711ef8b0d5254001e7c00",
                    "name": "OrPudding",
                    "avatar": "https://pic1.afdiancdn.com/...",
                    "url_slug": "orpud",
                    "is_verified": 1,
                    "creator": {
                        "doing": "VelaOS快应用",
                        "category": {
                            "name": "技术"
                        }
                    }
                },
                "sale_list": [...],
                "sponsor_info": {
                    "out_detail": {
                        "all_sum_amount": "14.86",
                        "current_amount": "14.86"
                    }
                }
            }
        ]
    }
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.sponsoring` | array | 赞助中的创作者列表 |
| `user` | object | 创作者用户信息 |
| `user.name` | string | 创作者昵称 |
| `user.avatar` | string | 创作者头像 |
| `user.url_slug` | string | 创作者 URL 后缀 |
| `user.creator.doing` | string | 创作者简介 |
| `sponsor_info.out_detail.all_sum_amount` | string | 累计赞助金额 |
| `sponsor_info.out_detail.current_amount` | string | 当前赞助金额 |
