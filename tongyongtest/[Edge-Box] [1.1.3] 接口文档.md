# [Edge-Box] [1.1.3] 接口文档

> 后端行业产品部

---

## 目录

- [1. 概述](#1-概述)
- [2. 接口规范](#2-接口规范)
  - [2.1 统一流程](#21-统一流程)
  - [2.2 Token获取/续签](#22-token获取续签)
- [3. 数据格式规范](#3-数据格式规范)
  - [3.1 请求报文](#31-请求报文)
  - [3.2 应答报文](#32-应答报文)
  - [3.3 注意事项](#33-注意事项)
  - [3.4 示例](#34-示例)
- [4. 接口描述](#4-接口描述)
  - [4.1 设备激活](#41-设备激活)
  - [4.2 用户管理](#42-用户管理)
  - [4.3 密保问题查询/更新](#43-密保问题查询更新)
  - [4.4 忘记密码修改流程](#44-忘记密码修改流程)
  - [4.5 通道管理](#45-通道管理)
  - [4.6 实时视频](#46-实时视频)
  - [4.7 智能任务管理](#47-智能任务管理)
  - [4.8 系统规格和资源查询](#48-系统规格和资源查询)
  - [4.9 日志查询](#49-日志查询)
  - [4.10 系统设置与维护](#410-系统设置与维护)
  - [4.11 上传中心管理](#411-上传中心管理)
  - [4.12 库与人员管理](#412-库与人员管理)
  - [4.13 图片服务](#413-图片服务)
  - [4.14 事件与状态](#414-事件与状态)
  - [4.15 流数据推送](#415-流数据推送)
  - [4.16 存储设置](#416-存储设置)

---

## 1. 概述

本文档统一描述了对外提供的API接口，并提供代码示例，任何业务可基于此API文档做二次开发。

### 相关约定

| 约定 | 说明 |
|------|------|
| 协议 | HTTP |
| 服务端口 | 80 |
| 消息格式 | JSON |
| 字符编码 | 请求和回复都使用UTF-8 |
| 接口请求频率 | 同一接口调用频率建议低于1QPS |

---

## 2. 接口规范

### 2.1 统一流程

各业务使用本产品第一步是登录，登录后得到token。

期间业务请求都要在HTTP body中的`basic`节点中带上token，否则服务端会拒绝请求，如：

```json
"basic" : {
    "ver" : "1.0",
    "id" : 2,
    "time" : 156541727465,
    "nonce" : 2553352443,
    "token" : "xxxxxxxxxxxxxxx"
},
"data" : {
}
```

任何请求在返回token失效时，请求方需使用旧的token通过"token续签"接口获取新的token，保证token有效性。

### 2.2 Token获取/续签

各业务使用时先登录获取Token，并维护Token的有效性，在使用完后登出系统。

- 用户登录获取Token，参见"4.2.7 用户登录"
- 在请求返回错误-7004（Token time out）时，需续签Token，参见"4.2.9 续签token"
- 除了获取Token和续签Token接口外，都要在HTTP body中的basic节点中带上token，否则拒绝服务，并统一返回-7003（Token invalid）

---

## 3. 数据格式规范

### 3.1 请求报文

HTTP请求的Header都具有如下信息：

| 参数名 | 类型 | 是否必填 | 格式 | 说明 |
|--------|------|----------|------|------|
| Content-Type | string | 是 | application/json | 请求格式，填写application/json |

#### basic（大小写敏感）

1. **ver**：本报文版本，字符串，"主版本.子版本"，如：12.7
2. **time**：请求报文产生时本机UTC时间，1970年开始的秒数，32位无符号整数，数值
3. **id**：命令序号，由请求端产生，用于接收到回复时区分哪一次请求，32位无符号整数，数值（可以为字符串）
4. **nonce**：随机数，一定时期内不得重复，32位无符号整数，数值
5. **sign**：可选，按具体协议需要，用于签名验证信息的有效性

#### data（大小写敏感）

1. 无多余字段：不能带有协议定义之外的字段
2. 无空内容字段：不带空内容的字段，如有，必须特殊说明

### 3.2 应答报文

Response的数据统一遵循如下格式：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| Content-Type | string | 是 | application/json |

#### basic（大小写敏感）

1. **ver**：按请求报文版本
2. **time**：应答报文产生时本机UTC时间，1970年开始的秒数，32位无符号整数，数值
3. **id**：按请求报文id
4. **code**：回复结果码，32位无符号整数，数值
5. **msg**：结果描述，字符串
6. **sign**：可选，按具体命令需要，用于签名验证信息的有效性

#### data（大小写敏感）

1. 无多余字段：不能带有协议定义之外的字段
2. 无空内容字段：不带空内容的字段，如有，必须特殊说明

### 3.3 注意事项

1. **大小写规则**：url为小写、单单词字段为小写、多单词为驼峰（首字母为小写）、缩写（id、sms）为小写
2. **报文长度校验**：报文过长时不响应；[通用]
3. **报文完整性校验**：包含有basic和data，包含字段完整；[通用]
4. **版本识别**：接收到请求时，如版本支持则按请求给的版本响应，如不支持，则返回错误码；[通用]
5. **时间校验**：时间与本机时间差不大于5分钟，否则返回错误码；[通用]
6. **nonce校验**：不为0的随机数；[通用]
7. **版本匹配**：版本针对单个命令协议定义，且请求报文与应答报文匹配；[通用]
8. **字段内容类型校验**：字符、bool、数值等类型；[具体]
9. **字段内容有效性校验**：字段内容无效返回错误；[具体]
10. **字段长度校验**：字段长度限制，不符合为无效字段内容，返回错误；[具体]

### 3.4 示例

#### 请求报文：

```json
{
    "basic" : {
        "ver" : "1.0",
        "id" : 2,
        "time" : 156541727465,
        "nonce" : 2553352443,
        "token" : "xxxxxxxxxxxxxxx"
    },
    "data" : {
    }
}
```

#### 应答报文：

```json
{
    "basic" : {
        "ver" : "1.0",
        "id" : 2,
        "time" : 156541727466,
        "code" : 200,
        "msg" : "success"
    },
    "data" : {
        //内容
    }
}
```

---

## 4. 接口描述

### 4.1 设备激活

#### 4.1.1 获取激活状态

- **功能**：查询激活状态
- **地址**：`http://${ip}:${port}/edgeboxapi/active/status/get`
- **方法**：POST

**请求参数**：无

**返回参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| status | bool | 激活状态 |
| public_key | string | 32位，用于加密激活信息的公钥 |
| encrypy_type | string | 加密类型 |

#### 4.1.2 激活设备

- **功能**：激活设备
- **地址**：`http://${ip}:${port}/edgeboxapi/active`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| user_name | string | 是 | 用户名，RSA加密后的密文 |
| password | string | 是 | 使用RSA加密后的密码 |
| list | array | 否 | 问题和答案的列表，恢复密码时至少需要回答2个问题 |
| encrypy_type | string | 是 | 加密类型 |

list中单个object定义如下：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| question_id | Int | 是 | 问题序号 |
| question | string | 是 | 问题，RSA加密后的密文 |
| answer | string | 是 | 使用RSA加密 |

**返回参数**：无

---

### 4.2 用户管理

#### 4.2.1 批量查询用户

- **功能**：查询用户列表
- **地址**：`http://${ip}:${port}/edgeboxapi/user/list`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| type | string | 是 | 固定为page |
| current | int | 是 | 当前页记录的offset |
| pageSize | int | 是 | 一页显示的总条数，上限50，默认10 |

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | array | 用户列表 |
| pagination | object | 分页参数 |

list中单个object定义如下：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| record_id | string | 唯一标识uuid，查询修改删除需要带上 |
| user_name | string | 用户名 |
| real_name | string | 真实姓名 |
| mobile_no | string | 电话号码 |
| password_expired_at | long | 密码过期时间 |
| status | int | 用户状态(1: 启用 2: 停用) |
| created | long | 创建时间戳 |
| role_id | string | 用户对应角色编号 |

pagination定义如下：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| current | int | 当前页记录的offset |
| pageSize | int | 一页显示的总条数，上限50，默认10 |
| total | int | 总行数 |

#### 4.2.2 创建用户

- **功能**：新建用户
- **地址**：`http://${ip}:${port}/edgeboxapi/user/create`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| user_name | string | 是 | 用户名 |
| real_name | string | 否 | 真实姓名 |
| mobile_no | string | 否 | 手机号码 |
| role_id | string | 是 | 拥有的角色id列表id，目前只支持2（创建普通用户） |
| password | string | 是 | 对password MD5值使用登录时返回的encrypt_key明文加密后的密文 |
| encrypy_type | string | 是 | 加密类型 |

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| record_id | string | uuid |

#### 4.2.3 重置密码

- **功能**：重置密码
- **地址**：`[地址待确认]/reset`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| record_id | string | 是 | 用户uuid |

**返回参数**：无

#### 4.2.4 修改密码

- **功能**：修改用户密码
- **地址**：`http://${ip}:${port}/edgeboxapi/user/password/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| password | string | 是 | 当前密码，对它的MD5值使用登录时返回的encrypt_key明文加密后的密文 |
| password_new | string | 是 | 新密码，对它的MD5值使用登录时返回的encrypt_key明文加密后的密文 |
| encrypy_type | string | 是 | 加密类型 |

**返回参数**：无

#### 4.2.5 更新用户

- **功能**：更新用户
- **地址**：`http://${ip}:${port}/edgeboxapi/user/info/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| record_id | string | 是 | 用户的uuid |
| real_name | string | 否 | 真实姓名 |
| role_id | string | 否 | 拥有的角色id列表id，目前只支持2（创建普通用户） |
| mobile_no | string | 否 | 用户手机号码 |
| status | int | 是 | 用户状态。1：正常，2：停用 |
| user_name | string | 是 | 用户名 |

**返回参数**：无

#### 4.2.6 删除指定用户

- **功能**：删除指定用户
- **地址**：`http://${ip}:${port}/edgeboxapi/user/delete`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| record_id | string | 是 | 用户的uuid |

**返回参数**：无

#### 4.2.7 用户登录

- **功能**：用户登录
- **地址**：`https://${ip}:${port}/edgeboxapi/user/login`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| user_name | string | 是 | 用户名 |
| password | string | 是 | 按加密公式计算出来的结果，并非密码的MD5 |

**密码错误时返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| locked | bool | 否 | 锁定状态，true表示锁定，false表示未锁定 |
| remaining_number | int | 否 | 剩余密码尝试次数，最多5次 |
| remaining_time | int | 否 | 剩余锁定时长，最大5分钟，单位为秒 |

**密码正确时返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| userid | string | 是 | 用户id |
| default_pwd | bool | 是 | 是否默认密码，true标识默认，false不是默认 |
| expire_pwd | bool | 是 | 密码是否过期，true过期，false有效 |
| roleid | string | 是 | 用户角色id |
| token | string | 是 | 请求token |
| encrypt_key | string | 是 | 后续传输私密数据使用AES加密Key（用登录账号的密码AES加密后传输）的密文 |
| encrypy_type | string | 是 | 加密类型 |

> **注意**：登录成功后，服务器会返回token。业务后续每次请求都要在HTTP body中的basic节点中带上token值，否则服务器会拒绝访问。若token失效，业务需用失效的token通过"续签token"接口完成续期。

#### 4.2.8 用户登出

- **功能**：用户登出
- **地址**：`https://${ip}:${port}/edgeboxapi/user/logout`
- **方法**：POST

**请求参数**：无 | **返回参数**：无

#### 4.2.9 续签token

- **功能**：续签token
- **地址**：`http://${ip}:${port}/edgeboxapi/user/token/renewal`
- **方法**：POST

**请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| userid | string | 是 | 用户id |
| roleid | string | 是 | 用户角色id |
| token | string | 是 | 新的token |
| encrypt_key | string | 是 | 后续传输私密数据使用AES加密Key（用登录账号的密码AES加密后传输）的密文 |
| encrypy_type | string | 是 | 加密类型 |

#### 4.2.10 心跳

- **功能**：心跳
- **地址**：`http://${ip}:${port}/edgeboxapi/heartbeat`
- **方法**：POST

> **备注**：Web间隔10秒发送一次心跳信息，盒子如果6次没有收到心跳即可认为Web客户端失联，此时设置token为失效状态。

---

### 4.3 密保问题查询/更新

#### 4.3.1 创建密保问题和答案

- **功能**：创建密保问题和答案
- **地址**：`http://${ip}:${port}/edgeboxapi/user/security_question/create`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| list | array | 是 | 问题和答案的列表，恢复密码时至少需要回答2个问题 |
| encrypy_type | string | 是 | 加密类型 |
| admin_pwd | string | 是 | 按加密公式计算出来的结果，并非密码的MD5 |

list中单个object定义如下：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| question_id | Int | 是 | 问题序号，必填，填写范围[1,3] |
| question | string | 是 | 问题 |
| answer | string | 是 | 答案，对它的MD5值使用登录时返回的encrypt_key明文加密后的密文 |

**返回参数**：无

#### 4.3.2 查询密保问题

- **功能**：查询密保问题
- **地址**：`http://${ip}:${port}/edgeboxapi/user/security_question/list`
- **方法**：POST

**请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| list | array | 否 | 问题和答案的列表 |

list中单个object定义如下：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| question_id | Int | 是 | 问题序号，必填，填写范围[1,3] |
| question | string | 是 | 问题 |

#### 4.3.3 校验密码答案

- **功能**：校验密码答案
- **地址**：`http://${ip}:${port}/edgeboxapi/user/security_question/check`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| list | array | 是 | 问题和答案的列表，恢复密码时至少需要回答2个问题 |
| encrypy_type | string | 是 | 加密类型 |
| user_name | string | 是 | 管理员名称 |

list中单个object定义如下：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| question_id | int | 是 | 问题序号，必填，填写范围[1,3] |
| question | string | 是 | 问题 |
| answer | string | 是 | 答案，对它的MD5值使用登录时返回的encrypt_key明文加密后的密文 |

**返回参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| result | bool | 密码问题校验结果 |

---

### 4.4 忘记密码修改流程

#### 4.4.1 找回密码

- **功能**：找回密码
- **地址**：`[地址待确认]`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| user_name | string | 是 | 超级管理员用户名 |
| password | string | 是 | 新密码，使用查询密码问题返回的公钥进行加密后的密文 |
| list | array | 是 | 问题和答案的列表，恢复密码时至少需要回答2个问题 |
| encrypy_type | string | 是 | 加密类型 |

list中单个object定义如下：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| question_id | Int | 是 | 问题序号，必填，填写范围[1,3] |
| question | string | 是 | 问题 |
| answer | string | 是 | 答案，使用查询密码问题返回的公钥进行加密后的密文 |

**返回参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| result | bool | 是 | 找回密码操作的结果，成功或失败 |

#### 4.4.2 找回密码前校验，无Token查询密保问题

- **功能**：找回密码前校验
- **地址**：`[地址待确认]/check`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| username | string | 是 | 用户名 |

**返回参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| public_key | string | 是 | 32位，用于加密激活信息的公钥 |
| encrypt_type | string | 是 | 加密类型 |
| list | array | 否 | 问题id和问题的列表，恢复密码时至少需要回答2个问题 |

list中单个object定义如下：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| question_id | Int | 是 | 问题序号，必填，填写范围[1,3] |
| question | string | 是 | 问题 |

---

### 4.5 通道管理

#### 4.5.1 查询自动搜索设备列表

- **功能**：查询局域网内自动发现的设备列表
- **地址**：`http://${ip}:${port}/edgeboxapi/device/auto_search/list`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| refresh | bool | 是 | 刷新标记，true刷新，false不刷新 |

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| devices | object array | 自动搜索设备详细数据 |

单个device格式：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| ipV4 | string | IPv4地址 |
| Ipv4_geteway | string | IPv4网关 |
| Ipv4_netmask | string | IPv4子网掩码 |
| ipV6 | string | IPv6地址 |
| Ipv6_geteway | string | IPv6网关 |
| http_port | Int | http端口 |
| service_port | Int | 服务端口 |
| serial | string | 序列号 |
| firmware_ver | string | 固件版本 |
| protocol | string | 协议 |
| mac_addr | string | MAC地址 |
| product_type | string | 产品型号 |
| verdor | string | 生产厂家 |
| device_name | string | 设备名称 |
| device_type | string | 设备类型 |
| channel_count | Int | 通道数量 |
| ethName | string | 网卡名称 |

#### 4.5.2 添加通道

- **功能**：添加通道
- **地址**：`http://${ip}:${port}/edgeboxapi/camera/add`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| channels | object array | 通道的详细数据 |

单个channel格式：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| create_time | long | 否 | 通道的创建时间，单位秒 |
| id | int | 是 | 通道唯一标识id，通道号 |
| in_out | int | 否 | 进出标识(0：默认 1：进 2：出) |
| path | string | 是 | 视频流通道URL，视频流、热成像使用 |
| latitude | string | 否 | 纬度 |
| longitude | string | 否 | 经度 |
| name | string | 是 | 通道名称 |
| protocol | string | 是 | 通道的网络协议：RTSP / NET1400 |
| type | int | 是 | 通道类型：1: 视频流通道，2: 抓拍机通道 |
| ip | string | 是 | 访问ip地址（protocol为NET1400时无意义） |
| port | Int | 是 | 访问端口号（protocol为NET1400时无意义） |
| username | string | 否 | 用户名 |
| password | string | 否 | 密码，使用登录时返回的encrypt_key明文加密后的密文 |
| vendor | string | 是 | 厂商ID |
| encrypy_type | string | 是 | 加密类型 |
| device_id | string | 是 | 1400IN设备的设备ID（protocol为NET1400时有效） |
| authentication | Int | 是 | 1400IN设备的鉴权方式：1：无需认证，2：基本认证，3：摘要认证 |

**返回参数**：无

#### 4.5.3 编辑通道

- **功能**：编辑通道
- **地址**：`http://${ip}:${port}/edgeboxapi/camera/edit`
- **方法**：POST

**请求参数**（仅填充修改字段提交即可）：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| update_time | long | 否 | 通道的最后修改时间 |
| id | int | 是 | 通道唯一标识id |
| in_out | int | 否 | 进出标识(0：默认 1：进 2：出) |
| path | string | 否 | 视频流通道URL |
| latitude | string | 否 | 纬度 |
| longitude | string | 否 | 经度 |
| name | string | 否 | 通道名称 |
| protocol | string | 否 | 通道的网络协议：RTSP / NET1400 |
| type | int | 否 | 通道类型：1: 视频流通道，2: 抓拍机通道，3: 热成像通道 |
| ip | string | 否 | 访问ip地址（protocol为NET1400时无意义） |
| port | string | 否 | 访问端口号（protocol为NET1400时无意义） |
| username | string | 否 | 用户名 |
| password | string | 否 | 密码，使用登录时返回的encrypt_key明文加密后的密文 |
| device_id | string | 是 | 1400IN设备的设备ID（protocol为NET1400时有效） |
| authentication | Int | 是 | 1400IN设备的鉴权方式：1：无需认证，2：基本认证，3：摘要认证 |

**返回参数**：无

#### 4.5.4 删除通道

- **功能**：删除通道
- **地址**：`http://${ip}:${port}/edgeboxapi/camera/delete`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| record_ids | object array | 通道id集合 |

单个record_id格式：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| id | int | 是 | 通道唯一标识id |

**返回参数**：无

#### 4.5.5 已添加通道查询

- **功能**：获取通道列表
- **地址**：`http://${ip}:${port}/edgeboxapi/camera/list`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| record_id | int | 通道id（不带参数查询全部通道） |

**返回参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| channels | object array | 通道详细数据 |

单个channel格式：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| create_time | long | 是 | 通道的创建时间 |
| id | int | 是 | 通道唯一标识id |
| in_out | int | 否 | 进出标识(0：默认 1：进 2：出) |
| path | string | 是 | 视频流通道URL |
| latitude | string | 否 | 纬度 |
| longitude | string | 否 | 经度 |
| name | string | 是 | 通道名称 |
| protocol | string | 是 | 通道的网络协议：RTSP / NET1400 |
| type | int | 是 | 通道类型：1: 视频流通道，2: 抓拍机通道 |
| ip | string | 是 | 访问ip地址（protocol为NET1400时无意义） |
| port | string | 是 | 访问端口号（protocol为NET1400时无意义） |
| username | string | 否 | 用户名 |
| vendor | int | 是 | 厂商ID |
| update_time | long | 否 | 通道的最后修改时间 |
| device_id | string | 是 | 1400IN设备的设备ID（protocol为NET1400时有效） |
| authentication | Int | 是 | 1400IN设备的鉴权方式：1：无需认证，2：基本认证，3：摘要认证 |
| channel_capability | object | 是 | 通道能力 |

channel_capability定义：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| supported_algorithms | string array | 支持的算法集：face_detect, face_verify, target_detect, target_verify, region_enter, region_exit, region_invade, cross_border, passline_count, ebike_enter_elevator, off_duty_detect, fire_exit_occupied, safety_helmet, wear_mask, custom_algoXXX |

#### 4.5.6 获取通道封面

- **功能**：获取通道封面
- **地址**：`http://${ip}:${port}/edgeboxapi/camera/cover/get`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| record_id | int | 是 | 通道id |

**返回参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| Image | string | 原始的Jpg图片通过base64加密后的数据 |
| video_resolution | string | Jpg图片的宽度和高度，示例：1920x1080 |

---

### 4.6 实时视频

> 系统中实时视频流仅支持基于WebSocket协议，仅支持私有格式非加密的视频流传输。

#### 4.6.1 打开视频流

- **功能**：根据请求打开视频流
- **地址**：`http://${IP}:${PORT}/edgeboxapi/real_video/open`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| task_id | string | 是 | 视频流的uuid，由请求方填写，不能重复 |
| channel_id | int | 是 | 通道号，范围1~最大值 |
| stream_index | int | 是 | 码流索引，范围1-3，目前盒子固定为1 |
| audio | bool | 是 | 是否带音频，true带音频，false不带 |

**返回参数**：无

#### 4.6.2 关闭视频流

- **功能**：根据请求关闭视频流
- **地址**：`http://${IP}:${PORT}/edgeboxapi/real_video/close`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| task_id | string | 是 | 视频流的uuid，与打开视频流提供的一致 |

**返回参数**：无

---

### 4.7 智能任务管理

智能任务类型：

| 智能任务类型 | 取值 | 说明 |
|-------------|------|------|
| 人脸抓拍 | face_detect | 人脸抓拍算法 |
| 抓拍识别 | face_verify_capture | 抓拍图识别算法 |
| 视频识别 | face_verify_video | 人脸抓拍与识别算法 |
| 目标检测(抓拍) | target_detect | 目标抓拍，不包含人脸 |
| 目标识别 | target_verify | 目标识别，不包含人脸（预留） |
| 进入区域 | region_enter | 进入区域 |
| 离开区域 | region_exit | 离开区域 |
| 区域入侵 | region_invade | 区域入侵 |
| 越界侦测 | cross_border | 越界侦测 |
| 过线统计 | passline_count | 过线统计 |
| 电瓶车进电梯 | ebike_enter_elevator | 检测到电瓶车进电梯 |
| 离岗检测 | off_duty_detect | 离岗检测 |
| 消防通道占用 | fire_exit_occupied | 消防通道被占用 |
| 未戴安全帽检测 | safety_helmet | 未戴安全帽的人检测 |
| 未戴口罩检测 | wear_mask | 未戴口罩的人检测 |
| 自定义算法 | custom_algoXXX | 自定义算法占位，实际名称根据开放给客户的配置文件决定 |

#### 4.7.1 智能任务列表查询

- **功能**：查询智能任务
- **地址**：`http://${ip}:${port}/edgeboxapi/IntelliTask/list`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| disposition_id | int | 是 | -1获取所有智能任务，否则获取某个具体智能任务 |
| disposition_type | string | 否 | 智能任务类型：face_detect, face_verify_capture, face_verify_video, target_detect, target_verify, region_enter, region_exit, region_invade, cross_border, passline_count, ebike_enter_elevator, off_duty_detect, fire_exit_occupied, safety_helmet, wear_mask, custom_algoXXX |
| channel_id | int | 否 | 通道号，范围1~最大值 |

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| dispositions | object array | 智能任务列表 |

单个智能任务格式：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| disposition_id | int | 是 | 智能任务ID |
| name | string | 是 | 智能任务名字 |
| create_time | int | 是 | 创建时间戳 |
| update_time | int | 是 | 更新时间戳 |
| create_by | string | 是 | 创建者 |
| channel_id | int | 是 | 通道号 |
| channel_name | string | 是 | 通道名称 |
| disposition_type | string | 是 | 智能任务类型 |
| worker_data | object | 是 | 智能任务工作参数 |
| logic_data | object | 是 | 智能任务逻辑数据 |
| status | string | 否 | 智能任务状态（AI_resource_not_enough, decode_resource_not_enough, resource_not_enough） |
| meta | object | 否 | 业务自定义 |

**worker_data定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_detect | object | 否 | 人脸侦测算法参数（含wear_mask未戴口罩检测参数） |
| event_detect | object | 否 | 事件侦测类算法参数 |

**face_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| sensibility | int | 否 | 灵敏度，1-5：较高、高、中、低、较低 |
| policy | object | 是 | 抓拍策略 |
| pitch | int | 否 | 瞳距，单位为像素 |
| region | object | 否 | 抓拍区域 |
| region_min | object | 否 | 抓拍区域限制的最小值 |
| region_max | object | 否 | 抓拍区域限制的最大值 |

**face_detect -> policy定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| mode | string | 是 | 抓拍模式：间隔抓拍或离开时抓拍 |
| multiplayer | bool | 否 | 人员模式：true多人，false单人 |
| prior | string | 否 | 优先原则：质量优先、距离优先 |
| fash | bool | 否 | 首次快速抓拍 |
| gap | int | 否 | 抓拍间隔（秒） |
| number | int | 否 | 抓拍数量 |

**face_detect -> region定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| lefttop_x | int | 是 | 矩形区域左上角横坐标 |
| lefttop_y | int | 是 | 矩形区域左上角纵坐标 |
| rightbottom_x | int | 是 | 矩形区域右下角横坐标 |
| rightbottom_y | int | 是 | 矩形区域右下角纵坐标 |

**event_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| region_event_detect | object | 否 | 区域事件类算法参数 |
| line_event_detect | object | 否 | 过线事件类算法参数 |

**region_event_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| enabled | bool | 是 | 是否开启本类算法 |
| event_detect_para | object array | 是 | 参数列表 |

**event_detect_para定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | int | 是 | 区域标识，1~n |
| region_coordinate | object | 是 | 侦测区域的坐标 |
| block_region | object array | 否 | 屏蔽区域列表 |
| detect_target | object array | 是 | 侦测的目标及参数列表 |
| variant_para | object | 否 | 区域类不同业务的差异参数 |
| custom_para | object | 否 | 自定义算法参数 |

**region_coordinate定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| point | object array | 是 | 区域多边形的顶点坐标X,Y列表 |

**point定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| x | int | 是 | X坐标 |
| y | int | 是 | Y坐标 |

**block_region定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | int | 是 | 屏蔽区域的标识，1~n |
| region_coordinate | object | 是 | 屏蔽区域的坐标 |

**detect_target定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| target_type | int | 是 | 目标类型：1:人脸 2:人体 3:机动车 4:车牌 5:非机动车 6:电瓶车 7:未戴安全帽的人 8:未戴口罩的人 9:火焰 10:物品 |
| enabled | bool | 是 | 是否使能 |
| max_target_size | object | 否 | 最大目标尺寸 |
| min_target_size | object | 否 | 最小目标尺寸 |
| min_duration | Int | 否 | 最小持续时间(秒) |
| sensitivity | Int | 否 | 算法检测的灵敏度 |

**min_target_size / max_target_size定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| x | int | 是 | 左上角x坐标 |
| y | int | 是 | 左上角y坐标 |
| width | Int | 是 | 宽度 |
| height | Int | 是 | 高度 |

**variant_para定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| post_personnel_info | object array | 否 | 离岗检测差异参数，侦测岗位对应的人员信息 |

**post_personnel_info定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| name | string | 否 | 姓名 |
| phone_number | string | 否 | 电话号码 |

**custom_para定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| custom_algo_para | string | 否 | 自定义算法扩展参数 |

**line_event_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| enabled | bool | 是 | 是否开启本类算法 |
| event_detect_para | object array | 是 | 参数列表 |

**line_event_detect -> event_detect_para定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | int | 是 | 线标识，1~n |
| line_coordinate | object | 是 | 线的坐标 |
| direction | string | 是 | 过线方向：left_right, right_left, any_direction |
| block_region | object array | 否 | 屏蔽区域列表 |
| detect_target | object array | 是 | 侦测的目标及参数列表 |
| custom_para | object | 否 | 自定义算法参数 |

**line_coordinate定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| start_point | object | 是 | 线的起点X,Y坐标 |
| end_point | object | 是 | 线的终点X,Y坐标 |

**start_point / end_point定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| x | int | X坐标 |
| y | int | Y坐标 |

**logic_data定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face | object | 否 | 人脸比对/识别类逻辑参数配置 |
| event_detect | object | 否 | 事件侦测类逻辑参数配置 |

**logic_data -> face定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| enabled | bool | 是 | 是否开启该条规则 |
| role_repo | string array | 否 | 布控作用的人像库列表（默认全库） |
| similarity | int | 是 | 比对阈值 |
| global_actions | object | 是 | 全局行为 |
| rule | object array | 是 | 具体规则-行为对 |

**global_actions定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| alarm | object | 是 | 告警行为 |

**alarm定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| upload | object | 是 | 是否上传汇聚平台 |

**upload定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| is_upload | bool | 是 | 是否上传汇聚平台 |
| upload_id | int | 是 | 汇聚平台id |

**rule定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| enabled | bool | 是 | 是否开启该条规则 |
| type | string | 是 | 规则的类型：stranger/white/black |
| conds | object | 是 | 布控规则 |
| actions | object | 是 | 布控行为 |

**conds -> in_repo定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| role_repo | int array | 布控作用的人像库列表 |

**logic_data -> event_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| service_para | object | 否 | 业务参数配置 |
| alarm | object | 否 | 告警联动配置 |

**service_para定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| schedule | object | 否 | 排程配置 |
| data_statistics | object | 否 | 统计数据相关参数 |

**schedule定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| schedule_type | string | 是 | 排程类型：weekly |
| schedule_para | object array | 是 | 排程详情 |

**schedule_para定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| schedule_day | string | 是 | 排程日：Mon/Tue/Wed/Thu/Fri/Sat/Sun |
| schedule_time_slot | string array | 是 | 排程时间段。格式：HH:MM-HH:MM |

**data_statistics定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| cycle_type | string | 是 | 统计周期：always(一直累加), daily(按天统计) |
| data_reset_time | string | 否 | 统计数据清零时间点，格式HH:MM:NN |

**event_detect -> alarm定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| upload | object | 否 | 是否上传汇聚平台 |
| audio_alarm | object | 否 | 联动声音告警 |
| mail_alarm | object | 否 | 联动发邮件告警 |
| popup_screen_alarm | object | 否 | 联动弹出画面告警 |
| alarm_out | object | 否 | 联动告警输出 |

**alarm_out定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| is_alarm_out | bool | 是 | 是否联动告警输出 |
| alarm_out_port | string array | 是 | 需要联动的告警输出端口名称列表 |

**meta定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| mutex_relationship | object | 否 | 互斥关系 |

**mutex_relationship定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| mutex_type | string | 是 | 互斥类型：all_others_mutex, all_others_not_mutex, only_parts_mutex, only_parts_not_mutex, all_mutex_relationship |
| mutex_details | object array | 否 | 互斥关系详情 |

**mutex_details定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| mutex_disposition_type | string | 是 | 智能任务类型 |
| mutex_flag | bool | 是 | 互斥标志位：true互斥，false不互斥 |

#### 4.7.2 更新智能任务

- **功能**：更新智能任务
- **地址**：`http://${ip}:${port}/edgeboxapi/IntelliTask/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| dispositions | object array | 智能任务列表 |

单个智能任务格式：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| disposition_id | int | 是 | 智能任务ID |
| name | string | 否 | 智能任务名字 |
| update_time | int | 否 | 更新时间戳 |
| disposition_type | string | 是 | 智能任务类型 |
| worker_data | object | 是 | 智能任务工作参数 |
| logic_data | object | 是 | 智能任务逻辑数据 |
| meta | object | 否 | 业务自定义 |

> worker_data、logic_data格式定义请参考智能任务列表查询。

**meta -> mutex_handle定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| mutex_disposition_type | string | 是 | 智能任务类型 |
| handle_strategy | string | 是 | 处理策略：fail（新任务失败），force（强制开启新任务） |

**返回参数**：无

#### 4.7.3 智能操作

- **功能**：智能业务操作
- **地址**：`http://${IP}:${PORT}/edgeboxapi/IntelliTask/operation`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| disposition_id | int | 是 | 智能任务ID |
| disposition_type | string | 是 | 智能任务类型 |
| operation_code | string | 是 | 操作码：clear_current_statistics（清除当前统计周期内的计数数据） |

**返回参数**：无

---

### 4.8 系统规格和资源查询

#### 4.8.1 系统规格查询

- **功能**：查询系统规格
- **地址**：`http://${ip}:${port}/edgeboxapi/system/specification/get`
- **方法**：POST

**请求参数**：无

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| channel | object | 通道接入能力 |
| decode | object | 解码能力 |
| ai_engine | object | AI引擎能力 |
| repo | object | 人像库 |
| net | object | 网络 |
| client | object | 客户端访问 |
| user | object | 用户 |

**channel定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| video_channel | int | 视频通道接入总量，不包含图片通道，为0表示不支持 |
| capture_channel | int | 抓拍通道接入总量，不包含视频通道，为0表示不支持 |

**decode定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| total | int | 解码总能力，多少个1080P |
| engine | int | 解码引擎数量 |

**ai_engine定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| total | int | 引擎总数 |
| face_detect_count | int | 画面中支持侦测的最大人脸数量 |
| recognition_cycle | int | 比对数量的规格以多长时间为周期进行计算，单位为秒 |
| recognition_count | int | 在一个周期内的最大比对数量 |
| detail | object list | 引擎详细描述 |

**detail定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| face_detect | bool | 人脸侦测，true表示支持，false表示不支持 |
| face_recognition | bool | 人脸比对，true表示支持，false表示不支持 |

#### 4.8.2 系统能力和能力参数集查询

- **功能**：查询系统能力和能力参数集
- **地址**：`http://${ip}:${port}/edgeboxapi/system/feature_set/get`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| channel | bool | 否 | 通道参数集 |
| ai_algorithms | bool | 否 | AI算法能力和参数集 |
| repo | bool | 否 | 人像库图片参数集 |
| system | bool | 否 | 系统参数集 |

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| channel | object | 否 | 通道参数集 |
| ai_algorithms | object | 否 | AI算法能力和参数集 |
| repo | object | 否 | 人像库图片参数集 |
| system | object | 否 | 系统参数集 |

**channel参数集**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| stream_type | int array | 支持的摄像机类型：1:视频流通道, 2:抓拍机通道 |
| protocol | string array | 支持的摄像机协议集：rtsp、1400 |
| verdor | int array | 厂商 |

**ai_algorithms参数集**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| algorithms | string array | 是 | 支持的AI算法集：face_detect, face_verity, target_detect, target_verify, region_enter, region_exit, region_invade, cross_border, passline_count, ebike_enter_elevator, off_duty_detect, fire_exit_occupied, safety_helmet, wear_mask, custom_algoXXX |
| target_attribute | object array | 是 | 支持的目标属性集 |
| capture_mode | string array | 否 | 支持的抓拍模式参数集 |
| priority | string array | 否 | 支持抓拍的优先模式参数集 |
| sensitivity | int array | 否 | 灵敏度参数集 |
| capture_number | int array | 否 | 总的抓拍张数 |
| capture_gap | int array | 否 | 抓拍间隔范围 |
| pd | int array | 否 | 瞳距参数集 |

**target_attribute参数集**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| target_type | int | 是 | 目标类型：1:人脸 2:人体 3:机动车 4:车牌 5:非机动车 6:电瓶车 7:未戴安全帽的人 8:未戴口罩的人 9:火焰 10:物品 |
| attribute_set | string array | 是 | 支持的属性集 |
| similarity_range | object | 否 | 相似度阈值设置范围 |

**人脸属性全集**：age, gender, glasses, mask, sunglass

**人体属性全集**：ped_age, ped_gender, ped_glasses, ped_mask, ped_hat, ped_backpack_type, ped_trolley_case, ped_clothes_type, ped_clothes_color

**车辆属性全集**：vehicle_brand, vehicle_type, vehicle_color

**非机动车属性全集**：nonmotor_type, nonmotor_color

**system参数集**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| region | int array | 否 | 支持的地区集合 |
| language | string array | 否 | 支持的语言集合 |
| ntp_time_server | string array | 否 | 支持的时间服务器集合 |
| log_originate | int array | 否 | 日志来源参数集 |
| log_category | string array | 否 | 日志类别参数集 |
| user_pwd_expire_time | int array | 否 | 用户密码过期时间参数集 |
| user_pwd_strong | int array | 否 | 用户密码强度参数集 |

#### 4.8.3 系统资源利用率查询

- **功能**：查询系统资源使用率，如CPU、内存、磁盘等
- **地址**：`http://${ip}:${port}/edgeboxapi/system/resource_used/get`
- **方法**：POST

**请求参数**：无

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| cpu | object | CPU使用情况 |
| ram | object | 内存使用情况 |
| time | object | 系统运行时间 |
| disk | object | 磁盘使用状态 |
| channel | object | 通道接入情况 |
| decode | object | 解码能力情况 |
| ai_engine | object | AI引擎能力情况 |
| repo | object | 图像库的情况 |

**cpu定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| total | int | CPU总量 |
| idle | int | 空闲CPU数量 |
| usage_rate | double | CPU使用率，取值0-100 |

**ram定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| total | int | 内存总量 |
| free | int | 剩余内存数量 |
| usage_rate | double | 内存使用率，取值0-100 |

**time定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| now | int | 当前时间时间戳 |
| begin | int | 系统开启时间戳 |
| duration | int | 安全运行时间，单位s |

**disk定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| total | int | 磁盘总量 |
| used | int | 使用磁盘数量 |
| usage_rate | double | 磁盘使用率，取值0-100 |

**channel(资源)定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| video_channel | int | 视频通道接入总量 |
| video_channel_used | int | 视频通道接入使用数量 |
| capture_channel | int | 抓拍通道接入总量 |
| capture_channel_used | int | 抓拍通道接入使用数量 |

**decode(资源)定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| decoding | int | 解码总能力，单位为1080P |
| decoding_used | int | 解码能力使用数量 |
| engine | int | 引擎总数 |
| engine_used | int | 引擎使用数量 |

**ai_engine(资源)**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| total | int | 总量 |
| used | int | 使用数量 |

**repo(资源)**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| total | int | 总量 |
| used | int | 使用数量 |
| usage_rate | double | 使用率，取值0-100 |

---

### 4.9 日志查询

#### 4.9.1 日志查询

- **功能**：查询系统的日志数据
- **地址**：`http://${ip}:${port}/edgeboxapi/log/search`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| current | int | 是 | 当前查询记录的offset |
| pageSize | int | 是 | 一页显示的总条数，上限50，默认值10 |
| end | long | 是 | 查询时间上限，UTC绝对时间的秒数，无上限传0 |
| start | long | 是 | 查询时间下限，UTC绝对时间的秒数，无下限传0 |
| isAscend | bool | 是 | 是否正序，按记录创建时间排序 |
| originate | list array | 是 | 来源：操作记录、告警事件、异常事件，查全部传空值 |
| category | list array | 是 | 类别：操作记录的类型、告警事件的类型、异常事件类型的列表，查全部传空值 |

**originate映射表**：

| 编号 | 对应内容 | 说明 |
|------|----------|------|
| （具体编号映射见原始文档） | | |

**category映射表**：

| 编号 | 对应内容 | 说明 |
|------|----------|------|
| （具体编号映射见原始文档） | | |

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| list | object array | 日志列表 |
| pagination | object | 分页参数 |

单条日志格式：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| originate | int | 是 | 来源 |
| category | int | 是 | 类别 |
| account | string | 否 | 操作者 |
| ip | string | 否 | 操作者ip |
| role | int | 否 | 操作者角色 |
| comment | string | 是 | 操作内容 |
| created | long | 是 | 创建时间戳 |

pagination定义：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| current | int | 当前页 |
| pageSize | int | 页大小 |
| total | int | 总行数 |

---

### 4.10 系统设置与维护

#### 4.10.1 网络配置查询

- **功能**：查询网络配置
- **地址**：`http://${ip}:${port}/edgeboxapi/system_general/network/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| eth0 | object | 是 | 网卡IP |
| eth1 | object | 是 | 网卡IP |
| default_route | string | 是 | 默认路由 |
| request_from_eth | string | 否 | 发起请求网口的名称（eth0/eth1） |
| port | int | 是 | HTTP端口号 |
| gat1400in_port | int | 是 | GAT 1400 IN的监听端口 |

**eth格式定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| ip_assign | Int | 是 | IP分配模式，0为手动，1为自动 |
| ip | string | 是 | 网卡IP |
| netmask | string | 是 | 子网掩码 |
| gateway | string | 否 | 网关IP |
| status | string | 否 | 网口状态：UP/DOWN |
| mac_addr | string | 否 | 网卡物理地址 |
| dns_assign | Int | 是 | DNS分配模式，0为手动，1为自动 |
| first | string | 否 | 首选DNS |
| second | string | 否 | 备选DNS |

#### 4.10.2 网络配置更新

- **功能**：修改网络配置
- **地址**：`[地址待确认]`
- **方法**：POST

**请求参数**：与网络配置查询返回参数一致（含request_from_eth）

**返回参数**：无

#### 4.10.3 密码安全查询

- **功能**：查询密码安全参数
- **地址**：`[地址待确认]/cryptosecurity/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| level | int | 是 | 密码等级，0开始，依次表示从高到低，默认为中 |
| expires | int | 是 | 过期时间单位为天，0表示永不过期，默认为0 |

#### 4.10.4 密码安全更新

- **功能**：更新密码安全参数
- **地址**：`[地址待确认]/cryptosecurity/update`
- **方法**：POST

**请求参数**：与密码安全查询返回参数一致

**返回参数**：无

#### 4.10.5 黑白名单查询（1.0.0版本不支持）

- **功能**：查询系统黑白名单信息
- **地址**：`http://${ip}:${port}/edgeboxapi/system_general/black_white/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| enabled | bool | 是 | 访问控制开关 |
| mode | int | 是 | 控制模式 |
| records | object array | 是 | 名单记录的集合 |

单个record格式：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| id | int | 是 | 记录的序号 |
| enabled | bool | 是 | 记录的开关 |
| start | string | 否 | 范围的开始IP/MAC（与value不能同时使用） |
| end | string | 否 | 范围的结束IP/MAC（与value不能同时使用） |
| value | string | 否 | IP/MAC的值（与start/end不能同时使用） |

#### 4.10.6 黑白名单更新（1.0.0版本不支持）

- **功能**：更新系统黑白名单信息
- **地址**：`http://${ip}:${port}/edgeboxapi/system_general/black_white/update`
- **方法**：POST

**请求参数**：与黑白名单查询返回参数一致

**返回参数**：无

#### 4.10.7 固件/文件上传请求

- **功能**：固件或文件开始上传的请求
- **地址**：`http://${ip}:${port}/edgeboxapi/file/upload`
- **方法**：POST
- **传输协议**：WebSocket

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | int | 是 | 消息ID号，取值范围0~4294967295，当前设备中唯一 |
| size | int | 是 | 文件大小，单位：字节 |
| file_id | string | 是 | 文件id，唯一的文件标识 |
| param | object | 否 | 文件参数 |
| meta | object | 否 | 业务扩展 |

param格式定义：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| sign | string | 是 | 文件签名（固件签名） |
| signMethod | string | 是 | 签名方法：SHA256、Md5 |
| version | string | 否 | 版本号 |

**返回参数**：无

#### 4.10.8 固件/文件下载请求

- **功能**：固件或文件开始下载的请求
- **地址**：`http://${ip}:${port}/edgeboxapi/file/download`
- **方法**：POST
- **传输协议**：WebSocket

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | string | 是 | 消息ID号，String类型的数字，取值范围0~4294967295 |
| file_id | string | 是 | 文件id，唯一的文件标识 |
| param | object | 否 | 文件参数 |

param格式定义：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| sign | string | 是 | 文件签名 |
| signMethod | string | 是 | 签名方法：SHA256、Md5 |
| version | string | 否 | 版本号 |
| file_head | string | 是 | 文件头信息，base64编码后的数据 |

**返回参数**：无

#### 4.10.9 固件/文件传送进度推送

- **功能**：固件或文件上传和下载过程中的进度信息推送
- **地址**：`http://${ip}:${port}/edgeboxapi/file/step`
- **方法**：POST
- **传输协议**：WebSocket

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | string | 是 | 消息ID号 |
| step | int | 是 | 传送进度：[1,100]百分比；-1：升级失败；-2：下载失败；-3：校验失败 |
| desc | string | 否 | 当前步骤的描述信息（异常时承载错误信息） |
| module | string | 否 | 错误所属的模块 |

**返回参数**：无

#### 4.10.10 系统重启

- **功能**：设备重启
- **地址**：`http://${ip}:${port}/edgeboxapi/system/reboot`
- **方法**：POST
- **请求参数**：无
- **返回参数**：无

#### 4.10.11 恢复出厂设置

- **功能**：恢复设备配置至出厂状态
- **地址**：`http://${ip}:${port}/edgeboxapi/device/restore`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| net | bool | 否 | 网络相关配置参数 |
| user | bool | 否 | 用户相关配置参数 |
| log | bool | 否 | 日志数据，默认false |
| cameras | bool | 否 | 摄像机列表 |
| ai_event | bool | 否 | 智能事件（依赖摄像机列表） |
| other | bool | 否 | 其他相关配置参数 |
| face_lib | bool | 否 | 人脸库 |
| app_data_partition | bool | 否 | flash上存储抓拍和比对数据的分区 |
| password | String | 是 | 超级管理员密码，对它的MD5值使用登录时返回的encrypt_key明文加密后的密文 |
| encrypy_type | string | 是 | 加密类型 |

> **备注**：恢复出厂设置需要独立鉴权。

**返回参数**：无

#### 4.10.12 系统配置备份

复用"固件/文件下载请求"和"固件/文件传送进度推送"协议。

#### 4.10.13 系统配置还原

复用"固件/文件上传请求"和"固件/文件传送进度推送"协议。

#### 4.10.14 系统自动维护设置

- **功能**：更新系统自动维护参数信息
- **地址**：`http://${ip}:${port}/edgeboxapi/system/auto_maintain/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| enabled | bool | 是 | 开关，true启用，false禁用 |
| cycle | int | 否 | 维护周期，单位为天 |
| time | int | 否 | 时间戳，单位为分钟 |

**返回参数**：无

#### 4.10.15 系统自动维护查询

- **功能**：查询系统自动维护参数
- **地址**：`http://${ip}:${port}/edgeboxapi/system/auto_maintain/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| enabled | bool | 是 | 开关 |
| cycle | int | 是 | 维护周期，单位为天 |
| time | int | 是 | 24小时内的分钟数 |
| next_maintain_local_date | string | 是 | 下次维护日期，设备本地的日期 |

#### 4.10.16 系统基本信息查询

- **功能**：查询系统设备信息
- **地址**：`[地址待确认]`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| get_local_time | bool | 否 | 获取设备本地当前时间 |

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| region | int | 否 | 地区 |
| language | string | 否 | 语言 |
| time_zone | int | 否 | 时区 |
| date_format | string | 否 | 日期格式 |
| time_format | string | 否 | 时间格式 |
| sync_time_mode | int | 否 | 1为手动同步，2为自动同步 |
| sync_time_server | String | 否 | 同步时间服务器 |
| timestamp | long | 否 | 时间戳，当前时间，单位为秒 |
| guide | bool | 否 | 是否启用开机向导 |

#### 4.10.17 系统基本信息更新

- **功能**：更新系统设备信息
- **地址**：`[地址待确认]/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| region | int | 否 | 地区 |
| language | string | 否 | 语言 |
| time_zone | int | 否 | 时区 |
| date_format | string | 否 | 日期格式 |
| time_format | string | 否 | 时间格式 |
| sync_time_mode | int | 否 | 同步模式 |
| sync_time_server | String | 否 | 同步时间服务器 |
| timestamp | long | 否 | 时间戳，设定时间，单位为秒 |
| guide | bool | 否 | 是否启用开机向导 |

**返回参数**：无

#### 4.10.18 关于本机查询

- **功能**：查询系统版本信息
- **地址**：`http://${ip}:${port}/edgeboxapi/system/about/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| device_specs | object array | 否 | 设备规格特性 |
| software_specs | object array | 否 | 软件规格特性 |
| safe_centre | object array | 否 | 系统安全中心信息 |

单个规格信息格式：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | int | 是 | 记录id |
| name | string | 否 | 规格名称 |
| attribute | string | 否 | 规格属性 |

**设备规格名称映射表**：

| 类别 | ID | 名称 | 对应内容 |
|------|----|------|----------|
| 设备 | 1 | name | 设备名称 |
| 设备 | 10 | id | 设备ID，有效范围1-9999 |
| 设备 | 20 | product_mode | 产品型号（预留） |
| 设备 | 30 | product_id | 产品标识 |
| 设备 | 40 | hardware_code | 硬件平台编码 |
| 设备 | 50 | disk | 磁盘 |
| 设备 | 60 | memory | 内存大小 |
| 设备 | 70 | ai | AI引擎总数 |

**版本规格名称映射表**：

| 类别 | ID | 名称 | 对应内容 |
|------|----|------|----------|
| 版本规格 | 200 | kernal_version | 内核版本号 |
| 版本规格 | 210 | uboot_version | uboot版本号 |
| 版本规格 | 220 | firmware_version | 固件版本号 |
| 版本规格 | 230 | firmware_build_date | 固件安装日期 |
| 版本规格 | 240 | api_version | API版本号 |
| 版本规格 | 250 | ai_version | AI算法库版本号 |
| 版本规格 | 260 | version_detail | 版本详情 |
| 版本规格 | 270 | hardware_version | 硬件版本号 |
| 版本规格 | 280 | MCU_version | 单片机版本号 |

**安全中心信息**：

| 类别 | ID | 名称 | 对应内容 |
|------|----|------|----------|
| 安全中心信息 | 200 | licenses_state | 授权状态（文件授权/固件授权—已授权/未授权） |

#### 4.10.19 关于本机更新

- **功能**：更新系统版本信息
- **地址**：`http://${ip}:${port}/edgeboxapi/system/about/update`
- **方法**：POST

**请求参数**：与关于本机查询返回参数一致

**返回参数**：无

#### 4.10.20 系统授权配置查询

- **功能**：查询系统授权配置
- **地址**：`[地址待确认]/authorize/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| server_addr | string | 否 | 服务器地址 |
| port | Int | 否 | 服务器端口 |
| username | String | 否 | 用户名 |

#### 4.10.21 系统授权配置设置

- **功能**：设置系统授权配置
- **地址**：`[地址待确认]/authorize/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| server_addr | string | 否 | 服务器地址 |
| port | int | 否 | 服务器端口 |
| username | string | 否 | 用户名 |
| password | string | 否 | 密码，对它的值使用登录时返回的encrypt_key明文加密后的密文 |
| encrypy_type | string |  | 加密类型 |

**返回参数**：无

---

### 4.11 上传中心管理

上传中心类型：

| 上传中心类型 | type 取值(int) |
|-------------|----------------|
| 通用上传 | 1 |
| GAT1400 OUT上传 | 2 |

#### 4.11.1 上传地址创建

- **功能**：创建上传中心，目前仅支持一个
- **地址**：`http://${ip}:${port}/edgeboxapi/upload_center/create`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| type | int | 是 | 上传中心类型 |
| name | string | 是 | 上传中心名称 |
| address | string | 是 | 上传中心URL地址 |
| user_name | string | 否 | 用户名 |
| password | string | 否 | 密码 |
| uploadType | string | 是 | 上传中心类型为2(1400OUT)时有效。取值：image, video, file, face, ped, vehicle, nonmotor_vehicle |
| heartbeat_interval | Int | 否 | 心跳发送间隔，单位秒。默认值：10 |
| device_id | string | 是 | EdgeBox的设备ID |
| authentication | Int | 是 | 1400 OUT的鉴权方式：1：无需认证，2：基本认证，3：摘要认证 |

**返回参数**：无

#### 4.11.2 "通用"类型上传中心数据格式

当前选中上传中心类型为"通用"时，盒子会以POST方法向 `http://${IP}:${PORT}/edgeboxapi/upload_center/data` 推送JSON数据：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| box_id | string | 是 | 设备ID，作为盒子唯一标识 |
| box_name | string | 是 | 可能为空字符串 |
| timestamp | int | 是 | UTC时间戳，单位为秒 |
| task_id | string | 否 | 任务ID |
| capture | object | 否 | 抓拍数据 |
| verify | object | 否 | 比对数据 |
| event_list | string array | 否 | 事件结果，空表示无信息 |
| extra | object | 否 | 扩展信息 |

> **注意**：抓拍和告警数据使用相同格式。有alarm_list是告警数据，否则是抓拍数据。同一box通过snapshot_info->face_id关联抓拍和告警数据。

**capture格式定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| target_id | string | 是 | 抓拍记录ID |
| target_type | int | 是 | 目标类型：1:人脸 2:人体/人形 3:机动车 4:车牌 5:非机动车 6:电瓶车 7:未戴安全帽的人 8:未戴口罩的人 9:火焰 10:物品 |
| channel | object | 是 | 产生抓拍的通道信息 |
| target_image | object | 是 | 目标抠图 |
| scene_image | object | 否 | 场景图 |
| quality_score | number(float64) | 否 | 图片质量评分 |
| attributes | array(object) | 否 | 目标结构化信息 |
| target_feature | object | 否 | 目标特征（保留） |
| target_rect | object | 否 | 目标矩形框 |

**capture -> channel定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| id | string | 是 | 通道ID |
| channel_name | string | 是 | 通道名称 |
| channel_type | int | 是 | 视频通道/抓拍通道 |

**capture -> target_image / scene_image定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| type | string | 是 | 图片类型 |
| content | string | 是 | base64编码后的图片数据 |

**capture -> attributes定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| key | string | 是 | 字段名，属性名称 |
| value | string | 是 | 字段值，属性值 |
| confidence | int | 否 | 置信度，实际小数位的数值乘以100 |

**capture -> target_rect定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| x | int | 是 | 左上角x坐标 |
| y | int | 是 | 左上角y坐标 |
| width | int | 是 | 区域宽度 |
| height | int | 是 | 区域高度 |

**verify格式定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| result | string | 是 | 比对结果：success/fail/unqualified（无法提取特征的图片） |
| verify_info | object | 否 | 比对信息 |

**verify -> verify_info定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| person_id | string | 是 | 被比中人员唯一ID |
| repository_id | string | 是 | 被比中的人像库ID |
| similarity | int | 是 | 相似度，实际小数位的数值乘以100 |
| name | string | 是 | 被比中人员姓名 |
| identity | string | 否 | 证件号 |
| repository_image | object | 否 | 样本图 |
| meta | object | 否 | 业务自定义 |

**结构化信息表**（attributes中可能出现的字段）：

| 字段名称 | 字段含义 | 取值 | 取值含义 |
|----------|----------|------|----------|
| age | 年龄 | kid, kid_youth, youth, youth_adult, adult, adult_old, old | 儿童/青少年/青年/中青年/中年/中老年/老年 |
| glasses | 是否戴眼镜 | yes, no | |
| sunglass | 是否戴墨镜 | yes, no | |
| hat | 是否戴帽子 | yes, no | |
| gender | 性别 | female, male | |

**extra定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| event_detect_info | object | 否 | 事件侦测信息 |

**event_detect_info定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| event_type | string | 是 | 事件类型 |
| event_status | string | 否 | 事件状态：event_trigger（事件发生），event_recover（事件恢复） |
| event_rule | object | 否 | 事件对应的具体规则信息 |

**event_rule定义**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| region_event_detect | object array | 否 | 区域类事件算法参数 |
| line_event_detect | object array | 否 | 过线类事件算法参数 |

**region_event_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | number | 否 | 区域标识 |
| region_coordinate | object | 否 | 侦测区域的坐标 |
| variant_para | object | 否 | 区域类不同业务的差异参数 |

**line_event_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | number | 是 | 线标识 |
| line_coordinate | object | 是 | 线的坐标 |
| direction | string | 是 | 过线方向：left_right, right_left, any_direction |

#### 4.11.3 上传地址查询

- **功能**：查询上传地址
- **地址**：`[地址待确认]/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| upload_center | object list | 是 | 上传中心 |

upload_center格式：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | int | 是 | 上传地址ID |
| type | int | 是 | 上传中心类型 |
| name | string | 是 | 上传中心名称 |
| address | string | 是 | 上传中心URL地址 |
| user_name | string | 否 | 用户名 |
| password | string | 否 | 密码 |
| status | string | 否 | 状态：success/fail |
| device_id | string | 是 | EdgeBox的设备ID |
| authentication | Int | 是 | 1400 OUT的鉴权方式 |
| enabled | bool | 是 | 上传中心是否激活，默认"是" |

#### 4.11.4 修改上传地址

- **功能**：编辑上传地址信息
- **地址**：`[地址待确认]/update`
- **方法**：POST

**请求参数**：与上传地址查询返回参数一致

**返回参数**：无

#### 4.11.5 删除上传地址

- **功能**：删除上传地址
- **地址**：`http://${ip}:${port}/edgeboxapi/upload_center/delete`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | int | 是 | 上传地址ID |

**返回参数**：无

#### 4.11.6 上传数据配置查询

- **功能**：查询上传数据配置
- **地址**：`http://${ip}:${port}/edgeboxapi/upload_center/data_upload/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| capture | object | 是 | 抓拍数据 |
| verify | object | 是 | 比对数据 |

capture格式定义：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| capture_data | bool | 是 | 抓拍图与信息 |
| scene_image | bool | 是 | 场景图 |

verify格式定义：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| capture_data | bool | 是 | 抓拍数据（抓拍图和抓拍信息） |
| scene_image | bool | 是 | 场景图 |
| verify_data | bool | 是 | 比对数据（比对图和比对信息） |
| unqualified_capture_data | bool | 是 | 不合格抓拍数据 |

#### 4.11.7 上传数据配置更新

- **功能**：更新上传数据配置
- **地址**：`http://${ip}:${port}/edgeboxapi/upload_center/data_upload/update`
- **方法**：POST

**请求参数**：与上传数据配置查询返回参数一致

**返回参数**：无

---

### 4.12 库与人员管理

#### 4.12.1 查询库列表

- **功能**：查询当前系统中所有库
- **地址**：`http://${ip}:${port}/edgeboxapi/repositories/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| repositories | object array | 是 | 库列表 |

单个人像库格式：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| repository_id | int | 是 | 库ID |
| name | string | 是 | 库名 |
| create_time | long | 是 | 创建时间 |
| update_time | long | 是 | 更新时间 |
| number | int | 是 | 包含数量 |
| meta | object | 是 | 创建时业务自定义数据 |
| expire_time | int | 否 | 有效期天数 |
| expire_timestamp | long | 否 | 过期时间 |
| repo_capacity | long | 否 | 库容量 |
| type | int | 是 | 库类型，1：人像库 |
| delete_flag | bool | 是 | 删除标记：true可删除，false不可删除 |
| update_id | string | 是 | 人像库修改ID，每次变更时更新 |

#### 4.12.2 创建库

- **功能**：新建人像库
- **地址**：`http://${ip}:${port}/edgeboxapi/repositories/create`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| name | string | 是 | 库名，不能重复 |
| meta | object | 否 | 业务自定义 |
| type | int | 是 | 库类型，1为人员库 |
| expire_time | int | 否 | 剩余天数，0为已失效 |
| repo_capacity | int | 否 | 库容量 |

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| repository_id | int | 是 | 库ID |

#### 4.12.3 删除库

- **功能**：删除人像库
- **地址**：`http://${ip}:${port}/edgeboxapi/repositories/delete`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| repository_id | int | 是 | 库ID |

**返回参数**：无

> **备注**：此接口只能删除空的人像库。

#### 4.12.4 更新库

- **功能**：更新库
- **地址**：`http://${ip}:${port}/edgeboxapi/repositories/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| repository_id | int | 是 | 库ID |
| name | string | 是 | 库名，不能重复 |
| meta | object | 否 | 业务自定义 |
| expire_time | int | 否 | 剩余天数，0为已失效 |

**返回参数**：无

#### 4.12.5 人像录入

- **功能**：向库中添加一个人
- **地址**：`http://${ip}:${port}/edgeboxapi/face_images/add`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| repository_id | int | 是 | 人像库ID |
| face_image_content | string | 是 | 照片内容，base64编码，支持jpg格式 |
| name | string | 是 | 姓名 |
| certificate_id | string | 否 | 证件号码 |
| face_id | string | 是 | 人像ID，全局唯一 |
| gender | int | 是 | 性别：1：男；2：女；-1：未知 |
| meta | object | 否 | 业务自定义数据 |
| remarks | string | 否 | 备注信息 |

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_id | string | 是 | 人像ID |
| face_image_path | string | 是 | 人像位置 |

#### 4.12.6 人像删除

- **功能**：删除人像库中一个或多个人员
- **地址**：`http://${ip}:${port}/edgeboxapi/face_images/delete`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_ids | string array | 是 | 人像ID |

**返回参数**：无

#### 4.12.7 删除库中的所有人像

- **功能**：清空人像库中所有人员
- **地址**：`http://${ip}:${port}/edgeboxapi/face_images/clear`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| repository_id | int | 是 | 库ID |

**返回参数**：无

#### 4.12.8 人像与信息批量查询

- **功能**：一次查询多个人像信息
- **地址**：`[地址待确认]/by_id/list`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_ids | string array | 是 | 人脸ID，最大支持同时200个id |
| face_info_type | int | 否 | 返回信息类型：0=全部，1=仅update_id |

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_infos | object array | 否 | 人员/人像信息列表 |

单个人像数据格式：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| repository_id | int | 是 | 人像库ID |
| face_id | string | 是 | 人像ID |
| face_image_path | string | 是 | 人像图片位置 |
| timestamp | int | 是 | 创建时间戳（UTC秒） |
| name | string | 是 | 姓名 |
| certificate_id | string | 否 | 证件号码 |
| gender | int | 是 | 性别：0：女；1：男；-1：未知 |
| meta | object | 否 | 业务自定义数据 |
| face_info_update_id | string | 是 | 人像信息修改ID |
| face_image_update_id | string | 是 | 人像图ID |

#### 4.12.9 人像更新

- **功能**：更新人像库中人像信息
- **地址**：`http://${ip}:${port}/edgeboxapi/face_images/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_id | int | 是 | 人像ID |
| repository_id | int | 否 | 人像库ID |
| face_image_content | string | 否 | 照片内容，base64编码 |
| name | string | 否 | 姓名 |
| certificate_id | string | 否 | 证件号码 |
| gender | int | 否 | 性别：1：男；2：女；-1：未知 |
| meta | object | 否 | 业务自定义数据 |
| remarks | string | 否 | 备注信息 |

**返回参数**：无

#### 4.12.10 分库查询人像

- **功能**：查询人像库中人像
- **地址**：`http://${ip}:${port}/edgeboxapi/face_images/by_keyword/list`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| repository_id | int | 否 | 人像库id，空查全部库 |
| offset | int | 否 | 起始数字 |
| size | int | 是 | 查询条数 |
| keyword | string | 否 | 搜索关键字 |
| order_by_id | int | 否 | 排序：1：asc，2：desc |

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| data | object array | 人像列表 |
| pagination | object | 分页参数 |

pagination定义：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| offset | int | 起始值 |
| size | int | 页大小 |
| total | int | 总行数 |

#### 4.12.11 批量导入人像信息

- **功能**：通过上传zip压缩包来创建人像库，系统同时只能有一个批量导图任务
- 复用"固件/文件上传请求"协议

meta格式定义：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| repository_id | int | 是 | 人像库id |
| file_name | string | 是 | 上传文件名 |

**压缩包规范**：
- zip格式
- 小于500M
- 单张图片命名格式：`姓名_性别_证件号`（不按此格式则图片名当姓名，其他按默认值录入）
- 图片格式支持jpg

> **备注**：通过WebSocket导入zip文件前，需先通过HTTP协议告知设备zip文件所属的人像库组。

#### 4.12.12 全库或部分导出

- **功能**：导出整个库或库中的部分
- **地址**：`http://${ip}:${port}/edgeboxapi/repositories/export`
- **传输协议**：WebSocket
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| repository_id | int | 是 | 库ID |
| face_ids | string array | 否 | 需要导出的人脸ID列表 |

**返回参数**：无，正常直接返回zip文件

#### 4.12.13 查询当前是否有批量导图任务

- **功能**：查询当前批量导图任务ID
- **地址**：`http://${ip}:${port}/edgeboxapi/face_images/upload_task`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data | string | 当前正在运行的任务ID，没有则返回"-1" |

#### 4.12.14 查询批量导图的记录

- **功能**：查询所有批量上传记录
- **地址**：`http://${ip}:${port}/edgeboxapi/face_images/upload_res`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| list | object array | 否 | 上传信息数据列表 |

单条上传信息格式：

| 参数名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| upload_id | string | 是 | 上传任务uuid |
| file_name | string | 是 | 上传文件名 |
| status | int | 是 | 任务状态：0:完成 1:处理中 2:失败（解压失败） |
| total_file_num | int | 是 | 总文件数 |
| file_size | long | 是 | 文件大小，单位为字节 |
| process_num | double | 是 | 当前上传进度 |
| excuted_num | int | 是 | 已执行文件数 |
| success_num | int | 是 | 已成功文件数 |
| upload_time | int | 是 | 上传时间戳（UTC秒） |
| repository_id | int | 否 | 库id |
| repo_name | string | 是 | 库名称 |

#### 4.12.15 查询单个批量导图结果详情

- **功能**：查询指定批量导图任务的结果详情
- **地址**：`http://${ip}:${port}/vbox/v1/face_images/upload_detail`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| upload_id | string | 是 | 上传任务id |

**返回参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| list | object array | 失败记录（最多100条） |

单条失败记录格式：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | int | 序号 |
| upload_id | string | 上传任务的uuid |
| failed_file_name | string | 文件名 |
| failed_reason | int | 错误原因码：1:解压失败 2:文件类型错误 3:抽特征失败 4:单个文件大小超出限制 5:超出库容量 6:信息重复 |

---

### 4.13 图片服务

#### 4.13.1 图片流订阅

- **功能**：根据订阅参数返回图片流
- **地址**：`http://${IP}:${PORT}/edgeboxapi/real_image/subscribe`
- **方法**：POST
- **传输协议**：WebSocket

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_detect | object | 否 | 人脸侦测图片流订阅参数 |
| face_verify | object | 否 | 人脸比对图片流订阅参数 |
| event_detect | object array | 否 | 事件侦测类图片流订阅参数 |

**face_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| info | bool | 是 | 人脸抓拍信息 |
| face | bool | 否 | 人脸抓拍图 |
| scene | bool | 否 | 人脸抓拍场景图 |

**face_verify定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| info | bool | 否 | 人脸比对信息 |
| face | bool | 否 | 人脸抓拍图 |
| scene | bool | 否 | 人脸抓拍场景图 |
| repo | bool | 否 | 人像库图 |

**event_detect定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| event_type | string | 是 | 事件类型 |
| info | bool | 否 | 事件信息 |
| event_rule | bool | 否 | 目标矩形框及事件触碰的规则信息 |
| detect_pic | bool | 否 | 事件抓拍小图 |
| scene_pic | bool | 否 | 事件抓拍场景图 |

**返回参数**：无

#### 4.13.2 查询订阅

- **功能**：返回订阅结果
- **地址**：`[地址待确认]`
- **传输协议**：WebSocket
- **方法**：POST
- **请求参数**：无

**返回参数**：与图片流订阅请求参数相同

#### 4.13.3 取消图片流订阅

- **功能**：根据参数取消图片流的订阅
- **地址**：`http://${IP}:${PORT}/edgeboxapi/real_image/unsubscribe`
- **方法**：POST
- **传输协议**：WebSocket

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_detect | bool | 否 | 人脸侦测图片流订阅标识 |
| face_verify | bool | 否 | 人脸比对图片流订阅标识 |
| event_detect | object array | 否 | 事件侦测类图片流订阅标识 |

event_detect定义：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| event_type | string | 是 | 事件类型 |
| unsubscribe | bool | 是 | 是否取消订阅：true取消，false不取消 |

**返回参数**：无

#### 4.13.4 抓拍记录单张查询

- **功能**：单张抓拍详情查询
- **地址**：`http://${ip}:${port}/edgeboxapi/image/detail/by_key/get`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| timestamp | int | 是 | 查询的时间戳 |
| channel_id | string | 是 | 通道id |
| face_id | int | 是 | 人脸ID |

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_id | int | 是 | 人脸ID |
| face_image | String | 是 | 人脸抓拍图，Base64编码 |
| scene_image | string | 是 | 场景图，Base64编码 |
| timestamp | int | 是 | 抓拍时间戳 |
| channel_id | int | 是 | 通道id |
| meta | object | 是 | 业务自定义数据 |

meta定义：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| age | int | 否 | 年龄 |
| gender | int | 否 | 性别 |
| glasses | int | 否 | 是否戴眼镜 |
| mask | int | 否 | 是否戴口罩 |
| sunglass | int | 否 | 是否戴墨镜 |
| quality_score | double |  | 图片质量分数 |
| temperature | double |  | 测温温度 |

**结构化信息取值映射**：

**age取值**：

| 取值 | 中文 | int |
|------|------|-----|
| kid | 小孩 | 0 |
| kid_youth | 青少年 | 1 |
| youth | 青年 | 2 |
| youth_adult | 中青年 | 3 |
| adult | 中年 | 4 |
| adult_old | 中老年 | 5 |
| old | 老年 | 6 |
| unknown | 未知 | -1 |

**glasses/sunglass/mask取值**：

| 取值 | 中文 | int |
|------|------|-----|
| yes | 是 | 1 |
| no | 否 | 0 |
| unknown | 未知 | -1 |

**gender取值**：

| 取值 | 说明 | int |
|------|------|-----|
| male | 男 | 1 |
| female | 女 | 0 |
| unknown | 未知 | -1 |

**ped_age**: 同 age 取值

**ped_gender**: 同 gender 取值

**ped_glasses/ped_mask/ped_hat**: 同 glasses 取值 (yes:1, no:0, unknown:-1)

**ped_backpack_type**：

| 取值 | 说明 | int |
|------|------|-----|
| no | 没有背包 | 0 |
| backpack | 背包 | 1 |
| shoulderbag | 单肩包 | 2 |
| unknown | 未知 | -1 |

**ped_trolley_case**：

| 取值 | 说明 | int |
|------|------|-----|
| unknown | 未知 | 0 |
| yes | 提拉杆箱 | 1 |
| no | 未提拉杆箱 | 2 |

**ped_clothes_type**：

| 取值 | 说明 | int |
|------|------|-----|
| unknown | 未知 | 0 |
| yes | 穿裙子 | 1 |
| no | 不穿裙子 | 2 |

**颜色取值（ped_clothes_color, vehicle_color, nonmotor_color）**：

| 取值 | 说明 | int |
|------|------|-----|
| unknown | 未知 | 0 |
| others | 其它 | 1 |
| red | 红色 | 2 |
| orange | 橙色 | 3 |
| yellow | 黄色 | 4 |
| green | 绿色 | 5 |
| cyan | 青色 | 6 |
| blue | 蓝色 | 7 |
| purple | 紫色 | 8 |
| black | 黑色 | 9 |
| white | 白色 | 10 |
| gray | 灰色 | 11 |
| pink | 粉色 | 12 |
| brown | 棕色 | 13 |
| golden | 金色 | 14 |
| silver | 银色 | 15 |

**vehicle_type**：

| 取值 | 说明 | int |
|------|------|-----|
| unknown | 未知 | 0 |
| others | 其它 | 1 |
| car | 轿车 | 2 |
| suv | SUV | 3 |
| mpv | MPV | 4 |
| sports_car | 跑车 | 5 |
| van | 面包车 | 6 |
| big_bus | 大巴车 | 7 |
| school_bus | 校车 | 8 |
| bus | 公交车 | 9 |
| taxi | 出租车 | 10 |
| light_bus | 轻客车 | 11 |
| pickup_truck | 皮卡 | 12 |
| truck | 货车 | 13 |
| special_car | 专用车 | 14 |
| fire_engine | 消防车 | 15 |
| ambulance | 救护车 | 16 |
| police_car | 警车 | 17 |
| dump_truck | 泥头车 | 18 |
| water_car | 洒水车 | 19 |

**vehicle_brand**（部分列举）：

| 取值 | 品牌 | int |
|------|------|-----|
| audi | 奥迪 | 6 |
| bmw | 宝马 | 10 |
| byd | 比亚迪 | 12 |
| benz | 奔驰 | 16 |
| buick | 别克 | 19 |
| ferrari | 法拉利 | 40 |
| ford | 福特 | 42 |
| honda | 本田 | 53 |
| hyundai | 现代 | 55 |
| lamborghini | 兰博基尼 | 71 |
| maserati | 玛莎拉蒂 | 84 |
| porsche | 保时捷 | 97 |
| tesla | 特斯拉 | 114 |
| toyota | 丰田 | 112 |
| volkswagen | 大众 | 117 |

> 完整品牌列表包含128个品牌，详见原始文档。

**nonmotor_type**：

| 取值 | 说明 | int |
|------|------|-----|
| unknown | 未知 | 0 |
| bike | 自行车 | 1 |
| ebike | 电瓶车 | 2 |
| motor | 摩托车 | 3 |
| tricycle | 三轮车 | 4 |
| wheelchair | 轮椅 | 5 |
| buggy | 婴儿车 | 6 |
| others | 其它 | 7 |

#### 4.13.5 抓拍记录批量查询

- **功能**：查询抓拍记录列表
- **地址**：`http://${ip}:${port}/edgeboxapi/image/list`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| offset | int | 是 | 读取起始量，从头设置为0 |
| size | int | 是 | 读取数量 |
| channel_ids | string | 否 | 摄像头列表，用逗号隔开 |
| start_time | int | 是 | 起始时间 |
| end_time | int | 是 | 终止时间 |
| event_rule | bool | 否 | 搜索结果中是否携带抓拍目标的矩形框坐标及规则信息 |
| target_attribute | object array | 否 | 目标和属性 |
| by_event | object | 否 | 事件类型 |
| by_name | object | 否 | 名称（比对底图的人员信息名称） |

**by_event定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| event_array | string array | 是 | 事件类型列表：face_detect, face_verify, face_white, face_black, face_stranger, target_detect, target_verify, region_enter, region_exit, region_invade, cross_border, passline_count, ebike_enter_elevator, off_duty_detect, fire_exit_occupied, safety_helmet, wear_mask, custom_algoXXX |

**target_attribute定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| target_type | int | 是 | 目标类型：1:人脸 2:人体 3:机动车 4:车牌 5:非机动车 6:电瓶车 7:未戴安全帽的人 8:未戴口罩的人 9:火焰 10:物品 |
| attribute | object | 是 | 目标对应的属性取值 |

**attribute定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| common_attribute | object array | 否 | 一般枚举类属性值集合 |

**common_attribute定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| name | string | 是 | 属性名称（参考4.13.4属性全集） |
| value | Int array | 是 | 属性取值集 |

**返回参数**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| data | object array | 数据列表 |
| pagination | object | 分页参数 |

单张抓拍目标定义：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| target_id | int | 是 | 目标ID |
| target_type | int | 是 | 目标类型（1-10） |
| attribute | object | 否 | 目标属性信息 |
| timestamp | int | 是 | 时间戳（秒） |
| channel_id | string | 是 | 通道ID |
| meta | object | 否 | 业务自定义数据 |
| target_image_path | string | 是 | 目标图的位置信息 |
| scene_image_path | string | 是 | 场景图的位置信息 |
| compare_info | object | 否 | 比对信息 |
| repo_image_path | string | 否 | 人像库图片的位置信息 |
| target_rect | object | 否 | 目标矩形框 |
| event_type | string | 是 | 事件类型 |
| event_info | object | 否 | 事件信息 |

**target_rect定义**：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| x | int | 左上角x坐标 |
| y | int | 左上角y坐标 |
| width | int | 区域宽度 |
| height | int | 区域高度 |

**event_info定义**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| event_status | string | 是 | 事件状态：event_trigger, event_recover |
| event_rule | object | 否 | 事件对应的具体规则信息 |

event_rule中region_event_detect / line_event_detect定义与4.11.2节extra中的对应结构一致。

#### 4.13.6 抓拍图片统计查询

- **功能**：抓拍图片统计查询
- **地址**：`http://${IP}:${PORT}/edgeboxapi/real_image/capture_statistics/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| today | int | 是 | 今日人脸抓拍总数 |
| recent_24hour | int | 是 | 近24小时人脸抓拍总数 |
| recent_7d_total | int | 是 | 近7日人脸抓拍总数 |
| recent_7d | object list | 是 | 近7日按天统计数量 |

recent_7d格式：

| 参数名 | 类型 | 描述 |
|--------|------|------|
| date | string | 日期 |
| total | int | 日期抓拍图总数 |

#### 4.13.7 实时图片比对

- **功能**：实时图片比对
- **地址**：`http://${IP}:${PORT}/edgeboxapi/real_image/verify`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| face_id | string | 是 | 人脸ID |
| face_image | String | 是 | 人脸抓拍图，Base64编码 |
| type | string | 是 | 图片类型 |
| repository_id | int array | 否 | 比对库ID数组，空表示全库比对 |
| similarity | int | 是 | 比对阈值，百分制 |
| meta | object | 否 | 业务自定义 |

**返回参数**：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| box_id | string | 是 | 设备ID |
| box_name | string | 是 | 盒子设备的名称 |
| timestamp | int | 是 | UTC时间戳（秒） |
| face_id | string | 是 | 抓拍记录ID |
| verify | object | 否 | 比对数据 |

verify格式定义：

| 字段名 | 类型 | 是否必填 | 说明 |
|--------|------|----------|------|
| result | string | 是 | 比对结果：success/fail/unqualified |
| person_id | string | 否 | 被比中人员唯一ID |
| repository_id | string | 否 | 被比中的人像库ID |
| similarity | int | 否 | 相似度（实际值×100） |
| name | string | 否 | 被比中人员姓名 |
| identity | string | 否 | 证件号 |
| repository_image | string | 否 | 样本图，Base64编码 |
| meta | object | 否 | 业务自定义 |

---

### 4.14 事件与状态

#### 4.14.1 事件与状态通知

- **功能**：发送变化状态的通知
- **地址**：`http://${ip}:${port}/edgeboxapi/event/notify`
- **方法**：POST
- **数据传输协议**：WebSocket
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| name | string | 是 | 事件或状态名称（一级分类） |
| type | string | 是 | 事件或状态类型（二级分类） |
| timestamp | number | 否 | 产生的时间戳（UTC秒） |
| channel | int | 否 | 通道号 |
| value | string | 否 | 当前值 |
| threshold | float | 否 | 阈值 |
| msg | string | 否 | 详情 |

**Name/Type/Value取值说明**：

| 事件/状态 | name取值 | type取值 | Value取值 |
|-----------|----------|----------|-----------|
| 通道上线 | channel | on_off | on |
| 通道离线 | channel | on_off | off |
| 网络状态 | system | net | on/off |
| 上传中心状态 | system | upload_center | on |
| 上传中心状态 | system | upload_center | off |
| 时间 | time | run_time | runtime&currenttime |

#### 4.14.2 状态查询（*）

> **注**：1.0.0 版本不支持。

#### 4.14.3 事件与状态订阅查询

- **功能**：异常和告警事件订阅查询
- **地址**：`[地址待确认]/event_subscribe/get`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| name_list | string list | 否 | 订阅的事件列表 |

#### 4.14.4 事件与状态订阅更新

- **功能**：异常和告警事件订阅更新
- **地址**：`[地址待确认]/event_subscribe/update`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| name_list | string list | 否 | 订阅的事件列表 |

**返回参数**：无

---

### 4.15 流数据推送

#### 4.15.1 实时/历史视频流推送

- **传输协议**：WebSocket
- **应用协议**：私有化
- 详见《WebSocket协议文档》

#### 4.15.2 实时/历史图片推送

- **传输协议**：WebSocket
- **应用协议**：私有化
- 详见《WebSocket协议文档》

#### 4.15.3 其他文件数据流

- **传输协议**：WebSocket
- **应用协议**：私有化，二进制数据
- 详见《WebSocket协议文档》

---

### 4.16 存储设置

#### 4.16.1 获取存储时间

- **功能**：获取存储时间
- **地址**：`http://${ip}:${port}/edgeboxapi/store/update`
- **方法**：POST
- **请求参数**：无

**返回参数**：

| 参数名 | 类型 | 是否必填 | 描述 |
|--------|------|----------|------|
| id | int | 是 | 记录ID |
| capture_save_days | int | 否 | 抓拍存储天数 |
| cover_mode | bool | 否 | 抓拍存储模式（预留暂未启用） |
| log_save_space | int | 否 | 【只读】日志存储空间（字节） |
| log_save_time_range | string | 否 | 【只读】日志存储的时间范围 |
| alarm_save_space | int | 否 | 【只读】报警存储空间（字节） |
| alarm_save_time_range | bool | 否 | 【只读】报警存储的时间范围 |

#### 4.16.2 更新存储时间

- **功能**：更新抓拍、告警和日志存储天数
- **地址**：`http://${ip}:${port}/edgeboxapi/store/get`
- **方法**：POST

**请求参数**：

| 参数名 | 类型 | 必填/可选 | 描述 |
|--------|------|-----------|------|
| id | int | 是 | 记录ID |
| capture_save_days | int | 否 | 抓拍存储天数 |
| cover_mode | bool | 否 | 抓拍存储模式（预留暂未启用） |

**返回参数**：无

---

## 附录I 错误码定义

（待补充 - 原始PDF中错误码定义区域出现在页面页眉中，具体错误码值请参考原始文档）

---

> 本文档根据 [Edge-Box] [1.1.3] 接口文档.pdf 转换生成，原始文档由后端行业产品部提供。
