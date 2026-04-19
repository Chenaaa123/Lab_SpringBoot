# 实验室管理系统 — 后端 API 与测试说明

本文档根据当前代码库中的 Spring MVC 控制器整理，覆盖 **全部 HTTP 接口**（共 **46** 条路由）。默认服务地址：`http://localhost:8080`（见 `application.properties` 中 `server.port`）。

---

## 1. 统一响应格式

所有接口返回 `ResponseMessage<T>`：

| 字段 | 说明 |
|------|------|
| `code` | 成功为 `200`（`HttpStatus.OK`），业务/参数错误为 `400`（`HttpStatus.BAD_REQUEST`） |
| `message` | 成功一般为 `"success"`；失败为具体错误文案 |
| `data` | 成功时承载业务数据；失败时多为 `null` |

**测试要点**：断言 `code` 与 `message`；成功时再校验 `data` 结构。

---

## 2. 全量接口一览

| # | 方法 | 路径 | 说明 |
|---|------|------|------|
| 1 | POST | `/lab/auth/login` | 登录 |
| 2 | POST | `/lab/auth/register` | 注册（学生等，角色须为合法描述） |
| 3 | POST | `/lab/auth/logout` | 退出（当前仅返回成功） |
| 4 | PUT | `/lab/auth/password/reset-by-old-password` | 按账号 + 原密码重置密码 |
| 5 | GET | `/lab/users/profile` | 查询个人信息 |
| 6 | PUT | `/lab/users/profile` | 修改个人信息 |
| 7 | PUT | `/lab/users/password` | 用户修改自己的密码（需原密码） |
| 8 | GET | `/lab/users` | 分页用户列表（管理） |
| 9 | POST | `/lab/users` | 新增用户（管理） |
| 10 | PUT | `/lab/users/{userId}` | 编辑用户（管理） |
| 11 | DELETE | `/lab/users/{userId}` | 删除用户（管理） |
| 12 | PUT | `/lab/users/{userId}/password` | 管理员重置用户密码 |
| 13 | GET | `/lab/labs` | 分页实验室列表（按角色过滤） |
| 14 | GET | `/lab/labs/{id}` | 实验室详情 |
| 15 | GET | `/lab/labs/code/{code}` | 按编号查实验室 |
| 16 | GET | `/lab/labs/manager/{managerId}` | 按管理员用户 ID 查其管理的实验室列表 |
| 17 | POST | `/lab/labs` | 新增实验室 |
| 18 | PUT | `/lab/labs/{id}` | 更新实验室 |
| 19 | DELETE | `/lab/labs/{id}` | 删除实验室 |
| 20 | GET | `/lab/lab-categories` | 分类列表 |
| 21 | GET | `/lab/lab-categories/{id}` | 分类详情 |
| 22 | POST | `/lab/lab-categories` | 新增分类 |
| 23 | PUT | `/lab/lab-categories/{id}` | 编辑分类 |
| 24 | DELETE | `/lab/lab-categories/{id}` | 删除分类 |
| 25 | PUT | `/lab/lab-categories/{id}/manager` | 分配/更换分类管理员 |
| 26 | GET | `/lab/lab-categories/manager/{managerId}/name` | 按管理员用户 ID 查姓名及所辖分类 |
| 27 | GET | `/lab/reservations` | 分页预约列表（按角色过滤） |
| 28 | GET | `/lab/reservations/{id}` | 预约详情 |
| 29 | POST | `/lab/reservations` | 创建预约 |
| 30 | PUT | `/lab/reservations/{id}/audit` | 审核预约 |
| 31 | PUT | `/lab/reservations/{id}/cancel` | 取消预约 |
| 32 | PUT | `/lab/reservations/{id}/finish` | 使用完成 |
| 33 | POST | `/lab/reservations/{id}/repair` | 基于预约提交故障报修 |
| 34 | GET | `/lab/repairs` | 分页报修列表 |
| 35 | GET | `/lab/repairs/{id}` | 报修详情 |
| 36 | POST | `/lab/repairs` | 创建报修单 |
| 37 | GET | `/lab/maintenances` | 分页检修列表 |
| 38 | GET | `/lab/maintenances/{id}` | 检修详情 |
| 39 | POST | `/lab/maintenances` | 创建检修记录 |
| 40 | PUT | `/lab/maintenances/{id}` | 更新检修记录 |
| 41 | PUT | `/lab/maintenances/{id}/status` | 更新检修状态 |
| 42 | GET | `/lab/announcements` | 分页公告列表 |
| 43 | GET | `/lab/announcements/{id}` | 公告详情 |
| 44 | POST | `/lab/announcements` | 发布公告（需请求头权限） |
| 45 | PUT | `/lab/announcements/{id}` | 编辑公告（需请求头权限） |
| 46 | DELETE | `/lab/announcements/{id}` | 删除公告（需请求头权限） |

> **说明**：当前 `AuthController` 中**不存在**验证码类「忘记密码」接口；密码相关仅有「原密码重置」与 `/lab/users` 下的修改密码接口。

---

## 3. 认证 `/lab/auth`

### 3.1 `POST /lab/auth/login`

**Body（JSON，`LoginDto`）**

| 字段 | 必填 | 说明 |
|------|------|------|
| `userAccount` | 是 | 账号 |
| `password` | 是 | 密码（明文比对） |
| `role` | 是 | 须与用户表中 `role` **完全一致**；且须为枚举描述：`系统管理员`、`学生`、`老师`、`实验室管理员` |

**测试用例建议**

- 三字段齐全且正确 → `200`，`data` 为 `UserInfoDto`（含 `userId`、`userAccount`、`userName`、`role`、`avatar`）。
- 缺任一字段 → `400`。
- 角色描述非法 / 与库中不一致 / 密码错误 → `400` 及对应 `message`。

### 3.2 `POST /lab/auth/register`

**Body（JSON，`RegisterDto`）**：字段同登录（`userAccount`、`password`、`role`）。`role` 须能通过 `Role.fromDescription` 校验。

**测试**：账号重复 → `400`「账号已存在」。

### 3.3 `POST /lab/auth/logout`

无请求体。当前实现固定返回成功。

### 3.4 `PUT /lab/auth/password/reset-by-old-password`

**Body（JSON）**

| 字段 | 必填 |
|------|------|
| `userAccount` | 是 |
| `oldPassword` | 是 |
| `newPassword` | 是 |
| `confirmNewPassword` | 是 |

规则：新密码与确认一致；新密码长度 ≥ 6；新密码不得与原密码相同；原密码须正确。

---

## 4. 用户 `/lab/users`

`LabUserController` 对 `http://localhost:5173` 配置了 `@CrossOrigin`（仅该控制器）。

### 4.1 `GET /lab/users/profile?userId={id}`

**测试**：存在 / 不存在用户 ID。

### 4.2 `PUT /lab/users/profile`

**Body**：`userId`（必填）；可选 `userName`、`avatar`。

### 4.3 `PUT /lab/users/password`

**Body**：`userId`、`oldPassword`、`newPassword`（均必填）。

### 4.4 `GET /lab/users`

**Query**：`page`（默认 1）、`size`（默认 10）、`keyword`、`role`。

**分页响应 `data`**：`records`、`total`、`current`（注意为**页码减一后的索引**，与 Spring Data 一致）、`size`、`pages`。

### 4.5 `POST /lab/users`（管理）

**Body**：`userAccount`、`userName`、`role`、`password` 必填；可选 `avatar`。

### 4.6 `PUT /lab/users/{userId}`（管理）

**Body**：可选 `userAccount`、`userName`、`role`、`avatar`。

### 4.7 `DELETE /lab/users/{userId}`

### 4.8 `PUT /lab/users/{userId}/password`（管理）

**Body**：`password` 必填。

---

## 5. 实验室 `/lab/labs`

### 5.1 `GET /lab/labs`

**Query**：`categoryId`、`status`（在 `role` 为 `system_admin` 或未传 `role` 时参与过滤）、`name`（模糊匹配编号或名称）、`role`、`userId`、`page`、`size`。

**角色与 `role` 取值（代码中的 switch）**：`student`、`teacher`、`lab_admin`、`system_admin`。未匹配时默认仅展示 `status=1` 的实验室。

**测试**：学生/教师仅见正常实验室；实验室管理员仅见自己 `manager` 的实验室；系统管理员可见全部并可按 `status` 筛选。

### 5.2 `GET /lab/labs/{id}`、`GET /lab/labs/code/{code}`、`GET /lab/labs/manager/{managerId}`

只读；按路径参数查询。

### 5.3 `POST /lab/labs`、`PUT /lab/labs/{id}`、`DELETE /lab/labs/{id}`

请求体为 `Map`，字段以 `LabController` 内校验为准（如 `code` 必填等）。**测试**需结合前端或 Postman 构造完整创建体（参见控制器内注释示例）。

**实验室 `status` 含义（业务）**：`0` 停用、`1` 正常、`2` 维护中；仅 `1` 可预约。

---

## 6. 实验室分类 `/lab/lab-categories`

### 6.1 `GET /lab/lab-categories`、`GET /lab/lab-categories/{id}`

返回实体 `LabCategory`。

### 6.2 `POST /lab/lab-categories`、`PUT /lab/lab-categories/{id}`

**Body**：`name` 必填；`description` 可选；管理员用户 ID 可传 `managerUserId` 或 `managerId`（同时存在时优先 `managerUserId`）。非法或用户不存在 → `400`。

### 6.3 `DELETE /lab/lab-categories/{id}`

若分类下仍有实验室 → `400`，提示先处理关联。

### 6.4 `PUT /lab/lab-categories/{id}/manager`

**Body**：`managerUserId` 或 `managerId`（至少其一能解析出整数用户 ID）。会级联更新该分类下实验室的 `manager`。

### 6.5 `GET /lab/lab-categories/manager/{managerId}/name`

返回管理员姓名及其管理的分类列表（结构见控制器实现）。

---

## 7. 预约 `/lab/reservations`

### 7.1 状态约定（与测试数据构造相关）

- **预约状态 `status`**：`0` 待审核，`1` 通过，`2` 拒绝，`4` 已取消。  
- **使用状态 `useStatus`**：`0` 待使用，`1` 使用中，`2` 使用完成，`4` 已取消（与拒绝/取消逻辑一致处见代码）。

### 7.2 `GET /lab/reservations`

**Query**：`page`、`size`、`userId`、`role`（`student` / `teacher` / `lab_admin` / `system_admin`）、`labId`、`lab_id`、`labIds`（逗号分隔，多 ID 在非 `lab_admin` 时可用 `in` 查询）、`status`、`keyword`（订单号、用途模糊）。

**测试**：不传 `role`/`userId` 时无角色过滤谓词；传齐后按角色缩小范围。

### 7.3 `GET /lab/reservations/{id}`

### 7.4 `POST /lab/reservations`

**Body**：`userId`、`labId`、`startTime`、`endTime` 必填；`purpose` 可选。时间格式：`yyyy-MM-dd HH:mm:ss`。须同一天且在实验室 `openTime`～`closeTime` 内；实验室须 `status=1`；同一用户、同一实验室时间段不可重叠；实验室维度并发通过悲观锁防重。

**测试**：格式错误、非同一天、超出开放时间、实验室不可用、时间段冲突 → 各 `400`。

### 7.5 `PUT /lab/reservations/{id}/audit`

**Body**：`status` 必填，`1` 通过或 `2` 拒绝；可选 `rejectReason`（拒绝时）；可选 `auditUserId`。仅当当前预约 `status==0`（待审核）可审。

### 7.6 `PUT /lab/reservations/{id}/cancel`

**Body**：可选；若传 `userId` 则校验只能取消自己的预约。允许：`status==0`，或 `status==1` 且当前时间早于 `startTime`。

### 7.7 `PUT /lab/reservations/{id}/finish`

**Body**：可选 `userId`（校验归属）。要求：`status==1`，且当前时间 **不早于** `startTime`；将 `useStatus` 置为 `2`（使用完成）。

### 7.8 `POST /lab/reservations/{id}/repair`

**Body**：`userId` 必填；`title` 必填；`description` 可选；`role` 可选（不传则用该用户库中 `role` 再规范化）。

**前置条件**：预约 `status==1`，且 `useStatus==2` **或** `endTime` 早于当前时间。

**权限**：`system_admin` 任意；`lab_admin` 须为该实验室 `manager`；否则须为预约所属用户。

**角色别名规范化**（节选）：`系统管理员`/`admin`/`systemAdmin` 等 → `system_admin`；`实验室管理员`/`labManager` 等 → `lab_admin`；`学生`→`student`；`教师`→`teacher`；其余原样参与分支（系统管理员分支需值为 `system_admin`）。

---

## 8. 报修 `/lab/repairs`

### 8.1 `GET /lab/repairs`

**Query**：`page`、`size`、`userId`、`role`、`labId`、`lab_id`、`labIds`、`status`（报修单状态：`0` 待处理、`1` 处理中、`2` 已完成、`3` 已关闭）。

### 8.2 `GET /lab/repairs/{id}`

### 8.3 `POST /lab/repairs`

**Body**：`userId`、`labId`、`title` 必填；`description` 可选；`reservationId` 可选（合法则关联预约）。

---

## 9. 检修 `/lab/maintenances`

### 9.1 `GET /lab/maintenances`

**Query**：在报修列表基础上增加 **`reporterUserId`** 或 **`reporterId`**（二者等价，用于按报修人用户 ID 过滤；仅对关联了报修的检修记录生效）。

### 9.2 `GET /lab/maintenances/{id}`

### 9.3 `POST /lab/maintenances`

**Body**：`labId`、`content`、`maintenanceUnit`、`maintenanceTime`（格式 `yyyy-MM-dd HH:mm:ss`，可空则默认当前时间）、`handler`、`handlerPhone` 必填；`repairId` 可选。新建后 `status=0`（处理中）。若关联报修，会同步报修单状态。

### 9.4 `PUT /lab/maintenances/{id}`

部分字段更新；`status` 仅允许 `0` 或 `1`。可更新 `repairId`（传空串可解除关联）。

### 9.5 `PUT /lab/maintenances/{id}/status`

**Body**：`status`，`0` 或 `1`。完成后会同步关联报修状态（检修 `0` → 报修 `1`；检修 `1` → 报修 `2`）。

---

## 10. 公告 `/lab/announcements`

### 10.1 `GET /lab/announcements`

**Query**：`page`、`size`。`data` 含 `records`、`total`、`totalPages`、`currentPage`、`pageSize`、`hasNext`、`hasPrevious`。

### 10.2 `GET /lab/announcements/{id}`

### 10.3 `POST` / `PUT` / `DELETE`（写操作）

**请求头**：`X-Role` 须为 **`Admin`**（不区分大小写）或 **`系统管理员`**，否则 `400`「无权限（需要Admin）」。

**创建 `AnnouncementCreateDto`（JSON 字段）**：`title`、`content` 必填；`status` 可选（默认 `1`）；`publisherId`、`labId` 可选。

**更新 `AnnouncementUpdateDto`**：各字段均可空；传入的非空字段才会更新。

---

## 11. 建议的端到端冒烟顺序（可选）

1. `POST /lab/auth/register` 或管理端 `POST /lab/users` 准备账号。  
2. `POST /lab/auth/login` 取 `userId` 与中文 `role`。  
3. `GET /lab/lab-categories`、`GET /lab/labs`（列表查询带 `role`/`userId` 与前端约定一致）。  
4. `POST /lab/reservations` → `PUT .../audit` → `PUT .../finish` → `POST .../repair` 或 `POST /lab/repairs`。  
5. `POST /lab/maintenances`（带 `repairId`）→ `PUT .../status`。  
6. 公告写操作带 `X-Role: Admin` 测权限边界。

---

## 12. 文档与代码同步说明

- 接口列表以 `src/main/java/com/crud/lab_springboot/controller/` 下 `@RequestMapping` + `@GetMapping` 等映射为准。  
- 若后续新增控制器或修改路径，请同步更新本文件中的「全量接口一览」与各节说明。

---

## 13. 每个 API 测试用例清单（46/46）

> 约定：以下每个接口最少覆盖 1 条成功用例 + 1 条失败用例。  
> 可在 Apifox/Postman 中按「接口编号」直接建同名用例集。

### 13.1 认证模块 `/lab/auth`

#### API-01 `POST /lab/auth/login`
- 成功：账号/密码/角色（中文角色描述）正确，返回 `code=200` 且 `data.userId` 非空。
- 失败：`password` 错误，返回 `code=400`，`message` 包含「账号或密码错误」。

#### API-02 `POST /lab/auth/register`
- 成功：新账号 + 合法角色（如 `学生`）注册成功。
- 失败：重复账号注册，返回 `400`「账号已存在」。

#### API-03 `POST /lab/auth/logout`
- 成功：直接调用返回 `200`。
- 失败（稳健性）：发送任意无关 body，接口仍应返回 `200`（当前实现不使用 body）。

#### API-04 `PUT /lab/auth/password/reset-by-old-password`
- 成功：`userAccount + oldPassword + newPassword + confirmNewPassword` 全部正确，返回 `200`。
- 失败：`newPassword != confirmNewPassword`，返回 `400`「新密码与确认新密码不一致」。

### 13.2 用户模块 `/lab/users`

#### API-05 `GET /lab/users/profile`
- 成功：传存在的 `userId`，返回 `data.userId` 与请求一致。
- 失败：传不存在 `userId`，返回 `400`「用户不存在」。

#### API-06 `PUT /lab/users/profile`
- 成功：传 `userId` + `userName`/`avatar`，更新后再次查询 profile 验证生效。
- 失败：`userId` 缺失，返回 `400`「userId不能为空」。

#### API-07 `PUT /lab/users/password`
- 成功：`oldPassword` 正确且 `newPassword` 非空，返回 `200`。
- 失败：`oldPassword` 错误，返回 `400`「原密码错误」。

#### API-08 `GET /lab/users`
- 成功：`page=1&size=10` 返回分页结构 `records/total/current/size/pages`。
- 失败（参数边界）：`size=0`，接口应自动兜底为最小页大小并返回 `200`（代码有 `Math.max(size,1)`）。

#### API-09 `POST /lab/users`
- 成功：`userAccount/userName/role/password` 全部合法，返回新增用户信息。
- 失败：`userAccount` 重复，返回 `400`「用户账号已存在」。

#### API-10 `PUT /lab/users/{userId}`
- 成功：更新 `userName` 或 `role`，返回 `200` 且字段变化。
- 失败：`userId` 不存在，返回 `400`「用户不存在」。

#### API-11 `DELETE /lab/users/{userId}`
- 成功：删除存在用户，返回 `200`。
- 失败：删除不存在用户，返回 `400`「用户不存在」。

#### API-12 `PUT /lab/users/{userId}/password`
- 成功：传 `password` 非空，返回 `200`。
- 失败：`password` 为空，返回 `400`「password不能为空」。

### 13.3 实验室模块 `/lab/labs`

#### API-13 `GET /lab/labs`
- 成功：`role=student` 时仅返回正常实验室（`status=1`）。
- 失败（业务口径）：`role` 传未知值时，仍应走默认逻辑仅返回 `status=1`（不是报错）。

#### API-14 `GET /lab/labs/{id}`
- 成功：存在 `id` 返回实验室实体。
- 失败：不存在 `id` 返回 `400`「实验室不存在」。

#### API-15 `GET /lab/labs/code/{code}`
- 成功：存在 `code` 返回对应实验室。
- 失败：不存在 `code` 返回 `400`「实验室编号不存在」。

#### API-16 `GET /lab/labs/manager/{managerId}`
- 成功：传管理员 ID，返回该管理员负责的实验室列表。
- 失败（空数据）：管理员存在但无负责实验室，返回 `200` 且列表为空。

#### API-17 `POST /lab/labs`
- 成功：传 `code/categoryId/managerUserId` + 合法时间格式，返回 `200`。
- 失败：缺 `code` 或 `categoryId`，返回 `400` 对应提示。

#### API-18 `PUT /lab/labs/{id}`
- 成功：更新 `openTime/closeTime` 为 `HH:mm:ss`，返回 `200`。
- 失败：`openTime` 非法格式（如 `8:30`），返回 `400`「openTime 格式应为 HH:mm:ss」。

#### API-19 `DELETE /lab/labs/{id}`
- 成功：删除存在实验室返回 `200`。
- 失败：删除不存在实验室返回 `400`「实验室不存在」。

### 13.4 分类模块 `/lab/lab-categories`

#### API-20 `GET /lab/lab-categories`
- 成功：返回分类数组（可为空）。
- 失败（稳健性）：数据库无数据时也应返回 `200` 空数组。

#### API-21 `GET /lab/lab-categories/{id}`
- 成功：存在分类返回实体。
- 失败：不存在分类返回 `400`「分类不存在」。

#### API-22 `POST /lab/lab-categories`
- 成功：`name` + 合法 `managerUserId` 创建成功。
- 失败：`name` 为空或 `managerUserId` 非法，返回 `400`。

#### API-23 `PUT /lab/lab-categories/{id}`
- 成功：更新 `name/description/managerUserId`，返回 `200`。
- 失败：分类不存在返回 `400`「分类不存在」。

#### API-24 `DELETE /lab/lab-categories/{id}`
- 成功：分类下无实验室时删除成功。
- 失败：分类下存在实验室，返回 `400` 且提示先处理关联实验室。

#### API-25 `PUT /lab/lab-categories/{id}/manager`
- 成功：传 `managerUserId` 成功并触发级联。
- 失败：`managerUserId` 缺失或非数字，返回 `400`。

#### API-26 `GET /lab/lab-categories/manager/{managerId}/name`
- 成功：返回 `managerName` 与 `managedCategories`。
- 失败：`managerId` 不存在，返回 `400`「未找到该管理员ID对应的用户信息」。

### 13.5 预约模块 `/lab/reservations`

#### API-27 `GET /lab/reservations`
- 成功：`role=lab_admin&userId=...` 返回其管辖实验室预约记录。
- 失败（参数容错）：`labIds=abc` 时不应 500，应返回 `200`（无效值被忽略）。

#### API-28 `GET /lab/reservations/{id}`
- 成功：存在预约返回详情。
- 失败：不存在预约返回 `400`「预约记录不存在」。

#### API-29 `POST /lab/reservations`
- 成功：`startTime/endTime` 合法且不冲突，返回新预约（`status=0`）。
- 失败：时间冲突或实验室非正常状态，返回 `400`。

#### API-30 `PUT /lab/reservations/{id}/audit`
- 成功：待审核预约传 `status=1`，审核通过后 `useStatus=1`。
- 失败：传 `status=3`，返回 `400`「status只能是1(通过)或2(拒绝)」。

#### API-31 `PUT /lab/reservations/{id}/cancel`
- 成功：待审核预约取消成功，返回 `200`。
- 失败：非本人取消（传了他人 `userId`），返回 `400`「只能取消自己的预约」。

#### API-32 `PUT /lab/reservations/{id}/finish`
- 成功：已通过且到开始时间后，调用成功并将 `useStatus=2`。
- 失败：开始前调用，返回 `400`「尚未到预约开始时间，无法提前结束」。

#### API-33 `POST /lab/reservations/{id}/repair`
- 成功：预约已通过且已完成，`userId + title` 提交成功，返回 `repairId`。
- 失败：未完成就报修，返回 `400`（仅允许使用完成后报修）。

### 13.6 报修模块 `/lab/repairs`

#### API-34 `GET /lab/repairs`
- 成功：按 `status` 过滤返回报修列表。
- 失败（参数容错）：`labIds` 含非法值时不应报错，返回 `200`。

#### API-35 `GET /lab/repairs/{id}`
- 成功：返回报修详情及关联 `user/lab` 信息。
- 失败：不存在 ID 返回 `400`「报修记录不存在」。

#### API-36 `POST /lab/repairs`
- 成功：`userId/labId/title` 必填满足，返回 `200` 且 `status=0`。
- 失败：缺 `title` 返回 `400`「title不能为空」。

### 13.7 检修模块 `/lab/maintenances`

#### API-37 `GET /lab/maintenances`
- 成功：`reporterUserId` 或 `reporterId` 生效，返回目标报修人关联检修记录。
- 失败（参数容错）：`labIds` 非法字符串不应抛异常，返回 `200`。

#### API-38 `GET /lab/maintenances/{id}`
- 成功：返回检修详情。
- 失败：不存在 ID 返回 `400`「检修记录不存在」。

#### API-39 `POST /lab/maintenances`
- 成功：必填项齐全，创建成功并默认 `status=0`。
- 失败：`maintenanceTime` 格式错误，返回 `400`（格式应为 `yyyy-MM-dd HH:mm:ss`）。

#### API-40 `PUT /lab/maintenances/{id}`
- 成功：更新 `content/handler/status`，返回 `200`。
- 失败：`status=2`，返回 `400`「状态值无效，应为0（处理中）或1（已完成）」。

#### API-41 `PUT /lab/maintenances/{id}/status`
- 成功：传 `status=1`，更新成功并同步关联报修状态。
- 失败：不传 `status`，返回 `400`「状态不能为空」。

### 13.8 公告模块 `/lab/announcements`

#### API-42 `GET /lab/announcements`
- 成功：`page/size` 正常返回分页结构。
- 失败（边界）：`size=0` 时仍返回 `200`（自动兜底最小值）。

#### API-43 `GET /lab/announcements/{id}`
- 成功：返回公告详情。
- 失败：不存在 ID 返回 `400`「公告不存在」。

#### API-44 `POST /lab/announcements`
- 成功：请求头 `X-Role: Admin` 且 `title/content` 非空，创建成功。
- 失败：缺少 `X-Role` 或非管理员角色，返回 `400`「无权限（需要Admin）」。

#### API-45 `PUT /lab/announcements/{id}`
- 成功：`X-Role: 系统管理员`，更新 `title/content/status` 成功。
- 失败：公告不存在，返回 `400`「公告不存在」。

#### API-46 `DELETE /lab/announcements/{id}`
- 成功：`X-Role: Admin` 删除成功。
- 失败：`X-Role: student`，返回 `400`「无权限（需要Admin）」。

---

## 14. 回归测试执行建议（按依赖顺序）

1. 先准备基础数据：用户、分类、实验室。  
2. 再跑预约主链路：创建预约 → 审核 → 使用完成。  
3. 然后跑报修/检修联动：预约报修（或直报修）→ 创建检修 → 修改检修状态。  
4. 最后跑公告写权限与删除场景。  

建议在同一环境中固定以下测试账号：`system_admin`、`lab_admin`、`student`、`teacher`，用于复现角色过滤与权限边界。

---

## 15. Postman 请求示例（逐接口可复制）

在 Postman 中新建 **Environment**，建议变量：

| 变量名 | 示例值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost:8080` | 与 `server.port` 一致 |
| `userId` | `1` | 登录后写入 Tests 或手动改 |
| `labId` | `1` | 创建实验室后替换 |
| `categoryId` | `1` | 创建分类后替换 |
| `managerUserId` | `3` | 实验室管理员用户 ID |
| `reservationId` | `1` | 创建预约后替换 |
| `repairId` | `1` | 创建报修后替换 |
| `maintenanceId` | `1` | 创建检修后替换 |
| `announcementId` | `1` | 创建公告后替换 |

除特别说明外：**Headers** 增加 `Content-Type: application/json`（GET 无 Body 时可省略）。

---

### 15.1 认证 `/lab/auth`

**API-01 `POST /lab/auth/login`**

- URL: `{{baseUrl}}/lab/auth/login`
- Body (raw JSON):

```json
{
  "userAccount": "student01",
  "password": "123456",
  "role": "学生"
}
```

> `role` 须与数据库中该用户的 `role` 字段**完全一致**（中文：`系统管理员`、`学生`、`老师`、`实验室管理员`）。

**API-02 `POST /lab/auth/register`**

- URL: `{{baseUrl}}/lab/auth/register`
- Body:

```json
{
  "userAccount": "new_student_001",
  "password": "123456",
  "role": "学生"
}
```

**API-03 `POST /lab/auth/logout`**

- URL: `{{baseUrl}}/lab/auth/logout`
- Body: 无（可选任意，当前不读 body）

**API-04 `PUT /lab/auth/password/reset-by-old-password`**

- URL: `{{baseUrl}}/lab/auth/password/reset-by-old-password`
- Body:

```json
{
  "userAccount": "student01",
  "oldPassword": "123456",
  "newPassword": "654321",
  "confirmNewPassword": "654321"
}
```

---

### 15.2 用户 `/lab/users`

**API-05 `GET /lab/users/profile`**

- URL: `{{baseUrl}}/lab/users/profile?userId={{userId}}`
- Method: `GET`

**API-06 `PUT /lab/users/profile`**

- URL: `{{baseUrl}}/lab/users/profile`
- Body:

```json
{
  "userId": 1,
  "userName": "测试昵称",
  "avatar": "https://example.com/avatar.png"
}
```

**API-07 `PUT /lab/users/password`**

- URL: `{{baseUrl}}/lab/users/password`
- Body:

```json
{
  "userId": 1,
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

**API-08 `GET /lab/users`**

- URL: `{{baseUrl}}/lab/users?page=1&size=10&keyword=&role=`
- Method: `GET`

**API-09 `POST /lab/users`**

- URL: `{{baseUrl}}/lab/users`
- Body:

```json
{
  "userAccount": "lab_mgr_01",
  "userName": "实验室管理员甲",
  "role": "实验室管理员",
  "password": "123456",
  "avatar": ""
}
```

**API-10 `PUT /lab/users/{{userId}}`**

- URL: `{{baseUrl}}/lab/users/1`（将 `1` 换为实际 `userId`）
- Body（字段均可选，按需传）:

```json
{
  "userName": "新名称",
  "role": "老师",
  "avatar": ""
}
```

**API-11 `DELETE /lab/users/{{userId}}`**

- URL: `{{baseUrl}}/lab/users/999`
- Method: `DELETE`

**API-12 `PUT /lab/users/{{userId}}/password`**

- URL: `{{baseUrl}}/lab/users/1/password`
- Body:

```json
{
  "password": "newpass123"
}
```

---

### 15.3 实验室 `/lab/labs`

**API-13 `GET /lab/labs`**

- URL 示例（学生视角）:

`{{baseUrl}}/lab/labs?page=1&size=10&role=student&userId=1`

- 系统管理员带状态筛选:

`{{baseUrl}}/lab/labs?page=1&size=10&role=system_admin&userId=1&status=1`

- 按名称/编号模糊:

`{{baseUrl}}/lab/labs?name=305&role=lab_admin&userId=3&page=1&size=10`

**API-14 `GET /lab/labs/{{labId}}`**

- URL: `{{baseUrl}}/lab/labs/1`

**API-15 `GET /lab/labs/code/{code}`**

- URL: `{{baseUrl}}/lab/labs/code/LAB001`

**API-16 `GET /lab/labs/manager/{managerId}`**

- URL: `{{baseUrl}}/lab/labs/manager/3`

**API-17 `POST /lab/labs`**

- URL: `{{baseUrl}}/lab/labs`
- Body:

```json
{
  "code": "LAB001",
  "name": "计算机实验室A",
  "categoryId": 1,
  "managerUserId": 3,
  "location": "教学楼A-305",
  "equipment": "计算机40台,投影仪1台",
  "openTime": "08:00:00",
  "closeTime": "22:00:00",
  "status": 1,
  "description": "用于教学实验",
  "imageUrl": "http://example.com/lab.jpg"
}
```

> `managerUserId` 与 `managerId` 二选一即可（同时传优先 `managerUserId`）。

**API-18 `PUT /lab/labs/{{labId}}`**

- URL: `{{baseUrl}}/lab/labs/1`
- Body（部分更新示例）:

```json
{
  "status": 1,
  "openTime": "09:00:00",
  "closeTime": "21:00:00",
  "managerUserId": 3
}
```

**API-19 `DELETE /lab/labs/{{labId}}`**

- URL: `{{baseUrl}}/lab/labs/1`
- Method: `DELETE`

---

### 15.4 实验室分类 `/lab/lab-categories`

**API-20 `GET /lab/lab-categories`**

- URL: `{{baseUrl}}/lab/lab-categories`

**API-21 `GET /lab/lab-categories/{{categoryId}}`**

- URL: `{{baseUrl}}/lab/lab-categories/1`

**API-22 `POST /lab/lab-categories`**

- URL: `{{baseUrl}}/lab/lab-categories`
- Body:

```json
{
  "name": "计算机类",
  "description": "计算机相关实验室",
  "managerUserId": 3
}
```

**API-23 `PUT /lab/lab-categories/{{categoryId}}`**

- URL: `{{baseUrl}}/lab/lab-categories/1`
- Body:

```json
{
  "name": "计算机与网络类",
  "description": "更新描述",
  "managerUserId": 3
}
```

**API-24 `DELETE /lab/lab-categories/{{categoryId}}`**

- URL: `{{baseUrl}}/lab/lab-categories/1`
- Method: `DELETE`

**API-25 `PUT /lab/lab-categories/{{categoryId}}/manager`**

- URL: `{{baseUrl}}/lab/lab-categories/1/manager`
- Body:

```json
{
  "managerUserId": 3
}
```

**API-26 `GET /lab/lab-categories/manager/{managerId}/name`**

- URL: `{{baseUrl}}/lab/lab-categories/manager/3/name`

---

### 15.5 预约 `/lab/reservations`

**API-27 `GET /lab/reservations`**

- URL 示例:

`{{baseUrl}}/lab/reservations?page=1&size=10&userId=1&role=student&status=0&keyword=`

- 兼容 `lab_id`、`labIds`:

`{{baseUrl}}/lab/reservations?page=1&size=10&userId=3&role=lab_admin&labIds=1,2`

**API-28 `GET /lab/reservations/{{reservationId}}`**

- URL: `{{baseUrl}}/lab/reservations/1`

**API-29 `POST /lab/reservations`**

- URL: `{{baseUrl}}/lab/reservations`
- Body（时间须 `yyyy-MM-dd HH:mm:ss`，且与实验室开放时间、同一天规则一致）:

```json
{
  "userId": 1,
  "labId": 1,
  "startTime": "2026-04-20 10:00:00",
  "endTime": "2026-04-20 12:00:00",
  "purpose": "课程实验"
}
```

**API-30 `PUT /lab/reservations/{{reservationId}}/audit`**

- URL: `{{baseUrl}}/lab/reservations/1/audit`
- Body（通过）:

```json
{
  "status": 1,
  "auditUserId": 2,
  "rejectReason": ""
}
```

- Body（拒绝）:

```json
{
  "status": 2,
  "auditUserId": 2,
  "rejectReason": "时间段冲突"
}
```

**API-31 `PUT /lab/reservations/{{reservationId}}/cancel`**

- URL: `{{baseUrl}}/lab/reservations/1/cancel`
- Body（建议传，用于校验归属）:

```json
{
  "userId": 1
}
```

**API-32 `PUT /lab/reservations/{{reservationId}}/finish`**

- URL: `{{baseUrl}}/lab/reservations/1/finish`
- Body:

```json
{
  "userId": 1
}
```

**API-33 `POST /lab/reservations/{{reservationId}}/repair`**

- URL: `{{baseUrl}}/lab/reservations/1/repair`
- Body:

```json
{
  "userId": 1,
  "role": "学生",
  "title": "投影仪无信号",
  "description": "HDMI 口松动"
}
```

> 管理员代报时可传 `role: "system_admin"` 或 `lab_admin` 等（与代码中 `normalizeReservationRole` 一致）；不传则按用户表 `role` 推断。

---

### 15.6 报修 `/lab/repairs`

**API-34 `GET /lab/repairs`**

- URL: `{{baseUrl}}/lab/repairs?page=1&size=10&userId=1&role=student&status=0`

**API-35 `GET /lab/repairs/{{repairId}}`**

- URL: `{{baseUrl}}/lab/repairs/1`

**API-36 `POST /lab/repairs`**

- URL: `{{baseUrl}}/lab/repairs`
- Body:

```json
{
  "userId": 1,
  "labId": 1,
  "title": "空调异响",
  "description": "开启后噪音大",
  "reservationId": 1
}
```

> `reservationId` 可选；非法数字会被忽略。

---

### 15.7 检修 `/lab/maintenances`

**API-37 `GET /lab/maintenances`**

- URL: `{{baseUrl}}/lab/maintenances?page=1&size=10&userId=1&role=student&reporterUserId=1`

**API-38 `GET /lab/maintenances/{{maintenanceId}}`**

- URL: `{{baseUrl}}/lab/maintenances/1`

**API-39 `POST /lab/maintenances`**

- URL: `{{baseUrl}}/lab/maintenances`
- Body:

```json
{
  "labId": 1,
  "repairId": 1,
  "content": "更换 HDMI 线缆并紧固接口",
  "maintenanceUnit": "校后勤维修组",
  "maintenanceTime": "2026-04-21 14:00:00",
  "handler": "张三",
  "handlerPhone": "13800138000"
}
```

> `repairId`、`maintenanceTime` 可选；不传 `maintenanceTime` 则默认当前时间。

**API-40 `PUT /lab/maintenances/{{maintenanceId}}`**

- URL: `{{baseUrl}}/lab/maintenances/1`
- Body（按需字段）:

```json
{
  "content": "检修内容更新",
  "status": 1,
  "handlerPhone": "13900139000"
}
```

**API-41 `PUT /lab/maintenances/{{maintenanceId}}/status`**

- URL: `{{baseUrl}}/lab/maintenances/1/status`
- Body:

```json
{
  "status": 1
}
```

---

### 15.8 公告 `/lab/announcements`

**API-42 `GET /lab/announcements`**

- URL: `{{baseUrl}}/lab/announcements?page=1&size=10`

**API-43 `GET /lab/announcements/{{announcementId}}`**

- URL: `{{baseUrl}}/lab/announcements/1`

**API-44 `POST /lab/announcements`**

- URL: `{{baseUrl}}/lab/announcements`
- Headers: `X-Role: Admin`（或 `系统管理员`）
- Body:

```json
{
  "title": "五一实验室开放安排",
  "content": "详见附件……",
  "status": 1,
  "publisherId": 1,
  "labId": 1
}
```

**API-45 `PUT /lab/announcements/{{announcementId}}`**

- URL: `{{baseUrl}}/lab/announcements/1`
- Headers: `X-Role: Admin`
- Body:

```json
{
  "title": "五一安排（修订）",
  "content": "修订说明……",
  "status": 1,
  "publisherId": 1,
  "labId": null
}
```

**API-46 `DELETE /lab/announcements/{{announcementId}}`**

- URL: `{{baseUrl}}/lab/announcements/1`
- Method: `DELETE`
- Headers: `X-Role: Admin`

---

### 15.9 Postman 小技巧

1. 在 **Login** 请求的 **Tests** 里可写：`pm.environment.set("userId", pm.response.json().data.userId);`（若你的响应结构为 `data` 包一层）。  
2. 列表接口里 **`role` 英文取值**（`student` / `teacher` / `lab_admin` / `system_admin`）与登录体里的 **中文角色** 不同，请勿混用同一套字符串。  
3. 创建预约、报修前请确认 **`labId` 对应实验室 `status=1`** 且时间段在 `openTime`～`closeTime` 内。
