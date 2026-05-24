# IMS 语音通话接口文档

## 1. 文档范围

本文档只描述当前后端已经实现的接口与返回结构，按现有代码事实编写。

当前主链路已经不是旧版纯 mock 会话状态机，而是：

- 后端调用阿里云 IMS 创建/查询/结束 AI 通话实例
- 前端拿到 `data.ims` 中的 RTC 入会参数后，用客户端 SDK 入会
- 实时音频采集、播放、打断、静音等能力，仍应由 AICallKit / ARTC SDK 负责

代码里仍然保留了 `mock-audio` 和 mock 分支，但它们只适合以下场景：

- 角色音色试听
- 占位音频地址
- 本地回退调试

不能再把它理解成正式通话音频主链路。

所有接口都只使用 `GET` / `POST`。文档中所有时间字段示例均为秒级时间戳；当前项目的统一响应时间转换会把 `created_at`、`updated_at`、`last_message_time`、`timestamp`、`expires_at` 等字段转成秒级时间戳。

示例域名统一使用：`https://becomestar.com.cn`

---

## 2. 前端不要误解的点

1. 当前已经是“后端创建 IMS 实例 + 前端用 SDK 入会”。
2. 不是旧版纯 mock 会话状态机。
3. 但也不是“后端全权控制实时音频”。
4. 实时音频能力仍应由 AICallKit / ARTC SDK 负责，后端当前主要负责会话创建、状态查询、业务入参落库和会话结束。

补充说明：

- `POST /api/chat/voice_call/start` 会真实调用 IMS `GenerateAIAgentCall`
- `GET /api/chat/voice_call/active` 与 `GET /api/chat/voice_call/<session_id>/status` 会真实调用 IMS `DescribeAIAgentInstance`
- `POST /api/chat/voice_call/<session_id>/end` 会真实调用 IMS `StopAIAgentInstance`
- `POST /api/chat/voice_call/<session_id>/user_turn` 在真实 IMS 模式下会调用 IMS `SendAIAgentText`

---

## 3. IMS 接入现状

当前服务已接入真实 IMS：

- `GenerateAIAgentCall`
- `DescribeAIAgentInstance`
- `StopAIAgentInstance`
- `SendAIAgentText`

创建 IMS 实例时存在地域回退逻辑，尝试顺序为：

- 配置的 `ALIYUN_IMS_REGION`
- `cn-beijing`
- `cn-hangzhou`
- `cn-shanghai`
- `cn-shenzhen`

当前 agent 实测可用 region 是：`cn-beijing`

---

## 4. 鉴权

以下接口都需要登录态：

- `GET /api/characters/<character_id>/voice-config`
- `POST /api/characters/<character_id>/voice-config`
- `POST /api/chat/voice_call/start`
- `GET /api/chat/voice_call/active`
- `GET /api/chat/voice_call/<session_id>/status`
- `POST /api/chat/voice_call/<session_id>/user_turn`
- `POST /api/chat/voice_call/<session_id>/mic`
- `POST /api/chat/voice_call/<session_id>/interrupt`
- `POST /api/chat/voice_call/<session_id>/end`
- `GET /api/chat/messages/<conversation_id>`

以下接口不需要 token：

- `GET /api/chat/voice_call/mock-audio/<audio_id>.wav`

请求头示例：

```http
Authorization: Bearer <token>
Content-Type: application/json
```

---

## 5. 角色音色配置

### 5.1 `GET /api/characters/<character_id>/voice-config`

作用：

- 获取当前用户对该角色保存的本地音色偏好
- 返回可试听的音色列表

注意：

- 这是“本地角色音色偏好”和“试听配置”
- 不代表已经把真实音色下发到 IMS 通话实例
- `preview_audio_url` 指向的仍然是 `mock-audio` 试听资源

返回示例：

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
        "preview_audio_url": "https://becomestar.com.cn/api/chat/voice_call/mock-audio/preview_male_deep_01.wav",
        "is_default": true,
        "is_selected": true
      },
      {
        "voice_type": "male_gentle",
        "voice_code": "male_gentle_01",
        "voice_name": "温柔男声",
        "preview_audio_url": "https://becomestar.com.cn/api/chat/voice_call/mock-audio/preview_male_gentle_01.wav",
        "is_default": false,
        "is_selected": false
      }
    ],
    "updated_at": 1762213200
  }
}
```

### 5.2 `POST /api/characters/<character_id>/voice-config`

作用：

- 保存当前用户对该角色的本地音色偏好

请求体：

```json
{
  "voice_type": "male_gentle",
  "voice_code": "male_gentle_01"
}
```

说明：

- `voice_type` 和 `voice_code` 至少传一个
- 后端会在该角色可选音色中匹配并保存
- 这里只会保存到本地 `character_voice_preferences`
- 不会把该音色实时下发给 IMS 通话实例

返回示例：

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

## 6. 试听 / 占位音频

### `GET /api/chat/voice_call/mock-audio/<audio_id>.wav`

作用：

- 角色音色试听
- 占位音频资源
- 部分语音消息在客户端未上传真实音频文件时的占位 `audio_url`

限制说明：

- 它不是正式 IMS 通话的音频来源
- 正式通话的实时音频链路由客户端 SDK 负责

查询参数：

- `duration_ms`：可选，范围 `300-8000`

示例：

```text
GET https://becomestar.com.cn/api/chat/voice_call/mock-audio/preview_male_deep_01.wav?duration_ms=1800
```

返回：

- `audio/wav` 二进制流

---

## 7. 语音通话主链路

### 7.1 会话载荷结构

`start` / `active` / `status` / `end` 的 `data` 主体是同一类会话结构：

```json
{
  "session_id": "voice_1234567890abcdef12345678",
  "conversation_id": "6914b6c91e6bfd32d7a549d7",
  "character_id": "68b53a6a42fb612951702f61",
  "character_name": "化世景",
  "branch_id": null,
  "call_status": "connecting",
  "mic_muted": false,
  "can_interrupt": false,
  "can_switch_to_text": true,
  "duration_seconds": 3,
  "latest_user_text": null,
  "latest_ai_text": null,
  "current_message": null,
  "last_message_time": 1762213300,
  "created_at": 1762213300,
  "updated_at": 1762213300,
  "expires_at": 1762215100,
  "provider": "aliyun_ims",
  "ims": {
    "instance_id": "ims-instance-id",
    "region": "cn-beijing",
    "channel_id": "ims-channel-id",
    "rtc_user_id": "rtc-user-id",
    "rtc_token": "rtc-token",
    "aiagent_user_id": "aiagent-user-id",
    "avatar_user_id": "avatar-user-id",
    "artc_app_id": "95c78806-66c5-4094-b072-6af4964c2b6b",
    "ims_status": "Created",
    "call_log_url": null
  }
}
```

说明：

- 当前主链路下，`provider` 应视为 `aliyun_ims`
- `current_message` 主要是旧 mock 语音播放模型会使用，真实 IMS 模式下通常为 `null`
- `ims.call_log_url` 键会保留，但在 `start` 刚返回时通常可能还是 `null`

前端需要重点读取 `data.ims`：

- `instance_id`
- `region`
- `channel_id`
- `rtc_user_id`
- `rtc_token`
- `aiagent_user_id`
- `avatar_user_id`
- `artc_app_id`
- `ims_status`
- `call_log_url`

这些字段用于前端 SDK 入会和后续状态展示，其中 `artc_app_id` 当前真实值为：`95c78806-66c5-4094-b072-6af4964c2b6b`

### 7.2 `POST /api/chat/voice_call/start`

作用：

- 发起语音通话
- 当前真实主链路会调用 IMS `GenerateAIAgentCall`

请求体：

```json
{
  "character_id": "68b53a6a42fb612951702f61",
  "branch_id": null
}
```

说明：

- `character_id` 必填
- `branch_id` 可选
- 后端会校验角色权限
- 后端会自动创建或复用 `conversation`
- 用户同一时间只允许一个进行中的语音会话；如果已有进行中会话，会直接返回已有会话
- 返回里的 `required_fairy_jade` 表示当前发起通话要求的仙玉数，前端以返回值为准

成功返回示例：

```json
{
  "code": 200,
  "message": "发起语音通话成功",
  "data": {
    "session_id": "voice_1234567890abcdef12345678",
    "conversation_id": "6914b6c91e6bfd32d7a549d7",
    "character_id": "68b53a6a42fb612951702f61",
    "character_name": "化世景",
    "branch_id": null,
    "call_status": "connecting",
    "mic_muted": false,
    "can_interrupt": false,
    "can_switch_to_text": true,
    "duration_seconds": 0,
    "latest_user_text": null,
    "latest_ai_text": null,
    "current_message": null,
    "last_message_time": 1762213300,
    "created_at": 1762213300,
    "updated_at": 1762213300,
    "expires_at": 1762215100,
    "provider": "aliyun_ims",
    "ims": {
      "instance_id": "ims-instance-id",
      "region": "cn-beijing",
      "channel_id": "ims-channel-id",
      "rtc_user_id": "rtc-user-id",
      "rtc_token": "rtc-token",
      "aiagent_user_id": "aiagent-user-id",
      "avatar_user_id": "avatar-user-id",
      "artc_app_id": "95c78806-66c5-4094-b072-6af4964c2b6b",
      "ims_status": "Created",
      "call_log_url": null
    },
    "required_fairy_jade": 20
  }
}
```

前端联调重点：

1. 调 `start`
2. 读取 `data.ims`
3. 用 `region`、`channel_id`、`rtc_user_id`、`rtc_token`、`artc_app_id` 初始化并入会
4. 后续用 `active` / `status` 查询会话业务状态

### 7.3 `GET /api/chat/voice_call/active`

作用：

- 获取当前用户进行中的语音通话
- 真实 IMS 模式下会实时查询 IMS `DescribeAIAgentInstance`

查询参数：

- `character_id`：可选

示例：

```text
GET https://becomestar.com.cn/api/chat/voice_call/active?character_id=68b53a6a42fb612951702f61
```

返回：

- 有会话时：返回完整会话结构
- 无会话时：`data` 为 `null`

### 7.4 `GET /api/chat/voice_call/<session_id>/status`

作用：

- 按 `session_id` 查询会话状态
- 真实 IMS 模式下会实时查询 IMS `DescribeAIAgentInstance`

示例：

```text
GET https://becomestar.com.cn/api/chat/voice_call/voice_1234567890abcdef12345678/status
```

真实 IMS 模式下，后端当前对 IMS 状态的归一化规则是：

- `Created` -> `connecting`
- `Executing` -> `listening`
- `Finished` -> `ended`
- 其他状态 -> `connecting`

也就是说，`active/status` 的 `call_status` 是基于真实 IMS 实例状态同步出来的，不应再按旧 mock 状态机理解。

### 7.5 `POST /api/chat/voice_call/<session_id>/user_turn`

作用：

- 提交一轮用户输入
- 真实 IMS 模式下会调用 IMS `SendAIAgentText`

请求体：

```json
{
  "input_mode": "voice",
  "content": "你好，很高兴认识你",
  "audio_url": "https://becomestar.com.cn/upload/user-voice.wav",
  "audio_duration_ms": 2100
}
```

说明：

- `input_mode` 只支持 `voice` / `text`
- `content` 必填
- 当前接口会先把用户消息写入 `messages`
- `input_mode=voice` 时，消息会存成 `message_type=voice`
- 如果没有传 `audio_url`，后端会补一个 `mock-audio` 占位地址
- 真实 IMS 模式下，该接口当前只负责把文本转给 `SendAIAgentText`，不会在这个接口里直接生成角色语音消息

返回示例：

```json
{
  "code": 200,
  "message": "用户输入提交成功",
  "data": {
    "session_id": "voice_1234567890abcdef12345678",
    "call_status": "thinking",
    "user_message": {
      "id": "6914b6c91e6bfd32d7a549d8",
      "content": "你好，很高兴认识你",
      "message_type": "voice",
      "sender": "user",
      "timestamp": 1762213360,
      "audio_url": "https://becomestar.com.cn/upload/user-voice.wav",
      "audio_duration_ms": 2100
    },
    "updated_at": 1762213360
  }
}
```

补充说明：

- `user_turn` 返回里的 `call_status` 当前固定回给 `thinking`
- 但后续再调 `active/status` 时，真实 IMS 模式仍会按 `DescribeAIAgentInstance` 的结果覆盖为 IMS 归一化状态

### 7.6 `POST /api/chat/voice_call/<session_id>/mic`

作用：

- 更新业务层记录中的 `mic_muted`

请求体：

```json
{
  "mic_muted": true
}
```

返回示例：

```json
{
  "code": 200,
  "message": "麦克风状态更新成功",
  "data": {
    "session_id": "voice_1234567890abcdef12345678",
    "mic_muted": true,
    "updated_at": 1762213380,
    "handled_by": "client_sdk"
  }
}
```

注意：

- 在真实 IMS 模式下，实时麦克风控制应由客户端 SDK 处理
- 当前接口只保留业务层返回
- 响应里会带 `handled_by`
- 真实 IMS 模式下 `handled_by` 为 `client_sdk`

### 7.7 `POST /api/chat/voice_call/<session_id>/interrupt`

作用：

- 处理业务层“打断请求”

返回示例：

```json
{
  "code": 200,
  "message": "已处理打断请求",
  "data": {
    "session_id": "voice_1234567890abcdef12345678",
    "call_status": "listening",
    "can_interrupt": false,
    "updated_at": 1762213390,
    "handled_by": "client_sdk"
  }
}
```

注意：

- 在真实 IMS 模式下，实时音频打断应由客户端 SDK 处理
- 当前接口只保留业务层返回
- 响应里会带 `handled_by`
- 真实 IMS 模式下不会在这里额外调用 IMS 的实时音频控制接口

### 7.8 `POST /api/chat/voice_call/<session_id>/end`

作用：

- 结束语音通话
- 真实 IMS 模式下会调用 IMS `StopAIAgentInstance`

请求体：

```json
{
  "reason": "user_hangup"
}
```

返回：

- 返回完整会话结构
- 结束后 `call_status` 为 `ended`
- 真实 IMS 模式下，后端会把 `ims_status` 写成 `Finished`

---

## 8. 聊天历史接口中的语音消息结构

### `GET /api/chat/messages/<conversation_id>`

作用：

- 获取会话消息列表

示例：

```text
GET https://becomestar.com.cn/api/chat/messages/6914b6c91e6bfd32d7a549d7?page=1&limit=20
```

返回结构中，消息主键字段是：

- `_id`

不是 `id`。

普通消息示例：

```json
{
  "_id": "6914b6c91e6bfd32d7a549d8",
  "user_id": "68b45726c51416f4807ce37a",
  "character_id": "68b53a6a42fb612951702f61",
  "branch_id": null,
  "content": "你好，很高兴认识你",
  "message_type": "text",
  "sender": "user",
  "timestamp": 1762213360,
  "is_read": true
}
```

语音消息在普通消息字段之外，还会额外带：

- `audio_url`
- `audio_duration_ms`

语音消息示例：

```json
{
  "_id": "6914b6c91e6bfd32d7a549d9",
  "user_id": "68b45726c51416f4807ce37a",
  "character_id": "68b53a6a42fb612951702f61",
  "branch_id": null,
  "content": "你好，很高兴认识你",
  "message_type": "voice",
  "sender": "user",
  "timestamp": 1762213360,
  "audio_url": "https://becomestar.com.cn/upload/user-voice.wav",
  "audio_duration_ms": 2100,
  "is_read": true
}
```

说明：

- 当前后端会把查询出的消息按时间从旧到新返回
- 如果是最后一页且角色有简介，还会额外插入一条 `message_type=intro` 的简介消息
- 语音消息里的 `audio_url` 可能是真实上传地址，也可能是后端补的 `mock-audio` 占位地址

---

## 9. 发给前端的接口清单

- `GET /api/characters/<character_id>/voice-config`
- `POST /api/characters/<character_id>/voice-config`
- `POST /api/chat/voice_call/start`
- `GET /api/chat/voice_call/active`
- `GET /api/chat/voice_call/<session_id>/status`
- `POST /api/chat/voice_call/<session_id>/user_turn`
- `POST /api/chat/voice_call/<session_id>/mic`
- `POST /api/chat/voice_call/<session_id>/interrupt`
- `POST /api/chat/voice_call/<session_id>/end`
- `GET /api/chat/messages/<conversation_id>`
