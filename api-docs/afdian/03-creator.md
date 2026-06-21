# 创作者相关

## 获取所有计划

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 普通计划列表 |
| `data.sale_list` | array | 销售计划列表 |
| `plan_id` | string | 计划 ID |
| `name` | string | 计划名称 |
| `status` | int | 计划状态（1=启用） |
| `price` | string | 计划价格（单位：元） |
| `total_amount` | string | 该计划总收入（单位：元） |
| `sponsor_count` | int | 该计划赞助人数 |
| `pic` | string | 计划封面图片 URL |

---

## 赞助我的人列表

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
                        "desc": "<p>计划描述 HTML</p>",
                        "price": "4.00",
                        "show_price": "4.00",
                        "pay_month": 1,
                        "product_type": 1,
                        "sku_processed": [
                            {
                                "sku_id": "275ef95a903511f0859b52540025c377",
                                "price": "4.00",
                                "count": 1,
                                "name": "通用",
                                "pic": "https://pic1.afdiancdn.com/..."
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
                    "gender": 0,
                    "avatar": "https://pic1.afdiancdn.com/user/user_upload_osl/5fad4bb5cb3ee8d601fdd1d423047ad6_w132_h132_s3.jpeg",
                    "url_slug": "",
                    "is_verified": 0
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
| `data.total_count` | int | 赞助总人数 |
| `data.total_page` | int | 总页数 |
| `data.list` | array | 赞助记录列表 |
| `sponsor_plans` | array | 该用户购买的计划列表 |
| `sponsor_plans[].plan_id` | string | 计划 ID |
| `sponsor_plans[].name` | string | 计划名称 |
| `sponsor_plans[].pic` | string | 计划封面图片 URL |
| `sponsor_plans[].desc` | string | 计划描述（HTML） |
| `sponsor_plans[].price` | string | 计划价格（单位：元） |
| `sponsor_plans[].show_price` | string | 显示价格（单位：元） |
| `sponsor_plans[].pay_month` | int | 付费月数 |
| `sponsor_plans[].product_type` | int | 产品类型（0=订阅，1=商品） |
| `sponsor_plans[].sku_processed` | array | SKU 列表（售卖商品时有值） |
| `current_plan` | object | 当前生效的计划（结构同 `sponsor_plans` 中的元素） |
| `all_sum_amount` | string | 该用户累计赞助金额（单位：元） |
| `create_time` | int | 首次赞助时间（Unix 时间戳） |
| `last_pay_time` | int | 最近赞助时间（Unix 时间戳） |
| `user.user_id` | string | 赞助者用户 ID |
| `user.name` | string | 赞助者昵称 |
| `user.gender` | int | 赞助者性别（0=未知，1=男，2=女） |
| `user.avatar` | string | 赞助者头像 URL |
| `user.url_slug` | string | 赞助者 URL 后缀 |
| `user.is_verified` | int | 赞助者是否认证 |

---

## 创作者列表

**接口地址：** `GET https://afdian.com/api/creator/list`

**功能描述：** 获取创作者列表，支持分类筛选和排序。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | int | N | 页码（从 1 开始） |
| `type` | string | N | 排序类型（`hot`=热门） |
| `category_id` | string | N | 分类 ID（空表示全部） |
| `q` | string | N | 搜索关键词 |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 创作者列表 |
| `data.has_more` | int | 是否有更多数据（0=否，1=是） |
| `data.exists` | int | 是否存在结果（0=否，1=是） |
| `user.user_id` | string | 用户 ID |
| `user.name` | string | 用户昵称 |
| `user.gender` | int | 性别（0=未知，1=男，2=女） |
| `user.birthday` | string | 生日（格式：YYYYMMDD） |
| `user.avatar` | string | 头像 URL |
| `user.cover` | string | 封面 URL |
| `user.url_slug` | string | 自定义 URL 后缀 |
| `user.is_verified` | int | 是否认证 |
| `user.verified_type` | int | 认证类型 |
| `user.creator.status` | int | 创作者状态（1=正常，2=认证） |
| `user.creator.category_id` | string | 分类 ID |
| `user.creator.type` | int | 创作者类型（1=个人，2=组织） |
| `user.creator.doing` | string | 一句话简介 |
| `user.creator.detail` | string | 详细介绍 |
| `user.creator.monthly_fans` | string | 月粉丝数（`**` 表示隐藏） |
| `user.creator.monthly_income` | string | 月收入（`**` 表示隐藏） |
| `user.creator.category.category_id` | string | 分类 ID |
| `user.creator.category.name` | string | 分类名称 |
| `user.creator.category.pic` | string | 分类图标 URL |

---

## 分类列表

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 分类列表 |
| `data.is_hg` | boolean | 是否海外版 |
| `category_id` | string | 分类 ID |
| `name` | string | 分类名称 |
| `pic` | string | 分类图标 URL |
| `rank` | int | 排序权重 |

**常见分类 ID：**
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
