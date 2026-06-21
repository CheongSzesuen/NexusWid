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

**字段说明：**
- `out_trade_no`: 订单号
- `user_id`: 赞助者 user_id
- `plan_id`: 赞助档位 ID
- `month`: 发电月数
- `total_amount`: 实际金额（元）
- `show_amount`: 显示金额（元）
- `status`: 1=待支付, 2=已支付
- `remark`: 留言
- `product_type`: 0=常规方案订阅, 1=售卖商品
- `sku_detail`: SKU 详情数组（售卖商品时有值）

---

## 查询赞助者

**接口地址：** `POST https://ifdian.net/api/open/query-sponsor`

**Content-Type：** `application/x-www-form-urlencoded`

**请求参数：** 同查询订单（user_id, page, sign）

**响应数据结构：** 同查询订单