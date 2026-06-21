# 认证与授权

## 认证方式

### Cookie 认证（网页端接口）

所有网页端接口通过 Cookie 进行认证，需要在请求头中设置：
```
Cookie: auth_token=<your_token>
```

### API Token 签名认证（开发者接口）

开发者接口需要使用 API Token 生成签名进行认证。

**签名算法：**
```
base_string = 按 key 字母序拼接 "key=value&"
sign = MD5(base_string + token)
```

示例（Go）：
```go
func generateSign(params map[string]string, token string) string {
    keys := make([]string, 0, len(params))
    for k := range params { keys = append(keys, k) }
    sort.Strings(keys)
    var parts []string
    for _, k := range keys {
        parts = append(parts, fmt.Sprintf("%s=%s", k, params[k]))
    }
    base := strings.Join(parts, "&") + token
    return fmt.Sprintf("%x", md5.Sum([]byte(base)))
}
```

---

## 公共请求头

所有请求均包含以下公共头部：
```http
Content-Type: application/json
Cookie: auth_token=<token>  # Cookie认证时
User-Agent: Mozilla/5.0 (Windows) AppleWebKit/537.36 Chrome/122.0.0.0
referer: https://afdian.com/
```

---

## OAuth2 授权

### 发起授权

**接口地址：** `GET https://ifdian.net/oauth2/authorize`

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `response_type` | string | Y | 固定 `code` |
| `scope` | string | Y | 固定 `basic` |
| `client_id` | string | Y | 申请 OAuth2 时获得的 clientID |
| `redirect_uri` | string | Y | 回调地址 |
| `state` | string | Y | CSRF 防护随机串，回调时原样返回 |

**流程说明：**
1. 用户浏览器访问此 URL → 跳转爱发电授权页面
2. 用户点击"授权" → 爱发电 302 到 `redirect_uri?code=xxx&state=yyy`
3. 服务端用 `code` 换取 `user_id`（见下文）

---

### 换取 user_id

**接口地址：** `POST https://ifdian.net/api/oauth2/access_token`

**Content-Type：** `application/x-www-form-urlencoded`

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `grant_type` | string | Y | 固定 `authorization_code` |
| `client_id` | string | Y | 申请时获得的 clientID |
| `client_secret` | string | Y | 申请时获得的 clientSecret |
| `code` | string | Y | 回调中收到的 authorization code |
| `redirect_uri` | string | Y | 与发起授权时一致 |

**响应数据结构：**
```json
{
    "ec": 200,
    "em": "",
    "data": {
        "user_id": "7918072ee88711efa93552540025c377",
        "user_private_id": "..."
    }
}
```

**响应字段说明：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `ec` | int | 状态码，200 表示成功 |
| `em` | string | 错误信息 |
| `data.user_id` | string | 爱发电用户 ID（公开） |
| `data.user_private_id` | string | 爱发电用户私有 ID |
