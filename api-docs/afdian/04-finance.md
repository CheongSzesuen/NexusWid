# 财务相关

## 收入概览

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.summary.all_sum_amount` | string | 总收入金额（单位：元） |
| `data.summary.month_amount` | string | 本月收入金额（单位：元） |
| `data.summary.all_sponsor_count` | int | 总赞助人数 |
| `data.summary.month_sponsor_count` | int | 本月赞助人数 |

---

## 统计数据

**接口地址：** `GET https://afdian.com/api/my/stat`

**功能描述：** 获取按日统计的收入数据，支持分页。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | int | N | 页码（从 1 开始） |
| `type` | string | N | 统计类型，固定为 `"day"` |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 统计数据列表 |
| `data.has_more` | int | 是否有更多数据（1=有，0=无） |
| `date_str` | int | 日期（格式：YYYYMMDD） |
| `uv` | int | 页面访问量 |
| `paid_order_real_amount` | string | 实际支付金额（单位：元） |
| `paid_order_count` | int | 支付订单数 |
| `paid_user_count` | int | 支付用户数 |
| `paid_old_user_count` | int | 老用户支付数 |

---

## 赞助账单筛选（创作者视角）

**接口地址：** `GET https://afdian.com/api/my/sponsored-bill-filter`

**功能描述：** 筛选和查询赞助账单记录。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | int | N | 页码 |
| `sort_field` | string | N | 排序字段（`update_time`） |
| `sort_value` | string | N | 排序方向（`desc`） |
| `is_redeem` | int | N | 是否兑换码订单（0=否） |
| `plan_id` | string | N | 计划 ID 筛选 |
| `sign_status` | int | N | 签约状态 |
| `has_remark` | int | N | 是否有备注（0=全部） |
| `status` | int | N | 订单状态 |
| `order_id` | string | N | 订单号搜索 |
| `nick_name` | string | N | 用户昵称搜索 |
| `user_id` | string | N | 用户 ID 搜索 |
| `remark` | string | N | 备注搜索 |
| `order_remark` | string | N | 订单备注搜索 |
| `express_no` | string | N | 快递单号搜索 |
| `last_cart_order_id` | string | N | 上次购物车订单 ID（分页用） |
| `last_order_id` | string | N | 上次订单 ID（分页用） |
| `begin_time` | int | N | 开始时间（Unix 时间戳） |
| `end_time` | int | N | 结束时间（Unix 时间戳） |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.last_order_id` | int | 最后一个订单 ID（用于下一页分页） |
| `data.last_cart_order_id` | string | 最后一个购物车订单 ID |
| `data.has_more` | int | 是否有更多数据（0=否，1=是） |
| `data.list` | array | 订单列表 |
| `data.total_count` | int | 总订单数 |
| `out_trade_no` | string | 订单号 |
| `is_upgrade` | int | 是否升级订单 |
| `user_id` | string | 赞助者用户 ID |
| `plan_id` | string | 计划 ID |
| `remote_id` | string | 创作者用户 ID |
| `title` | string | 订单标题 |
| `month` | int | 月数 |
| `total_amount` | string | 实际支付金额（单位：元） |
| `show_amount` | string | 显示金额（单位：元） |
| `discount` | string | 折扣金额（单位：元） |
| `status` | int | 订单状态（1=待支付，2=已支付） |
| `remark` | string | 用户备注 |
| `create_time` | int | 创建时间（Unix 时间戳） |
| `update_time` | int | 更新时间（Unix 时间戳） |
| `pay_success_sn` | int | 支付成功序号 |
| `redeem_id` | string | 兑换码 ID |
| `product_type` | int | 产品类型（0=订阅，1=商品） |
| `sku_detail` | string | SKU 详情（JSON 字符串） |
| `sku_count` | int | SKU 数量 |
| `sign_status` | int | 签约状态 |
| `user.user_id` | string | 赞助者用户 ID |
| `user.name` | string | 赞助者昵称 |
| `user.avatar` | string | 赞助者头像 URL |
| `plan.plan_id` | string | 计划 ID |
| `plan.name` | string | 计划名称 |
| `plan.price` | string | 计划价格（单位：元） |
| `plan.product_type` | int | 产品类型 |
| `time_range.begin_time` | int | 赞助开始时间（Unix 时间戳） |
| `time_range.end_time` | int | 赞助结束时间（Unix 时间戳） |
| `sku_processed` | array | SKU 处理后列表 |
| `sku_processed[].sku_id` | string | SKU ID |
| `sku_processed[].price` | string | SKU 价格（单位：元） |
| `sku_processed[].count` | int | 购买数量 |
| `sku_processed[].name` | string | SKU 名称 |
| `total_amount_g` | string | 总金额（单位：元） |
| `py_type` | int | 支付方式（1=支付宝，2=微信） |

---

## 月度收入账单

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.monthly_bill` | array | 按年分组的月度账单列表 |
| `year` | int | 年份 |
| `data` | array | 该年份的月度数据列表 |
| `month` | int | 月份 |
| `data.total_amount` | string | 总金额（单位：元） |
| `data.creator_amount` | string | 创作者实际收入（扣除平台手续费后） |
| `data.sponsor_count` | int | 赞助人数 |
| `data.create_time` | int | 账单创建时间（Unix 时间戳） |

**注意事项：**
- 数据按年份倒序排列，每月一条记录
- `creator_amount` 是扣除平台手续费后的实际收入
- 月收入为 0 的月份也会返回

---

## 赞助账单筛选（用户视角）

**接口地址：** `GET https://afdian.com/api/my/sponsored-bill-filter`

**功能描述：** 筛选和查询我赞助他人的账单记录（与 Sponsored Bill Out Filter 类似）。

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | int | N | 页码 |
| `sort_field` | string | N | 排序字段（`update_time`） |
| `sort_value` | string | N | 排序方向（`desc`） |
| `is_redeem` | int | N | 是否兑换码订单（0=否） |
| `plan_id` | string | N | 计划 ID 筛选 |
| `sign_status` | int | N | 签约状态 |
| `has_remark` | int | N | 是否有备注（0=全部） |
| `status` | int | N | 订单状态 |
| `order_id` | string | N | 订单号搜索 |
| `nick_name` | string | N | 用户昵称搜索 |
| `user_id` | string | N | 用户 ID 搜索 |
| `remark` | string | N | 备注搜索 |
| `order_remark` | string | N | 订单备注搜索 |
| `express_no` | string | N | 快递单号搜索 |
| `last_cart_order_id` | string | N | 上次购物车订单 ID（分页用） |
| `last_order_id` | string | N | 上次订单 ID（分页用） |
| `begin_time` | int | N | 开始时间（Unix 时间戳） |
| `end_time` | int | N | 结束时间（Unix 时间戳） |

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.sponsored_count` | int | 赞助数量 |
| `data.payment_count` | string | 支付总额（单位：元） |
| `data.has_more` | int | 是否有更多数据（0=否，1=是） |
| `data.list` | array | 订单列表 |
| `data.total_count` | int | 总订单数 |
| `out_trade_no` | string | 订单号 |
| `user_id` | string | 我的用户 ID |
| `plan_id` | string | 计划 ID |
| `remote_id` | string | 创作者用户 ID |
| `title` | string | 订单标题 |
| `month` | int | 月数 |
| `total_amount` | string | 实际支付金额（单位：元） |
| `show_amount` | string | 显示金额（单位：元） |
| `status` | int | 订单状态（2=已支付） |
| `remark` | string | 用户备注 |
| `create_time` | int | 创建时间（Unix 时间戳） |
| `update_time` | int | 更新时间（Unix 时间戳） |
| `product_type` | int | 产品类型（0=订阅，1=商品） |
| `sku_detail` | string | SKU 详情（JSON 字符串） |
| `user.user_id` | string | 创作者用户 ID |
| `user.name` | string | 创作者昵称 |
| `user.avatar` | string | 创作者头像 URL |
| `plan.plan_id` | string | 计划 ID |
| `plan.name` | string | 计划名称 |
| `plan.price` | string | 计划价格（单位：元） |
| `time_range.begin_time` | int | 赞助开始时间（Unix 时间戳） |
| `time_range.end_time` | int | 赞助结束时间（Unix 时间戳） |
| `sku_processed` | array | SKU 处理后列表 |
| `sku_processed[].sku_id` | string | SKU ID |
| `sku_processed[].price` | string | SKU 价格（单位：元） |
| `sku_processed[].count` | int | 购买数量 |
| `sku_processed[].name` | string | SKU 名称 |
| `total_amount_g` | string | 总金额（单位：元） |
| `py_type` | int | 支付方式（1=支付宝，2=微信，3=兑换码，4=VIP） |
