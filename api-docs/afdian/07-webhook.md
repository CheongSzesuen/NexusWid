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
- `out_trade_no`: 订单号
- `user_id`: 赞助者 user_id
- `plan_id`: 赞助档位 ID
- `month`: 发电月数
- `total_amount`: 实际金额（元）
- `show_amount`: 显示金额（元）
- `status`: 1=待支付, 2=已支付
- `remark`: 留言
- `product_type`: 0=常规方案订阅, 1=售卖商品
- `sku_detail`: SKU 详情数组

## 响应要求

URL 必须返回 JSON，且 `ec` 字段为 `200`，否则爱发电认为请求失败。

```json
{
    "ec": 200,
    "em": ""
}
```