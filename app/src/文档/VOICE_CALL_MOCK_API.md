# 语音能力 Mock 接口文档

## 1. 说明

本文档基于当前项目现状和 `产品需求/语音` 设计稿整理，只定义语音相关的 mock 接口。

约束如下：

- 仅使用 `GET` 和 `POST`
- 优先复用现有 `/api/chat`、`/api/characters` 的接口语义
- 暂不引入 WebSocket，语音通话 mock 先采用 `发起通话 + 状态轮询 + 用户回合提交` 的方式
- 所有时间字段统一使用秒级时间戳
- 时间字段命名只使用拦截器可处理字段：`created_at`、`updated_at`、`last_message_time`、`timestamp`、`expires_at`

---

## 2. 状态定义

语音通话状态 `call_status`：

- `connecting`：发起通话后，进入通话页
- `listening`：等待用户说话
- `thinking`：已收到用户输入，角色正在思考
- `speaking`：角色语音输出中
- `ended`：通话已结束

前端页面状态可按下述映射：

- 聊天页点击语音按钮：`POST /api/chat/voice_call/start`
- 视频通话页默认态：`connecting`
- 说话中：`speaking`
- 聆听中：`listening`
- 思考中：`thinking`
- 键盘输入：仍在当前会话内，通过文本回合接口提交
- 结束通话：`ended`

---

## 3. 数据结构

### 3.1 角色音色配置

```json
{
  "character_id": "68b53a6a42fb612951702f61",
  "voice_type": "male_deep",
  "voice_code": "male_deep_01",
  "voice_name": "低沉男声",
  "preview_audio_url": "https://mock.example.com/voice/male_deep_01.mp3",
  "is_default": true,
  "is_selected": true
}
```

### 3.2 语音通话会话

```json
{
  "session_id": "voice_682c2f0ad1b9d61f0f120001",
  "conversation_id": "6914b6c91e6bfd32d7a549d7",
  "character_id": "68b53a6a42fb612951702f61",
  "character_name": "化世景",
  "branch_id": null,
  "call_status": "listening",
  "mic_muted": false,
  "can_interrupt": false,
  "can_switch_to_text": true,
  "duration_seconds": 12,
  "created_at": 1762213200,
  "updated_at": 1762213212,
  "expires_at": 1762213500
}
```

### 3.3 语音消息

```json
{
  "id": "682c2f0ad1b9d61f0f123456",
  "content": "我在开会，晚点再陪你。",
  "message_type": "voice",
  "sender": "character",
  "timestamp": 1762213212,
  "audio_url": "https://mock.example.com/audio/682c2f0ad1b9d61f0f123456.mp3",
  "audio_duration_ms": 2860
}
```

---

## 4. 获取角色音色配置

- `GET /api/characters/<character_id>/voice-config`

### 返回示例

```json
{
  "code": 200,
  "message": "获取角色音色配置成功",
  "data": {
    "character_id": "68b53a6a42fb612951702f61",
    "character_name": "化世景",
    "current_voice": {
      "voice_type": "male_deep",
      "voice_code": "male_deep_01",
      "voice_name": "低沉男声"
    },
    "voice_options": [
      {
        "voice_type": "male_deep",
        "voice_code": "male_deep_01",
        "voice_name": "低沉男声",
        "preview_audio_url": "https://mock.example.com/voice/male_deep_01.mp3",
        "is_default": true,
        "is_selected": true
      },
      {
        "voice_type": "male_gentle",
        "voice_code": "male_gentle_01",
        "voice_name": "温柔男声",
        "preview_audio_url": "https://mock.example.com/voice/male_gentle_01.mp3",
        "is_default": false,
        "is_selected": false
      }
    ],
    "updated_at": 1762213200
  }
}
```

---

## 5. 保存角色音色配置

- `POST /api/characters/<character_id>/voice-config`

### 请求体

```json
{
  "voice_type": "male_gentle",
  "voice_code": "male_gentle_01"
}
```

### 返回示例

```json
{
  "code": 200,
  "message": "保存角色音色配置成功",
  "data": {
    "character_id": "68b53a6a42fb612951702f61",
    "voice_type": "male_gentle",
    "voice_code": "male_gentle_01",
    "updated_at": 1762213260
  }
}
```

---

## 6. 发起语音通话

- `POST /api/chat/voice_call/start`

### 请求体

```json
{
  "character_id": "68b53a6a42fb612951702f61",
  "branch_id": null
}
```

### 返回示例

```json
{
  "code": 200,
  "message": "发起语音通话成功",
  "data": {
    "session_id": "voice_682c2f0ad1b9d61f0f120001",
    "conversation_id": "6914b6c91e6bfd32d7a549d7",
    "character_id": "68b53a6a42fb612951702f61",
    "character_name": "化世景",
    "branch_id": null,
    "call_status": "connecting",
    "mic_muted": false,
    "can_interrupt": false,
    "can_switch_to_text": true,
    "required_fairy_jade": 20,
    "created_at": 1762213300,
    "updated_at": 1762213300,
    "expires_at": 1762213330
  }
}
```

### 余额不足示例

```json
{
  "code": 15010,
  "message": "余额不足",
  "data": {
    "required_fairy_jade": 20,
    "current_fairy_jade": 12,
    "need_recharge": true
  }
}
```

---

## 7. 获取当前角色的进行中语音通话

- `GET /api/chat/voice_call/active?character_id=68b53a6a42fb612951702f61`

### 返回示例

```json
{
  "code": 200,
  "message": "获取进行中语音通话成功",
  "data": {
    "session_id": "voice_682c2f0ad1b9d61f0f120001",
    "conversation_id": "6914b6c91e6bfd32d7a549d7",
    "character_id": "68b53a6a42fb612951702f61",
    "character_name": "化世景",
    "branch_id": null,
    "call_status": "listening",
    "mic_muted": false,
    "can_interrupt": false,
    "can_switch_to_text": true,
    "duration_seconds": 18,
    "created_at": 1762213300,
    "updated_at": 1762213318,
    "expires_at": 1762213500
  }
}
```

无进行中会话时：

```json
{
  "code": 200,
  "message": "当前无进行中的语音通话",
  "data": null
}
```

---

## 8. 轮询语音通话状态

- `GET /api/chat/voice_call/<session_id>/status`

### 返回示例

```json
{
  "code": 200,
  "message": "获取语音通话状态成功",
  "data": {
    "session_id": "voice_682c2f0ad1b9d61f0f120001",
    "conversation_id": "6914b6c91e6bfd32d7a549d7",
    "character_id": "68b53a6a42fb612951702f61",
    "character_name": "化世景",
    "branch_id": null,
    "call_status": "speaking",
    "mic_muted": false,
    "can_interrupt": true,
    "can_switch_to_text": true,
    "duration_seconds": 26,
    "latest_user_text": "你在做什么",
    "latest_ai_text": "我在想你，刚准备给你打电话。",
    "current_message": {
      "id": "682c2f0ad1b9d61f0f123456",
      "content": "我在想你，刚准备给你打电话。",
      "message_type": "voice",
      "sender": "character",
      "timestamp": 1762213326,
      "audio_url": "https://mock.example.com/audio/682c2f0ad1b9d61f0f123456.mp3",
      "audio_duration_ms": 3010
    },
    "last_message_time": 1762213326,
    "created_at": 1762213300,
    "updated_at": 1762213326,
    "expires_at": 1762213500
  }
}
```

---

## 9. 提交用户一轮输入

- `POST /api/chat/voice_call/<session_id>/user_turn`

支持两种模式：

- `input_mode=voice`：用户说话结束后提交一轮语音
- `input_mode=text`：键盘模式下提交一轮文字

### 语音输入请求体

```json
{
  "input_mode": "voice",
  "content": "你在做什么",
  "audio_url": "https://mock.example.com/upload/user_682c2f0a.wav",
  "audio_duration_ms": 2140
}
```

### 文字输入请求体

```json
{
  "input_mode": "text",
  "content": "你在做什么"
}
```

### 返回示例

```json
{
  "code": 200,
  "message": "用户输入提交成功",
  "data": {
    "session_id": "voice_682c2f0ad1b9d61f0f120001",
    "call_status": "thinking",
    "user_message": {
      "id": "682c2f0ad1b9d61f0f123455",
      "content": "你在做什么",
      "message_type": "voice",
      "sender": "user",
      "timestamp": 1762213322,
      "audio_url": "https://mock.example.com/upload/user_682c2f0a.wav",
      "audio_duration_ms": 2140
    },
    "updated_at": 1762213322
  }
}
```

说明：

- 提交成功后，前端继续轮询 `GET /api/chat/voice_call/<session_id>/status`
- 当状态从 `thinking` 变成 `speaking` 时，开始播放角色语音

---

## 10. 切换麦克风状态

- `POST /api/chat/voice_call/<session_id>/mic`

### 请求体

```json
{
  "mic_muted": true
}
```

### 返回示例

```json
{
  "code": 200,
  "message": "麦克风状态更新成功",
  "data": {
    "session_id": "voice_682c2f0ad1b9d61f0f120001",
    "mic_muted": true,
    "updated_at": 1762213330
  }
}
```

---

## 11. 打断角色说话

- `POST /api/chat/voice_call/<session_id>/interrupt`

### 返回示例

```json
{
  "code": 200,
  "message": "已打断角色发言",
  "data": {
    "session_id": "voice_682c2f0ad1b9d61f0f120001",
    "call_status": "listening",
    "can_interrupt": false,
    "updated_at": 1762213335
  }
}
```

---

## 12. 结束语音通话

- `POST /api/chat/voice_call/<session_id>/end`

### 请求体

```json
{
  "reason": "user_hangup"
}
```

### 返回示例

```json
{
  "code": 200,
  "message": "结束语音通话成功",
  "data": {
    "session_id": "voice_682c2f0ad1b9d61f0f120001",
    "conversation_id": "6914b6c91e6bfd32d7a549d7",
    "character_id": "68b53a6a42fb612951702f61",
    "call_status": "ended",
    "duration_seconds": 80,
    "last_message_time": 1762213380,
    "updated_at": 1762213380
  }
}
```

---

## 13. 聊天记录接口增强

现有接口继续复用：

- `GET /api/chat/messages/<conversation_id>?page=1&limit=20`

当消息为语音消息时，`message_type=voice`，额外返回：

- `audio_url`
- `audio_duration_ms`

### 语音消息返回示例

```json
{
  "id": "682c2f0ad1b9d61f0f123456",
  "content": "我在想你，刚准备给你打电话。",
  "message_type": "voice",
  "sender": "character",
  "timestamp": 1762213326,
  "audio_url": "https://mock.example.com/audio/682c2f0ad1b9d61f0f123456.mp3",
  "audio_duration_ms": 3010
}
```

---

## 14. 建议错误码

- `15001`：未提供有效的认证token
- `15002`：无效的token
- `15003`：用户不存在
- `15005`：角色不存在
- `15010`：余额不足
- `15020`：请求参数错误
- `15022`：角色未加入聊天槽位
- `15130`：语音通话会话不存在
- `15131`：语音通话已结束
- `15099`：服务器内部错误

---

## 15. 前端调用顺序建议

1. 点击聊天页语音入口：`POST /api/chat/voice_call/start`
2. 进入通话页后轮询：`GET /api/chat/voice_call/<session_id>/status`
3. 用户说完一句后提交：`POST /api/chat/voice_call/<session_id>/user_turn`
4. 如果角色正在说话且用户点击打断：`POST /api/chat/voice_call/<session_id>/interrupt`
5. 如果切到键盘输入：仍调用 `POST /api/chat/voice_call/<session_id>/user_turn`，但 `input_mode=text`
6. 点击挂断：`POST /api/chat/voice_call/<session_id>/end`

---

## 16. 备注

- 本文档是 mock 定义，便于前后端先联调页面状态
- 真正实现实时语音时，后续可把 `user_turn` 替换成 RTC 或流式音频上行
- 即使后续改成实时流，`start`、`status`、`end` 这几个接口仍然建议保留
