# Webhook

## 配置

在开发者页面配置 Webhook URL，爱发电会在有新订单时 POST 到此 URL。

## 请求格式

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
| `ec` | int | 状态码，200 表示成功 |
| `em` | string | 状态信息 |
| `data.type` | string | 通知类型（`order`=订单） |
| `data.order.out_trade_no` | string | 订单号 |
| `data.order.user_id` | string | 赞助者 user_id |
| `data.order.plan_id` | string | 赞助档位 ID |
| `data.order.month` | int | 发电月数 |
| `data.order.total_amount` | string | 实际金额（单位：元） |
| `data.order.show_amount` | string | 显示金额（单位：元） |
| `data.order.status` | int | 订单状态（1=待支付，2=已支付） |
| `data.order.remark` | string | 留言 |
| `data.order.redeem_id` | string | 兑换码 ID |
| `data.order.product_type` | int | 产品类型（0=常规方案订阅，1=售卖商品） |
| `data.order.discount` | string | 折扣金额（单位：元） |
| `data.order.sku_detail` | array | SKU 详情数组 |
| `data.order.address_person` | string | 收货人姓名 |
| `data.order.address_phone` | string | 收货人电话 |
| `data.order.address_address` | string | 收货地址 |

## 响应要求

URL 必须返回 JSON，且 `ec` 字段为 `200`，否则爱发电认为请求失败。

```json
{
    "ec": 200,
    "em": ""
}
```
