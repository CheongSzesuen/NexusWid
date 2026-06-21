# 订单相关

## 查询订单

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

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `data.list` | array | 订单列表 |
| `out_trade_no` | string | 订单号 |
| `user_id` | string | 赞助者 user_id |
| `plan_id` | string | 赞助档位 ID |
| `month` | int | 发电月数 |
| `total_amount` | string | 实际金额（单位：元） |
| `show_amount` | string | 显示金额（单位：元） |
| `status` | int | 订单状态（1=待支付，2=已支付） |
| `remark` | string | 留言 |
| `redeem_id` | string | 兑换码 ID |
| `product_type` | int | 产品类型（0=常规方案订阅，1=售卖商品） |
| `discount` | string | 折扣金额（单位：元） |
| `sku_detail` | array | SKU 详情数组（售卖商品时有值） |
| `address_person` | string | 收货人姓名 |
| `address_phone` | string | 收货人电话 |
| `address_address` | string | 收货地址 |

---

## 查询赞助者

**接口地址：** `POST https://ifdian.net/api/open/query-sponsor`

**Content-Type：** `application/x-www-form-urlencoded`

**请求参数：** 同查询订单（user_id, page, sign）

**响应数据结构：** 同查询订单
