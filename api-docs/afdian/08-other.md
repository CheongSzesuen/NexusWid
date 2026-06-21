# 其他

## 前端嵌入

### 嵌入页面

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

### 赞助按钮

```html
<a href="https://ifdian.net/a/{slug}"><img width="200" src="https://pic1.afdiancdn.com/static/img/welcome/button-sponsorme.png" alt=""></a>
```

---

## 订单创建 URL 参数

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

## 商品 SKU 数据结构

### product_type 说明

| 值 | 说明 |
|----|------|
| `0` | 常规方案订阅 |
| `1` | 售卖商品 |

### SKU 详情（sku_detail）

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

**SKU 字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `sku_id` | string | SKU ID |
| `count` | int | 购买数量 |
| `name` | string | SKU 名称 |
| `album_id` | string | 专辑 ID（通常为空） |

### SKU 列表示例

| sku_id | name | 月数 | 价格 |
|--------|------|------|------|
| `982649c0448c11f1990852540025c377` | 一季度 | 3 | 3.00 |
| `982df558448c11f1821552540025c377` | 两季度 | 6 | 5.40 |
| `9835c468448c11f1be9e52540025c377` | 三季度 | 9 | 7.50 |
| `983d0d90448c11f186dd52540025c377` | 一年 | 12 | 8.40 |

---

## 推荐动态列表

**接口地址：** `GET https://afdian.com/api/post/get-rec-list`

**功能描述：** 获取推荐的动态/帖子列表。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `publish_sn` | string | N | 发布序号（用于分页，空表示第一页） |
| `type` | string | N | 类型，`"old"` 表示加载更早的内容 |
| `all` | int | N | 是否获取全部（1=是） |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 动态列表 |
| `data.has_more` | int | 是否有更多数据（0=否，1=是） |
| `post_id` | string | 动态 ID |
| `user_id` | string | 作者用户 ID |
| `title` | string | 标题 |
| `preview_text` | string | 预览文本 |
| `group_id` | string | 电圈 ID |
| `type` | int | 类型 |
| `cover` | string | 封面图片 URL |
| `content` | string | 内容 |
| `pics` | array | 图片列表 |
| `audio` | string | 音频 URL |
| `video` | string | 视频 URL |
| `is_public` | int | 是否公开（0=否，1=是） |
| `min_price` | string | 最低价格（付费内容，单位：元） |
| `plan_ids` | array | 关联的计划 ID 列表 |
| `status` | int | 状态 |
| `like_count` | int | 点赞数 |
| `comment_count` | int | 评论数 |
| `publish_time` | int | 发布时间（Unix 时间戳） |
| `publish_sn` | int | 发布序号 |
| `cate` | string | 类型（`pic`=图片，`video`=视频，`article`=文章） |
| `user.user_id` | string | 作者用户 ID |
| `user.name` | string | 作者昵称 |
| `user.avatar` | string | 作者头像 URL |
| `user.url_slug` | string | 作者 URL 后缀 |

---

## 首页轮播图

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 轮播图列表 |
| `image` | string | 图片 URL |
| `url` | string | 跳转链接 |
| `new_tab` | int | 是否新窗口打开（0=否，1=是） |

---

## 默认头像

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

## 日志收集

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.stat_id` | string | 统计记录 ID |
| `data.user_id` | string | 当前用户 ID |
| `data.creator_id` | string | 被访问的创作者 ID |
| `data.uri` | string | 完整访问 URL |
| `data.path` | string | 访问路径 |
| `data.host` | string | 访问域名 |
| `data.platform` | string | 操作系统 |
| `data.browser` | string | 浏览器 |
| `data.browser_v` | string | 浏览器版本 |
| `data.is_login` | int | 是否已登录（0=否，1=是） |
| `data.is_mobile` | int | 是否移动端（0=否，1=是） |
| `data.ip` | string | 用户 IP |
| `data.ip_area` | string | IP 归属地 |
| `data.time` | int | 访问时间（Unix 时间戳） |
| `data.is_creator` | int | 是否为创作者（0=否，1=是） |
| `data.is_verified` | int | 是否认证用户（0=否，1=是） |
| `data.has_income` | int | 是否有收入（0=否，1=是） |

---

## 获取相册帖子

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
                "content": "📖 使用教程：https://www.yuque.com/zaona/weather\n🐧 QQ交流群：947038648",
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
                        "cover": "https://pic1.afdiancdn.com/...",
                        "post_count": 2
                    }
                ],
                "unlock_plan_ids": [
                    "695ac5fc609011f0a7e452540025c377"
                ],
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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 帖子列表 |
| `data.has_more` | int | 是否有更多数据（0=否，1=是） |
| `post_id` | string | 帖子 ID |
| `user_id` | string | 作者用户 ID |
| `title` | string | 标题 |
| `cover` | string | 封面图 URL |
| `content` | string | 内容摘要 |
| `pics` | array | 图片列表 |
| `is_public` | int | 是否公开（0=否，1=是） |
| `min_price` | string | 最低价格（付费内容，单位：元） |
| `like_count` | int | 点赞数 |
| `comment_count` | int | 评论数 |
| `publish_time` | int | 发布时间（Unix 时间戳） |
| `albums` | array | 所属相册列表 |
| `albums[].album_id` | string | 相册 ID |
| `albums[].title` | string | 相册标题 |
| `albums[].cover` | string | 相册封面 URL |
| `albums[].post_count` | int | 相册内帖子数 |
| `unlock_plan_ids` | array | 可解锁该帖子的计划 ID 列表 |
| `user.user_id` | string | 作者用户 ID |
| `user.name` | string | 作者昵称 |
| `user.avatar` | string | 作者头像 URL |
| `user.url_slug` | string | 作者 URL 后缀 |
| `rank` | int | 在相册中的排序值（用于分页） |

---

## 评论接口

### 获取帖子评论

**接口地址：** `GET https://afdian.com/api/comment/get-list`

**功能描述：** 获取指定帖子的评论列表。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `post_id` | string | Y | 帖子 ID |
| `publish_sn` | string | N | 发布序号（用于分页，空表示第一页） |
| `type` | string | N | 加载方向，`"old"` 表示加载更早的评论 |
| `hot` | int | N | 是否按热门排序（1=是） |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "ok",
    "data": {
        "list": [
            {
                "comment_id": "6d09bae46d6611f1bbe95254001e7c00",
                "user_id": "59bfdd1eebc611f0bfc352540025c377",
                "status": 1,
                "post_id": "76e15c5a6cb411f194ee52540025c377",
                "content": "得多看了一眼爱发电，一看就看到作者发布的了。差点错过好作品😢",
                "like_count": 0,
                "publish_time": 1782042210,
                "publish_sn": 17820422108643,
                "has_like": 0,
                "user": {
                    "user_id": "59bfdd1eebc611f0bfc352540025c377",
                    "name": "FF",
                    "avatar": "https://pic1.afdiancdn.com/user/user_upload_osl/d730e70f67f8683a458248259a76dc64_w132_h132_s5.jpeg"
                },
                "can_edit": 0,
                "block": 0,
                "sub_comment_has_more": 0,
                "sub_comments": []
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
| `data.list` | array | 评论列表 |
| `data.has_more` | int | 是否有更多评论（0=否，1=是） |
| `comment_id` | string | 评论 ID |
| `user_id` | string | 评论者用户 ID |
| `status` | int | 状态 |
| `post_id` | string | 帖子 ID |
| `content` | string | 评论内容 |
| `like_count` | int | 点赞数 |
| `publish_time` | int | 发布时间（Unix 时间戳） |
| `publish_sn` | int | 发布序号 |
| `has_like` | int | 是否已点赞（0=否，1=是） |
| `user.user_id` | string | 评论者用户 ID |
| `user.name` | string | 评论者昵称 |
| `user.avatar` | string | 评论者头像 URL |
| `can_edit` | int | 是否可编辑（0=否，1=是） |
| `block` | int | 是否被屏蔽（0=否，1=是） |
| `sub_comment_has_more` | int | 是否有更多子评论（0=否，1=是） |
| `sub_comments` | array | 子评论列表 |

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
| 错误码 | 说明 |
|--------|------|
| `ec = 200` | 成功 |
| `ec = 400` | 请求参数错误 |
| `ec = 401` | 未授权或 Cookie/Token 无效 |
| `ec = 403` | 禁止访问 |
| `ec = 404` | 资源不存在 |
| `ec = 500` | 服务器内部错误 |

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
