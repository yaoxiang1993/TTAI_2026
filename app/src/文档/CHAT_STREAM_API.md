# 流式聊天接口文档

## 1. 接口说明

**接口地址**：`POST /api/chat/send_message_stream`

**功能说明**：  
与普通聊天接口 `POST /api/chat/send_message` 入参一致，不同点是角色回复会以 `SSE` 流的形式分段返回，适合前端边接收边渲染。

---

## 2. 请求头

```http
Authorization: Bearer <token>
Content-Type: application/json
Accept: text/event-stream
```

---

## 3. 请求参数

请求体与普通聊天接口一致。

### 必填参数

- `character_id`：角色 ID
- `message`：用户发送的消息内容
- `message_type`：消息类型，通常传 `text`

### 可选参数

- `branch_id`：分支 ID，不传时使用当前默认分支
- `vibration_intensity`：震动强度
- `sucking_intensity`：吸吮强度
- `use_ai_intensity`：是否让服务端自动计算返回强度

### 请求示例

```json
{
  "character_id": "68b53a6a42fb612951702f61",
  "message": "你好，很高兴认识你",
  "message_type": "text"
}
```

### 带分支的请求示例

```json
{
  "character_id": "68b53a6a42fb612951702f61",
  "branch_id": "branch_abc123",
  "message": "我们继续刚才的话题",
  "message_type": "text"
}
```

---

## 4. 返回方式

接口返回 `text/event-stream`，前端按事件流逐段读取。

流中会出现以下事件：

- `start`：开始返回，包含本次用户消息信息
- `delta`：角色回复的增量内容
- `finish`：本次回复结束，包含完整结果
- `error`：流式过程中发生错误

---

## 5. 事件格式

### 5.1 `start`

表示服务端已开始处理本次聊天。

```text
event: start
data: {"code":200,"message":"开始流式回复","data":{"branch_id":"branch_abc123","user_message":{"id":"6821f0d7d1b9d61f0f123456","content":"你好，很高兴认识你","message_type":"text","sender":"user","timestamp":1762213200}}}
```

前端通常可直接使用：

- `data.branch_id`
- `data.user_message`

---

### 5.2 `delta`

表示角色回复的增量片段，前端可直接拼接展示。

```text
event: delta
data: {"content":"你好呀，","chunk_index":1}
```

```text
event: delta
data: {"content":"很高兴见到你。","chunk_index":2}
```

前端通常只需要关注：

- `content`：本次新增文本

---

### 5.3 `finish`

表示本次回复结束，包含完整结果，可用于最终落地消息、刷新会话列表等。

```text
event: finish
data: {"code":200,"message":"发送消息成功","data":{"branch_id":"branch_abc123","vibration_intensity":10,"sucking_intensity":10,"user_message":{"id":"6821f0d7d1b9d61f0f123456","content":"你好，很高兴认识你","message_type":"text","sender":"user","timestamp":1762213200},"ai_message":{"id":"6821f0d9d1b9d61f0f123457","content":"你好呀，很高兴见到你。","message_type":"text","sender":"character","timestamp":1762213202},"character_chat_array":[{"message_id":"6821f0d9d1b9d61f0f123457","content":"你好呀，很高兴见到你。","message_type":"text","sender":"character","timestamp":1762213202}],"lucky_reward":{"won_reward":false,"reward_amount":0,"reward_type":null}}}
```

前端通常可直接使用：

- `data.branch_id`
- `data.vibration_intensity`
- `data.sucking_intensity`
- `data.user_message`
- `data.ai_message`
- `data.character_chat_array`
- `data.lucky_reward`

---

### 5.4 `error`

表示流式返回过程中出错。

```text
event: error
data: {"code":15099,"message":"流式聊天生成失败，请稍后重试","data":{}}
```

---

## 6. 普通错误响应

如果请求在进入流式返回前就失败了，例如：

- token 无效
- 参数缺失
- 角色不存在
- 余额不足

这类情况会直接返回普通 JSON，不会进入 `SSE` 流。

示例：

```json
{
  "code": 15002,
  "message": "无效的token",
  "data": null
}
```

---

## 7. 前端对接建议

### 7.1 渲染建议

- 收到 `start` 后，先把用户消息插入本地消息列表
- 每收到一次 `delta`，就把 `content` 追加到当前角色消息
- 收到 `finish` 后，用 `finish.data.ai_message` 作为最终结果落地
- 收到 `error` 后，提示“生成失败，请重试”

---

### 7.2 接收方式

由于该接口是 `POST`，并且需要携带认证头，前端建议使用 `fetch` + `ReadableStream` 方式读取，不建议使用原生 `EventSource`。

### 7.3 JavaScript 示例

```javascript
async function sendStreamMessage(payload, token, onEvent) {
  const response = await fetch('/api/chat/send_message_stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      'Accept': 'text/event-stream'
    },
    body: JSON.stringify(payload)
  });

  const contentType = response.headers.get('content-type') || '';

  if (!contentType.includes('text/event-stream')) {
    const json = await response.json();
    throw new Error(json.message || '请求失败');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let buffer = '';

  while (true) {
    const { value, done } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const blocks = buffer.split('\n\n');
    buffer = blocks.pop() || '';

    for (const block of blocks) {
      const lines = block.split('\n');
      const eventLine = lines.find(line => line.startsWith('event:'));
      const dataLine = lines.find(line => line.startsWith('data:'));
      if (!eventLine || !dataLine) continue;

      const event = eventLine.replace('event:', '').trim();
      const data = JSON.parse(dataLine.replace('data:', '').trim());
      onEvent?.(event, data);
    }
  }
}
```

---

## 8. 对接要点

- 接口入参与普通聊天接口保持一致
- `delta` 事件只负责增量展示
- `finish` 事件才是本次消息的完整最终结果
- 所有时间字段均为秒级时间戳
