# IMS 语音通话接口文档

## 1. 文档范围

本文档描述当前后端已经实现并建议前端正式接入的 IMS 语音通话链路。

当前正式方案是：

- 前端使用阿里云官方 AICallKit / ARTC SDK 负责实时通话
- 后端负责创建 IMS 实例、下发 SDK 初始化参数、保存业务状态、落库字幕和结束会话

不要再把后端的 `/user_turn` 理解成“完整实时语音主链路”。

它现在只保留为：

- 文本插话兼容接口
- 调试接口
- 非正式语音场景的备用入口

所有接口只使用 `GET` / `POST`。

文档中的时间字段示例均为秒级时间戳。当前项目会统一把常见时间字段转换为秒级时间戳返回。

示例域名统一使用：`https://becomestar.com.cn`

---

## 2. 总体分工

### 2.1 前端负责

- 调 `start` / `active` 获取会话和 SDK 入会参数
- 使用官方 SDK 初始化并入会
- 麦克风采集
- 降噪 / VAD / ASR / 字幕
- AI 音频播放
- 打断
- 网络和实时状态回调
- 将关键事件和最终字幕通过 `/event` 上报给后端

### 2.2 后端负责

- 鉴权与业务权限校验
- 发起 IMS `GenerateAIAgentCall`
- 下发 `agent_id`、`agent_type`、`region`、`rtc_token` 等 SDK 参数
- 保存会话
- 保存前端回传的字幕 / 事件
- 提供恢复会话能力
- 调用 IMS `DescribeAIAgentInstance`
- 调用 IMS `StopAIAgentInstance`

---

## 3. 当前后端使用到的 IMS 能力

- `GenerateAIAgentCall`
- `DescribeAIAgentInstance`
- `StopAIAgentInstance`
- `SendAIAgentText`

其中：

- `GenerateAIAgentCall`：正式通话创建入口
- `DescribeAIAgentInstance`：`active` / `status` 兜底同步 IMS 状态
- `StopAIAgentInstance`：正式挂断
- `SendAIAgentText`：仅保留给 `/user_turn` 兼容文本插话

创建 IMS 实例时存在 region 回退逻辑，尝试顺序为：

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
- `POST /api/chat/voice_call/<session_id>/event`
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

## 5. 正式主链路

推荐前端按以下顺序接入：

1. 进入语音页先调 `GET /api/chat/voice_call/active`
2. 如果存在进行中会话，直接使用返回的 `data.sdk`
3. 如果不存在进行中会话，再调 `POST /api/chat/voice_call/start`
4. 使用返回的 `data.sdk` 初始化官方 SDK 并入会
5. 通话中的字幕、状态、打断等回调，用 `POST /api/chat/voice_call/<session_id>/event` 上报
6. 页面重进或断线恢复时，再调 `active` / `status`
7. 挂断时调用 `POST /api/chat/voice_call/<session_id>/end`

注意：

- 正式实时语音体验以官方 SDK 回调为准
- `active` / `status` 主要用于恢复和兜底，不作为实时音频主状态源

---

## 6. 会话返回结构

`start` / `active` / `status` / `end` 返回的 `data` 主体是同一类会话结构。

示例：

```json
{
  "session_id": "voice_1234567890abcdef12345678",
  "conversation_id": "6914b6c91e6bfd32d7a549d7",
  "character_id": "68b53a6a42fb612951702f61",
  "character_name": "化世景",
  "branch_id": null,
  "call_status": "connecting",
  "sdk_status": "connecting",
  "sdk_mode": "official_aicallkit",
  "sdk_last_event_type": null,
  "mic_muted": false,
  "can_interrupt": false,
  "can_switch_to_text": true,
  "duration_seconds": 0,
  "latest_user_text": null,
  "latest_ai_text": null,
  "current_message": null,
  "last_message_time": 1780272000,
  "created_at": 1780272000,
  "updated_at": 1780272000,
  "expires_at": 1780273800,
  "provider": "aliyun_ims",
  "sdk": {
    "mode": "official_aicallkit",
    "agent_id": "ffcfce363eff49a7a0a33f23ea02ca1b",
    "agent_type": "VoiceAgent",
    "region": "cn-beijing",
    "agent_user_id": "aiagent-user-id",
    "rtc_user_id": "rtc-user-id",
    "rtc_token": "rtc-token",
    "artc_app_id": "95c78806-66c5-4094-b072-6af4964c2b6b"
  },
  "ims": {
    "agent_id": "ffcfce363eff49a7a0a33f23ea02ca1b",
    "agent_type": "VoiceAgent",
    "instance_id": "ims-instance-id",
    "region": "cn-beijing",
    "channel_id": "ims-channel-id",
    "rtc_user_id": "rtc-user-id",
    "rtc_token": "rtc-token",
    "aiagent_user_id": "aiagent-user-id",
    "agent_user_id": "aiagent-user-id",
    "avatar_user_id": "avatar-user-id",
    "artc_app_id": "95c78806-66c5-4094-b072-6af4964c2b6b",
    "ims_status": "Created",
    "call_log_url": null
  }
}
```

字段说明：

- `call_status`：后端业务态，可能来自 IMS 兜底状态或前端 `/event` 上报
- `sdk_status`：最近一次 SDK 侧同步到后端的状态
- `sdk_mode`：当前固定为 `official_aicallkit`
- `sdk`：前端初始化官方 SDK 时优先读取的字段
- `ims`：IMS 原始实例信息与排障字段
- `required_fairy_jade`：只会在 `start` 返回中额外带上，表示当前开通语音所需仙玉数

前端初始化官方 SDK 时应优先使用：

- `data.sdk.agent_id`
- `data.sdk.agent_type`
- `data.sdk.region`
- `data.sdk.agent_user_id`
- `data.sdk.rtc_user_id`
- `data.sdk.rtc_token`
- `data.sdk.artc_app_id`

不要前端本地硬编码：

- `agentId`
- `region`

`agentType` 当前统一固定为：

- `VoiceAgent`

---

## 7. 角色音色配置

### 7.1 `GET /api/characters/<character_id>/voice-config`

作用：

- 获取当前用户对该角色保存的本地音色偏好
- 返回可试听的音色列表

注意：

- 这是本地角色音色偏好
- 不代表已经实时下发到 IMS 通话实例

### 7.2 `POST /api/characters/<character_id>/voice-config`

作用：

- 保存当前用户对该角色的本地音色偏好

请求体：

```json
{
  "voice_type": "male_gentle",
  "voice_code": "male_gentle_01"
}
```

---

## 8. 试听 / 占位音频

### `GET /api/chat/voice_call/mock-audio/<audio_id>.wav`

作用：

- 音色试听
- 占位资源
- 本地调试

限制说明：

- 它不是正式 IMS 通话的实时音频来源

示例：

```text
GET https://becomestar.com.cn/api/chat/voice_call/mock-audio/preview_male_deep_01.wav?duration_ms=1800
```

---

## 9. `POST /api/chat/voice_call/start`

作用：

- 发起语音通话
- 创建 IMS 实例
- 返回前端初始化官方 SDK 所需参数

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
- 用户同一时间只允许一个进行中的语音会话
- 若已有进行中会话，会直接返回已有会话

成功返回：

- 返回第 6 节中的完整会话结构

前端联调重点：

1. 调 `start`
2. 读取 `data.sdk`
3. 用 `agent_id`、`agent_type`、`region`、`agent_user_id`、`rtc_user_id`、`rtc_token`、`artc_app_id` 初始化 SDK
4. 进入房间

---

## 10. `GET /api/chat/voice_call/active`

作用：

- 获取当前用户进行中的语音通话
- 用于页面重进、恢复会话、避免重复创建
- 真实 IMS 模式下会兜底查询 `DescribeAIAgentInstance`

查询参数：

- `character_id`：可选

示例：

```text
GET https://becomestar.com.cn/api/chat/voice_call/active?character_id=68b53a6a42fb612951702f61
```

返回：

- 有会话时：返回完整会话结构
- 无会话时：`data` 为 `null`

---

## 11. `GET /api/chat/voice_call/<session_id>/status`

作用：

- 按 `session_id` 查询会话状态
- 页面重进或前端需要兜底时使用
- 真实 IMS 模式下会兜底查询 `DescribeAIAgentInstance`

示例：

```text
GET https://becomestar.com.cn/api/chat/voice_call/voice_1234567890abcdef12345678/status
```

说明：

- 若 IMS 只返回通用执行态，后端会尽量保留前端最近上报的更细粒度 `sdk_status`

---

## 12. `POST /api/chat/voice_call/<session_id>/event`

作用：

- 接收前端官方 SDK 的事件和字幕回调
- 同步业务状态
- 持久化最终字幕到消息表

请求体：

```json
{
  "event_type": "ai_subtitle",
  "content": "你好，很高兴见到你",
  "is_final": true,
  "sentence_id": 17,
  "metadata": {
    "source": "sdk_callback"
  }
}
```

支持的 `event_type`：

- `call_started`
- `call_connected`
- `status_update`
- `user_subtitle`
- `ai_subtitle`
- `user_interrupt`
- `call_ended`
- `call_failed`

补充说明：

- `status_update` 额外需要 `call_status`
- `call_status` 仅支持：`connecting` / `listening` / `thinking` / `speaking`
- `user_subtitle` / `ai_subtitle` 需要 `content`
- `is_final=true` 的字幕会被落库为聊天消息

重要约定：

- `call_ended` / `call_failed` 只用于事件上报和排障记录
- 正式结束通话仍然必须调用 `/end`
- 不要把 `call_ended` 事件当作 `/end` 的替代

成功返回示例：

```json
{
  "code": 200,
  "message": "语音通话事件上报成功",
  "data": {
    "session_id": "voice_1234567890abcdef12345678",
    "event_id": "68395ef4bcdb6e3d7f54f401",
    "event_type": "ai_subtitle",
    "call_status": "speaking",
    "sdk_status": "speaking",
    "updated_at": 1780272060,
    "message": {
      "id": "68395ef4bcdb6e3d7f54f402",
      "content": "你好，很高兴见到你",
      "message_type": "text",
      "sender": "character",
      "timestamp": 1780272060,
      "audio_url": null,
      "audio_duration_ms": null
    }
  }
}
```

推荐前端上报方式：

1. 入会成功后上报 `call_started` / `call_connected`
2. 用户最终字幕上报 `user_subtitle`
3. AI 最终字幕上报 `ai_subtitle`
4. 打断时上报 `user_interrupt`
5. 发生错误时上报 `call_failed`
6. 离会时上报 `call_ended`
7. 然后正式调用 `/end`

---

## 13. `POST /api/chat/voice_call/<session_id>/end`

作用：

- 正式结束通话
- 后端调用 IMS `StopAIAgentInstance`
- 将会话置为结束态

请求体：

```json
{
  "reason": "user_hangup"
}
```

成功返回：

- 返回完整会话结构

注意：

- 前端 SDK 自己离会后，仍然应继续调用 `/end`
- `/end` 才是正式的服务端收尾接口

---

## 14. `POST /api/chat/voice_call/<session_id>/user_turn`

作用：

- 文本插话兼容接口
- 调试接口

当前行为：

- 在真实 IMS 模式下，后端会调用 `SendAIAgentText`
- 接口本身只返回“提交成功”与当前会话状态
- 不保证在该接口响应里同步返回 AI 回复

因此：

- 它不是正式实时语音主链路
- 不建议前端再把它当成完整语音对话入口

---

## 15. `POST /api/chat/voice_call/<session_id>/mic`

作用：

- 同步本地静音状态到后端业务态

说明：

- 实时静音能力仍以前端 SDK 为准
- 该接口主要用于业务状态持久化

---

## 16. `POST /api/chat/voice_call/<session_id>/interrupt`

作用：

- 同步一次打断行为到后端业务态

说明：

- 实时打断动作仍以前端 SDK 实际执行为准
- 建议同时通过 `/event` 上报 `user_interrupt`

---

## 17. 聊天记录

正式接入官方 SDK 后：

- 最终用户字幕会通过 `/event` 的 `user_subtitle` 落库
- 最终 AI 字幕会通过 `/event` 的 `ai_subtitle` 落库

前端仍可通过原有接口拉取聊天记录：

```text
GET /api/chat/messages/<conversation_id>?page=1&limit=20
```

---

## 18. 推荐前端实现

建议前端按下面方式实现：

1. 进入页面调 `active`
2. 无会话时调 `start`
3. 用 `data.sdk` 初始化官方 SDK
4. 实时语音能力全部走官方 SDK
5. 将关键事件和最终字幕通过 `/event` 上报
6. 页面恢复时用 `active` / `status` 兜底
7. 挂断时先离会，再调 `/end`

一句话结论：

- 实时语音体验由前端 SDK 负责
- 会话创建、配置下发、业务状态与落库由后端负责
 