## App版本检查接口文档

本文档描述客户端版本检查接口和后台版本配置接口。

注意事项：
- 本项目仅使用 `GET` 和 `POST`
- 新增时间字段 `published_at` 已纳入统一时间戳转换，返回为秒级时间戳
- 版本比较优先使用 `build`

---

## 1. 客户端查询最新版本

**接口地址**

`GET /api/app/version/check`

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |      
| platform | string | 是 | `android` 或 `ios` |
| current_build | int | 否 | 当前安装包build号，推荐传 |
| current_version | string | 否 | 当前安装包版本号，不传build时可作为兜底 |
| channel | string | 否 | 渠道，默认 `official` |

说明：
- `current_build` 和 `current_version` 至少传一个
- 推荐客户端直接读取本机安装包的 `build` 后传给服务端

**调用示例**

```bash
curl -X GET "http://localhost:5000/api/app/version/check?platform=android&current_build=110&channel=official"
```

**成功响应示例**

```json
{
  "code": 200,
  "message": "获取版本信息成功",
  "data": {
    "has_update": true,
    "force_update": false,
    "platform": "android",
    "channel": "official",
    "current_version": null,
    "current_build": 110,
    "latest_version": "1.2.0",
    "latest_build": 120,
    "min_supported_build": 100,
    "title": "发现新版本",
    "subtitle": "",
    "release_notes": [
      "TT商店增加购买盲盒功能",
      "AI角色回复文字流式输出展示优化"
    ],
    "action_type": "download",
    "action_url": "https://example.com/app-release.apk",
    "button_text": "下载更新",
    "cancel_text": "暂时不更新",
    "published_at": 1762752000
  }
}
```

**字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| has_update | bool | 是否存在更新 |
| force_update | bool | 是否强制更新 |
| latest_version | string/null | 最新版本号 |
| latest_build | int/null | 最新build号 |
| min_supported_build | int/null | 最低支持build，小于该值可判定为强更 |
| title | string | 弹窗标题 |
| subtitle | string | 弹窗副标题 |
| release_notes | array | 更新内容列表 |
| action_type | string | `download` / `store` / `tip` |
| action_url | string | 安卓下载地址或 iOS App Store 地址 |
| button_text | string | 主按钮文案 |
| cancel_text | string | 次按钮文案 |
| published_at | int/null | 发布时间，秒级时间戳 |

**错误码**

| 错误码 | 说明 |
| --- | --- |
| 22001 | platform是必填项 |
| 22002 | platform不合法 |
| 22003 | current_build不是整数 |
| 22004 | current_build和current_version都未提供 |
| 22099 | 服务器内部错误 |

---

## 2. 后台获取版本列表

**接口地址**

`GET /api/admin/app-versions`

**请求头**

```text
Authorization: Bearer <admin_token>
```

**查询参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| platform | string | 否 | `android` / `ios` |
| channel | string | 否 | 渠道名 |
| is_active | bool | 否 | 是否只看当前生效版本 |

**调用示例**

```bash
curl -X GET "http://localhost:5000/api/admin/app-versions?platform=android" \
  -H "Authorization: Bearer <admin_token>"
```

**成功响应示例**

```json
{
  "code": 200,
  "message": "获取版本列表成功",
  "data": {
    "versions": [
      {
        "_id": "68203f59dc0c7d404659f6aa",
        "platform": "android",
        "channel": "official",
        "version": "1.2.0",
        "build": 120,
        "min_supported_build": 100,
        "force_update": false,
        "title": "发现新版本",
        "subtitle": "",
        "release_notes": [
          "TT商店增加购买盲盒功能",
          "AI角色回复文字流式输出展示优化"
        ],
        "android_download_url": "https://example.com/app-release.apk",
        "app_store_url": "",
        "button_text": "下载更新",
        "cancel_text": "暂时不更新",
        "is_active": true,
        "created_at": 1762751000,
        "updated_at": 1762752000,
        "published_at": 1762752000
      }
    ],
    "total": 1
  }
}
```

---

## 3. 后台新增或更新版本配置

**接口地址**

`POST /api/admin/app-version/save`

**请求头**

```text
Content-Type: application/json
Authorization: Bearer <admin_token>
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | string | 否 | 版本配置ID，传则更新，不传则新增 |
| platform | string | 是 | `android` / `ios` |
| channel | string | 否 | 渠道，默认 `official` |
| version | string | 是 | 版本号，例如 `1.2.0` |
| build | int | 是 | build号 |
| min_supported_build | int | 否 | 最低支持build |
| force_update | bool | 否 | 是否强制更新 |
| title | string | 否 | 弹窗标题，默认 `发现新版本` |
| subtitle | string | 否 | 弹窗副标题 |
| release_notes | array/string | 否 | 更新内容，可传数组或换行字符串 |
| android_download_url | string | 否 | 安卓下载地址 |
| app_store_url | string | 否 | iOS商店地址 |
| button_text | string | 否 | 主按钮文案 |
| cancel_text | string | 否 | 次按钮文案 |
| is_active | bool | 否 | 是否设为当前生效版本 |

说明：
- 安卓版本若 `is_active=true`，必须提供 `android_download_url`
- 同一 `platform + channel` 下 `build` 不能重复
- 当某个版本保存为 `is_active=true` 时，服务端会自动将同平台同渠道的其他版本改为 `is_active=false`

**新增示例**

```bash
curl -X POST "http://localhost:5000/api/admin/app-version/save" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "platform": "android",
    "channel": "official",
    "version": "1.2.0",
    "build": 120,
    "min_supported_build": 100,
    "force_update": false,
    "title": "发现新版本",
    "subtitle": "",
    "release_notes": [
      "TT商店增加购买盲盒功能",
      "AI角色回复文字流式输出展示优化"
    ],
    "android_download_url": "https://example.com/app-release.apk",
    "button_text": "下载更新",
    "cancel_text": "暂时不更新",
    "is_active": true
  }'
```

**更新示例**

```bash
curl -X POST "http://localhost:5000/api/admin/app-version/save" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "id": "68203f59dc0c7d404659f6aa",
    "platform": "ios",
    "channel": "official",
    "version": "1.2.0",
    "build": 120,
    "force_update": true,
    "title": "发现新版本",
    "subtitle": "请前往苹果商店进行更新~",
    "release_notes": [
      "TT商店增加购买盲盒功能",
      "AI角色回复文字流式输出展示优化"
    ],
    "app_store_url": "https://apps.apple.com/app/example/id123456789",
    "button_text": "好的",
    "cancel_text": "",
    "is_active": true
  }'
```

**成功响应示例**

```json
{
  "code": 200,
  "message": "保存版本配置成功",
  "data": {
    "version": {
      "_id": "68203f59dc0c7d404659f6aa",
      "platform": "android",
      "channel": "official",
      "version": "1.2.0",
      "build": 120,
      "min_supported_build": 100,
      "force_update": false,
      "title": "发现新版本",
      "subtitle": "",
      "release_notes": [
        "TT商店增加购买盲盒功能",
        "AI角色回复文字流式输出展示优化"
      ],
      "android_download_url": "https://example.com/app-release.apk",
      "app_store_url": "",
      "button_text": "下载更新",
      "cancel_text": "暂时不更新",
      "is_active": true,
      "created_at": 1762751000,
      "updated_at": 1762752000,
      "published_at": 1762752000
    }
  }
}
```

**错误码**

| 错误码 | 说明 |
| --- | --- |
| 22101 | platform不合法 |
| 22102 | version是必填项 |
| 22103 | build是必填项 |
| 22104 | build必须是整数 |
| 22105 | min_supported_build必须是整数 |
| 22106 | min_supported_build不能大于build |
| 22107 | 安卓生效版本必须配置下载地址 |
| 22108 | 无效的版本ID |
| 22109 | 版本配置不存在 |
| 22110 | 同平台同渠道下build不能重复 |
| 22199 | 服务器内部错误 |

---

## 4. 后台上传Android APK到OSS

**接口地址**

`POST /api/upload/apk`

**请求头**

```text
Authorization: Bearer <admin_token>
Content-Type: multipart/form-data
```

**表单参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | APK文件 |

**调用示例**

```bash
curl -X POST "http://localhost:5000/api/upload/apk" \
  -H "Authorization: Bearer <admin_token>" \
  -F "file=@/path/to/app-release.apk"
```

**成功响应示例**

```json
{
  "code": 200,
  "message": "APK上传成功",
  "data": {
    "apk_url": "https://shenchu.oss-cn-hangzhou.aliyuncs.com/app_release/android/7399999999999999999_app-release.apk",
    "filename": "7399999999999999999_app-release.apk",
    "original_filename": "app-release.apk",
    "size": 73400320,
    "object_key": "app_release/android/7399999999999999999_app-release.apk",
    "snowflake_id": "7399999999999999999"
  }
}
```

**错误码**

| 错误码 | 说明 |
| --- | --- |
| 18021 | 没有找到APK文件或没有选择文件 |
| 18022 | 文件不是APK格式 |
| 18023 | APK文件超过大小限制 |
| 18103 | 上传APK到OSS失败 |
