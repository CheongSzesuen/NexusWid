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

**字段说明：**
- `data.list`: 普通计划列表
- `data.sale_list`: 销售计划列表
- `plan_id`: 计划ID
- `name`: 计划名称
- `status`: 计划状态（1表示启用）
- `price`: 计划价格（字符串，单位：元）
- `total_amount`: 该计划总收入（字符串，单位：元）
- `sponsor_count`: 该计划赞助人数
- `pic`: 计划封面图片URL

---

## 创作者列表

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
- `data.list`: 创作者列表
- `data.has_more`: 是否有更多数据
- `data.exists`: 是否存在结果
- `user.user_id`: 用户ID
- `user.name`: 用户昵称
- `user.avatar`: 头像URL
- `user.url_slug`: 自定义URL后缀
- `user.is_verified`: 是否认证
- `user.creator.doing`: 一句话简介
- `user.creator.detail`: 详细介绍
- `user.creator.monthly_fans`: 月粉丝数（`**`表示隐藏）
- `user.creator.monthly_income`: 月收入（`**`表示隐藏）
- `user.creator.category`: 所属分类

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

**字段说明：**
- `data.list`: 分类列表
- `data.is_hg`: 是否海外版
- `category_id`: 分类ID
- `name`: 分类名称
- `pic`: 分类图标URL
- `rank`: 排序权重

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