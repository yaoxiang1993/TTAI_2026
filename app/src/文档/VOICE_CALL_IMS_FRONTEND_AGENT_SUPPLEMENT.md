# IMS 语音通话前端 Agent 接入补充说明

## 1. 文档目的

本文是 [VOICE_CALL_IMS_API.md](./VOICE_CALL_IMS_API.md) 的补充。

主要解决一个容易混淆的问题：

- AICallKit SDK 初始化时会出现 `agentId`、`agentType`、`region` 等字段
- 但当前项目的正式链路并不是“前端自己创建智能体实例”
- 而是“后端先创建 IMS 实例，前端拿返回参数入会”

前端如果把这两层概念混在一起，就容易出现：

- 前端写死 `agentId`
- 前端写死 `region=cn-hangzhou`
- 后端实际创建出来的实例却在 `cn-beijing`
- 最终导致前端初始化参数和真实实例不一致

---

## 2. 当前项目采用的正式链路

当前项目使用的是：

1. 前端调用 `POST /api/chat/voice_call/start`
2. 后端调用 IMS `GenerateAIAgentCall`
3. 后端把本次实例的入会参数放到 `data.ims`
4. 前端使用 `data.ims` 初始化 SDK 并入会
5. 前端后续用 `GET /api/chat/voice_call/active` 或 `GET /api/chat/voice_call/<session_id>/status` 查询业务状态

这条链路下，前端的职责是“入会”，不是“决定这次要创建哪个智能体实例”。

---

## 3. 前端需要区分的两类 参数

### 3.1 业务实例参数

这类参数由后端创建 IMS 实例后返回，前端必须以返回值为准：

- `region`
- `channel_id`
- `rtc_user_id`
- `rtc_token`
- `aiagent_user_id`
- `avatar_user_id`
- `artc_app_id`
- `instance_id`

这些字段的来源应该是：

- `POST /api/chat/voice_call/start`
- 或已存在会话时的 `GET /api/chat/voice_call/active`

前端不要自己猜，不要自己写死。

### 3.2 SDK 初始化参数

这类参数是 AICallKit SDK 为了初始化引擎而要求的。

它们不等于“应该由前端自己决定”。

在当前项目里应按下面规则处理：

- `agentType`：前端保留，当前语音通话固定使用 `VoiceAgent`
- `region`：使用后端返回的 `data.ims.region`
- `agentUserId`：使用后端返回的 `data.ims.aiagent_user_id`
- `rtcUserId`：使用后端返回的 `data.ims.rtc_user_id`
- `rtcToken`：使用后端返回的 `data.ims.rtc_token`

---

## 4. `agentId` 应该怎么处理

### 4.1 结论

前端不要硬编码 `agentId`。

原因：

1. 当前真实智能体 ID 是后端配置的 `ALIYUN_IMS_AGENT_ID`
2. 实际由后端调用 `GenerateAIAgentCall` 时使用
3. 如果前端自己写死一份，会形成双来源
4. 一旦后端配置变更，前端硬编码就会过期

### 4.2 推荐规则

推荐按下面约定执行：

- 如果当前前端 SDK 初始化不需要 `agentId`：前端不传
- 如果当前前端 SDK 初始化强制要求 `agentId`：由后端补充 `data.ims.agent_id`
- 前端读取后端返回的 `data.ims.agent_id`
- 前端不要在本地写死控制台里的 Agent ID

### 4.3 当前协作建议

如果前端现阶段已经接了必须填写 `agentId` 的初始化逻辑，那么应和后端对齐为：

- 后端：在 `data.ims` 中新增 `agent_id`
- 前端：改为读取 `data.ims.agent_id`
- 前端：删除本地硬编码的 `agentId`

---

## 5. `agentType` 应该怎么处理

`agentType` 是 SDK 层的“智能体类型”配置，不是业务主键。

当前项目的语音通话页面应固定使用：

- `VoiceAgent`

只有在页面真正接的是数字人视频流时，才应切换到对应的视频/数字人类型。

所以：

- `agentType` 可以由前端固定写死
- 但这个值表达的是“SDK 使用哪类能力”
- 不是“本次 IMS 实例由谁创建”

---

## 6. `region` 为什么不能硬编码

当前后端创建 IMS 实例时存在 region 回退逻辑，谁创建成功就返回谁。

例如：

- 配置的 `ALIYUN_IMS_REGION`
- `cn-beijing`
- `cn-hangzhou`
- `cn-shanghai`
- `cn-shenzhen`

因此：

- 后端本次真正创建成功的 region 可能是 `cn-beijing`
- 前端如果仍然写死 `cn-hangzhou`
- 就可能和真实实例参数不一致

结论：

- `region` 必须使用后端返回值
- 不允许前端本地写死

---

## 7. 前端接入规则

前端应遵守以下规则：

1. 先调 `POST /api/chat/voice_call/start`
2. 读取返回的 `data.ims`
3. 使用返回值初始化 SDK
4. `agentType` 固定 `VoiceAgent`
5. `region` 必须使用 `data.ims.region`
6. `agentUserId` 必须使用 `data.ims.aiagent_user_id`
7. `rtcUserId` 必须使用 `data.ims.rtc_user_id`
8. `rtcToken` 必须使用 `data.ims.rtc_token`
9. `agentId` 不允许硬编码；如 SDK 必填，则读取 `data.ims.agent_id`
10. 通话中的业务状态使用 `active` / `status` 查询，不靠前端本地猜测

---

## 8. 错误示例

下面这类写法不应继续保留：

```kotlin
artcaiCallConfig.agentId = "ffcfce363eff49a7a0a33f23ea02ca1b"
artcaiCallConfig.region = "cn-hangzhou"
artcaiCallConfig.agentType = ARTCAICallEngine.ARTCAICallAgentType.VoiceAgent
```

问题在于：

- `agentId` 被前端硬编码
- `region` 被前端硬编码
- 只有 `agentType` 是适合前端固定配置的

---

## 9. 推荐示例

```kotlin
val ims = response.data.ims

val config = ARTCAICallConfig()
config.agentType = ARTCAICallEngine.ARTCAICallAgentType.VoiceAgent
config.region = ims.region
config.agentUserId = ims.aiagentUserId

if (!ims.agentId.isNullOrBlank()) {
    config.agentId = ims.agentId
}

engine.init(config)
engine.call(ims.rtcToken)
```

说明：

- `agentType` 固定由前端控制
- `region` / `agentUserId` / `rtcToken` 来自后端
- `agentId` 只有在 SDK 确实要求时才使用，并且来源仍然应是后端返回值

---

## 10. 给前端的最终结论

一句话结论：

- 当前项目不是前端直连创建智能体
- 前端只负责用后端返回的 IMS 实例参数入会
- `agentType` 可固定为 `VoiceAgent`
- `region` 和 `agentId` 都不要硬编码
- 如果 SDK 必须传 `agentId`，则由后端补返回，前端直接读取
