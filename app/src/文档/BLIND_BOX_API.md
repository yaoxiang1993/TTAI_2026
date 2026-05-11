## 盲盒功能 API 文档

本文档只描述前端需要调用的盲盒相关接口。

功能说明：
- 盲盒名称：`仙玉盲盒`
- 售价：`19.9元`
- 当前支持：`支付宝`、`苹果支付`
- 购买后：`直接开启`
- 奖励类型：`仙玉`

---

## 1. 获取盲盒规则、概率和当前中奖情况

**接口地址**

`GET /api/shop/blind-box/info`

**请求头**

可选登录态：

```text
Authorization: Bearer <token>
```

说明：
- 不带 token 也可以调用
- 带 token 时会额外返回当前用户自己的盲盒摘要信息

**调用示例**

```bash
curl -X GET "http://localhost:5000/api/shop/blind-box/info" \
  -H "Authorization: Bearer <token>"
```

**成功响应示例**

```json
{
  "code": 200,
  "message": "获取盲盒信息成功",
  "data": {
    "blind_box": {
      "product_id": "jade_blind_box",
      "name": "仙玉盲盒",
      "price": 19.9,
      "currency": "CNY",
      "payment_methods": ["alipay", "apple_iap"],
      "rule_version": "2026-05-10-v1",
      "open_immediately": true,
      "rules": [
        {
          "range_key": "comfort_reward",
          "range_label": "1000 ~ 1500",
          "min_jade": 1000,
          "max_jade": 1500,
          "probability": 0.15,
          "probability_text": "15%",
          "mean_jade": 1250,
          "description": "保底安慰奖"
        },
        {
          "range_key": "small_loss",
          "range_label": "1500 ~ 2500",
          "min_jade": 1500,
          "max_jade": 2500,
          "probability": 0.25,
          "probability_text": "25%",
          "mean_jade": 2000,
          "description": "小亏"
        },
        {
          "range_key": "near_break_even",
          "range_label": "2500 ~ 3500",
          "min_jade": 2500,
          "max_jade": 3500,
          "probability": 0.35,
          "probability_text": "35%",
          "mean_jade": 3000,
          "description": "接近回本（3300以上约占该区间20%长度）"
        },
        {
          "range_key": "small_profit",
          "range_label": "3500 ~ 5500",
          "min_jade": 3500,
          "max_jade": 5500,
          "probability": 0.15,
          "probability_text": "15%",
          "mean_jade": 4500,
          "description": "小赚"
        },
        {
          "range_key": "lucky_reward",
          "range_label": "5500 ~ 14000",
          "min_jade": 5500,
          "max_jade": 14000,
          "probability": 0.1,
          "probability_text": "10%",
          "mean_jade": 9750,
          "description": "幸运奖（最大值14000）"
        }
      ]
    },
    "current_winning_status": {
      "recent_wins": [
        {
          "username_masked": "用**8",
          "reward_jade": 2958,
          "range_label": "2500 ~ 3500",
          "range_probability_text": "35%",
          "opened_at": 1778400294
        }
      ],
      "overall_stats": {
        "total_count": 1,
        "total_reward_jade": 2958,
        "max_reward_jade": 2958
      },
      "user_summary": {
        "purchase_count": 1,
        "total_reward_jade": 2958,
        "max_reward_jade": 2958,
        "last_reward_jade": 2958,
        "last_opened_at": 1778400294
      }
    }
  }
}
```

**字段说明**

### `blind_box`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| product_id | string | 盲盒商品ID |
| name | string | 盲盒名称 |
| price | number | 售价，单位元 |
| currency | string | 币种，固定 `CNY` |
| payment_methods | array | 当前支持的支付方式 |
| rule_version | string | 规则版本号 |
| open_immediately | bool | 是否支付后直接开启 |
| rules | array | 奖池规则列表 |

### `rules[]`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| range_key | string | 区间唯一标识 |
| range_label | string | 区间展示文案 |
| min_jade | int | 最小仙玉值 |
| max_jade | int | 最大仙玉值 |
| probability | number | 概率，小数形式 |
| probability_text | string | 概率展示文案 |
| mean_jade | int | 区间均值 |
| description | string | 区间说明 |

### `current_winning_status`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| recent_wins | array | 最近中奖播报 |
| overall_stats | object | 全站盲盒统计 |
| user_summary | object/null | 当前用户自己的盲盒摘要，未登录时为 `null` |

---

## 2. 创建支付宝盲盒支付订单

**接口地址**

`POST /api/user/wallet/alipay-blind-box`

**请求头**

```text
Content-Type: application/json
Authorization: Bearer <token>
```

**请求参数**

无请求体参数，传 `{}` 即可。

**调用示例**

```bash
curl -X POST "http://localhost:5000/api/user/wallet/alipay-blind-box" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{}'
```

**成功响应示例**

```json
{
  "code": 200,
  "message": "盲盒支付订单创建成功",
  "data": {
    "order_id": "blindbox_10000_1778400294_c73b454d",
    "pay_url": "https://openapi.alipay.com/gateway.do?...",
    "pay_amount": 19.9,
    "product_id": "jade_blind_box",
    "product_name": "仙玉盲盒",
    "open_immediately": true
  }
}
```

**字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| order_id | string | 盲盒订单号 |
| pay_url | string | 支付宝支付链接 |
| pay_amount | number | 实际支付金额，固定 `19.9` |
| product_id | string | 盲盒商品ID |
| product_name | string | 盲盒商品名称 |
| open_immediately | bool | 支付成功后是否直接开启 |

**错误码**

| 错误码 | 说明 |
| --- | --- |
| 20001 | 缺少授权token |
| 20002 | token无效 |
| 20006 | 用户不存在 |
| 20007 | 创建订单失败 |
| 20008 | 支付订单创建失败 |
| 20099 | 服务器内部错误 |

---

## 3. 使用苹果支付购买盲盒

**接口地址**

`POST /api/apple-iap/blind-box/verify-receipt`

**请求头**

```text
Content-Type: application/json
Authorization: Bearer <token>
```

**请求参数**

| 字段名 | 类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| transactionReceipt | string | 是 | Base64编码的苹果支付收据 |
| transaction_id | string | 否 | 前端交易ID，用于幂等校验 |

**调用示例**

```bash
curl -X POST "http://localhost:5000/api/apple-iap/blind-box/verify-receipt" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "transactionReceipt": "<base64_receipt>",
    "transaction_id": "2000001063117751"
  }'
```

**成功响应示例**

```json
{
  "code": 200,
  "message": "购买成功",
  "data": {
    "order_id": "apple_blindbox_10000_1778400294_c73b454d",
    "transaction_id": "2000001063117751",
    "product_id": "jade_blind_box",
    "product_name": "仙玉盲盒",
    "pay_amount": 19.9,
    "fairy_jade_balance": 2958,
    "opened_at": 1778400294,
    "purchase_time": 1778400294,
    "environment": "Sandbox",
    "blind_box_result": {
      "product_id": "jade_blind_box",
      "product_name": "仙玉盲盒",
      "rule_version": "2026-05-10-v1",
      "reward_jade": 2958,
      "range_key": "near_break_even",
      "range_label": "2500 ~ 3500",
      "range_min": 2500,
      "range_max": 3500,
      "range_probability": 0.35,
      "range_probability_text": "35%",
      "range_mean_jade": 3000,
      "description": "接近回本（3300以上约占该区间20%长度）",
      "opened_at": 1778400294,
      "record_id": "6a003c26f48ab3b51a4009db"
    }
  }
}
```

**字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| order_id | string | 盲盒订单号 |
| transaction_id | string | 苹果交易ID |
| product_id | string | 盲盒商品ID |
| product_name | string | 盲盒商品名称 |
| pay_amount | number | 实际支付金额，固定 `19.9` |
| fairy_jade_balance | int | 当前用户仙玉余额 |
| opened_at | int | 开盒时间，秒级时间戳 |
| purchase_time | int | 购买时间，秒级时间戳 |
| environment | string | 苹果支付环境 |
| blind_box_result | object | 开奖结果 |

**错误码**

| 错误码 | 说明 |
| --- | --- |
| 21001 | 缺少授权token |
| 21002 | token无效 |
| 21003 | 缺少收据数据 |
| 21004 | 用户不存在 |
| 21005 | 该交易已处理过 |
| 21006 | 收据验证失败 |
| 21007 | 商品ID不正确 |
| 21008 | 盲盒购买失败 |
| 21099 | 服务器内部错误 |

---

## 4. 查询盲盒订单状态和开奖结果

盲盒下单后，前端继续复用现有订单状态接口查询支付和开奖结果。

**接口地址**

`GET /api/user/order/status/{order_id}`

**请求头**

```text
Authorization: Bearer <token>
```

**调用示例**

```bash
curl -X GET "http://localhost:5000/api/user/order/status/blindbox_10000_1778400294_c73b454d" \
  -H "Authorization: Bearer <token>"
```

### 3.1 支付未完成示例

```json
{
  "code": 200,
  "message": "获取订单状态成功",
  "data": {
    "_id": "6a003be100c7637572137791",
    "order_id": "blindbox_10000_1778400225_f53a560d",
    "status": "pending",
    "payment_method": "alipay",
    "pay_amount": 19.9,
    "product_type": "blind_box",
    "product_id": "jade_blind_box",
    "product_name": "仙玉盲盒",
    "reward_status": "pending",
    "blind_box_result": null,
    "created_at": 1778400225,
    "updated_at": 1778400225,
    "paid_at": null
  }
}
```

### 3.2 支付成功并已开奖示例

```json
{
  "code": 200,
  "message": "获取订单状态成功",
  "data": {
    "_id": "6a003c26f48ab3b51a4009da",
    "order_id": "blindbox_10000_1778400294_c73b454d",
    "status": "paid",
    "payment_method": "alipay",
    "pay_amount": 19.9,
    "product_type": "blind_box",
    "product_id": "jade_blind_box",
    "product_name": "仙玉盲盒",
    "reward_status": "completed",
    "opened_at": 1778400294,
    "paid_at": 1778400294,
    "created_at": 1778400294,
    "updated_at": 1778400295,
    "blind_box_result": {
      "product_id": "jade_blind_box",
      "product_name": "仙玉盲盒",
      "rule_version": "2026-05-10-v1",
      "reward_jade": 2958,
      "range_key": "near_break_even",
      "range_label": "2500 ~ 3500",
      "range_min": 2500,
      "range_max": 3500,
      "range_probability": 0.35,
      "range_probability_text": "35%",
      "range_mean_jade": 3000,
      "description": "接近回本（3300以上约占该区间20%长度）",
      "opened_at": 1778400294,
      "record_id": "6a003c26f48ab3b51a4009db"
    }
  }
}
```

**字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| status | string | 订单支付状态，`pending` / `paid` / `failed` |
| reward_status | string | 盲盒开奖状态，`pending` / `processing` / `completed` |
| blind_box_result | object/null | 开奖结果；未开奖时为 `null` |
| opened_at | int/null | 开盒时间，秒级时间戳 |

### `blind_box_result`

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| reward_jade | int | 实际获得的仙玉数量 |
| range_key | string | 命中的概率区间标识 |
| range_label | string | 命中的区间展示文案 |
| range_min | int | 区间最小值 |
| range_max | int | 区间最大值 |
| range_probability | number | 命中区间的概率，小数形式 |
| range_probability_text | string | 命中区间的概率展示文案 |
| range_mean_jade | int | 区间均值 |
| description | string | 区间说明 |
| opened_at | int | 开盒时间 |
| record_id | string | 盲盒开奖记录ID |

---

## 5. 账单体现方式

盲盒开奖成功后，会在现有账单接口里增加一条仙玉收入记录。

**接口地址**

`GET /api/bills/list`

**请求头**

```text
Authorization: Bearer <token>
```

**推荐查询方式**

按交易类型筛选：

```bash
curl -X GET "http://localhost:5000/api/bills/list?type=blind_box&page=1&limit=20" \
  -H "Authorization: Bearer <token>"
```

**返回示例**

```json
{
  "code": 200,
  "message": "获取账单列表成功",
  "data": {
    "bills": [
      {
        "_id": "6a003c27f48ab3b51a4009dc",
        "transaction_type": "blind_box",
        "currency_type": "fairy_jade",
        "amount": 2958,
        "description": "仙玉盲盒开奖",
        "related_id": "blindbox_10000_1778400294_c73b454d",
        "status": "success",
        "extra_data": {
          "product_id": "jade_blind_box",
          "product_name": "仙玉盲盒",
          "payment_amount": 19.9,
          "reward_jade": 2958,
          "range_label": "2500 ~ 3500",
          "range_probability": 0.35,
          "rule_version": "2026-05-10-v1"
        },
        "created_at": 1778400295,
        "updated_at": 1778400295
      }
    ]
  }
}
```

说明：
- `transaction_type = blind_box` 表示盲盒开奖记录
- `amount` 为本次盲盒实际获得的仙玉数量，正数
- `extra_data` 里带盲盒奖励详情

---

## 前端推荐调用顺序

1. 进入盲盒页面时调用 `GET /api/shop/blind-box/info`
2. 用户点击支付宝购买时调用 `POST /api/user/wallet/alipay-blind-box`
3. 用户点击苹果支付购买时调用 `POST /api/apple-iap/blind-box/verify-receipt`
4. 支付完成后可查询 `GET /api/user/order/status/{order_id}`
5. 当返回：
   - `status = paid`
   - `reward_status = completed`
   - `blind_box_result != null`
   前端即可展示开奖弹窗
6. 如需展示用户开奖记录，可复用 `GET /api/bills/list?type=blind_box`
