# 实验室预约系统后端 API 测试用例

> 说明：所有接口返回统一结构 `ResponseMessage<T>`，格式如下：
> 
> ```json
> {
>   "code": 200,
>   "message": "success",
>   "data": {}
> }
> ```
> 
> - `code = 200` 表示成功
> - `code = 400` 或其他非200值表示失败
> - `data` 为返回的具体数据

---

## 一、认证模块 AuthController（前缀 `/lab/auth`）

### 1.1 登录

- **URL**: `POST /lab/auth/login`
- **说明**: 用户登录（账号 + 密码 + 角色）
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "userAccount": "student001",
  "password": "123456",
  "role": "学生"
}
```

**字段说明**:
- `userAccount`: 用户账号（必填）
- `password`: 密码（必填）
- `role`: 角色（必填，可选值：系统管理员、学生、老师、实验室管理员）

- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "userAccount": "student001",
    "role": "学生",
    "userName": "student001",
    "avatar": null
  }
}
```

- **错误响应**:

```json
{
  "code": 400,
  "message": "账号不能为空",
  "data": null
}
```

---

### 1.2 注册

- **URL**: `POST /lab/auth/register`
- **说明**: 用户注册
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "userAccount": "student002",
  "password": "123456",
  "role": "学生"
}
```

**字段说明**:
- `userAccount`: 用户账号（必填，唯一）
- `password`: 密码（必填）
- `role`: 角色（必填）

- **成功响应**: 同登录接口，返回用户信息
- **错误响应**:

```json
{
  "code": 400,
  "message": "账号已存在",
  "data": null
}
```

---

### 1.3 退出登录

- **URL**: `POST /lab/auth/logout`
- **说明**: 退出登录
- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 二、用户模块 LabUserController（前缀 `/lab/users`）

### 2.1 分页查询用户列表（Admin）

- **URL**: `GET /lab/users`
- **说明**: 分页查询用户列表，支持关键字搜索和角色筛选
- **Query 参数**:
  - `page` (Integer, 默认 `1`) - 页码
  - `size` (Integer, 默认 `10`) - 每页数量
  - `keyword` (String, 可选) - 搜索关键字（匹配用户名/账号）
  - `role` (String, 可选) - 角色筛选

- **示例**:

```text
GET /lab/users?page=1&size=10&keyword=张三&role=实验室管理员
```

- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "userId": 1,
        "userAccount": "admin",
        "userName": "张三",
        "role": "系统管理员",
        "avatar": "http://xxx.jpg"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
}
```

---

### 2.2 新增用户（Admin）

- **URL**: `POST /lab/users`
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "userAccount": "zhangsan",
  "userName": "张三",
  "role": "实验室管理员",
  "password": "12345678",
  "avatar": "http://example.com/avatar.jpg"
}
```

**字段说明**:
- `userAccount`: 用户账号（必填，唯一）
- `userName`: 用户姓名（必填）
- `role`: 角色（必填）
- `password`: 密码（必填）
- `avatar`: 头像URL（可选）

- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 10,
    "userAccount": "zhangsan",
    "userName": "张三",
    "role": "实验室管理员",
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

---

### 2.3 编辑用户（Admin）

- **URL**: `PUT /lab/users/{userId}`
- **说明**: 编辑用户信息，支持修改头像
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "userAccount": "zhangsan",
  "userName": "张三",
  "role": "实验室管理员",
  "avatar": "http://example.com/avatar.jpg"
}
```

**字段说明**:
- `userAccount`: 用户账号（可选，修改时需保证唯一性）
- `userName`: 用户姓名（可选）
- `role`: 角色（可选）
- `avatar`: 头像URL（可选，传空字符串或null可清空头像）

**头像修改示例**:
```json
{
  "avatar": "https://example.com/new-avatar.png"
}
```

- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "userAccount": "zhangsan",
    "userName": "张三",
    "role": "实验室管理员",
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

---

### 2.4 删除用户（Admin）

- **URL**: `DELETE /lab/users/{userId}`
- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 2.5 管理员修改用户密码（Admin）

- **URL**: `PUT /lab/users/{userId}/password`
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "password": "newPassword123"
}
```

---

### 2.6 获取个人信息

- **URL**: `GET /lab/users/profile?userId=1`
- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "userAccount": "student001",
    "role": "学生",
    "userName": "张三",
    "avatar": "https://example.com/avatar.png"
  }
}
```

---

### 2.7 修改个人信息

- **URL**: `PUT /lab/users/profile`
- **请求体**:

```json
{
  "userId": 1,
  "userName": "新昵称",
  "avatar": "https://xxx.com/avatar.png"
}
```

---

### 2.8 修改密码（需要原密码）

- **URL**: `PUT /lab/users/password`
- **请求体**:

```json
{
  "userId": 1,
  "oldPassword": "123456",
  "newPassword": "abcdef"
}
```

---

## 三、实验室模块 LabController（前缀 `/lab/labs`）

### 3.1 查询实验室列表

- **URL**: `GET /lab/labs`
- **Query 参数**:
  - `categoryId` (Long, 可选)
  - `status` (Integer, 可选)
  - `name` (String, 可选)
  - `page` (int, 默认 `1`)
  - `size` (int, 默认 `10`)

- **示例**: `GET /lab/labs?categoryId=1&status=1&page=1&size=10`

---

### 3.2 查询实验室详情

- **URL**: `GET /lab/labs/{id}`
- **路径参数**: `id` (Long)
- **说明**: 响应 JSON 中实验室管理员与分类表一致，字段名为 **`manager_id`**（值为关联的用户对象，对应表列 `lab.manager_id`）。

---

### 3.3 根据编号查询实验室

- **URL**: `GET /lab/labs/code/{code}`
- **路径参数**: `code` (String)

---

### 3.4 根据实验室管理员查询实验室

- **URL**: `GET /lab/labs/manager/{managerId}`
- **路径参数**: `managerId` (Integer)，对应表字段 `lab.manager_id`（用户 `user_id`）

---

### 3.5 新增实验室（Admin）

- **URL**: `POST /lab/labs`
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "code": "LAB001",
  "name": "计算机实验室",
  "categoryId": 1,
  "managerUserId": 3,
  "location": "A-305",
  "equipment": "计算机40台,投影仪1台,空调2台",
  "openTime": "08:00:00",
  "closeTime": "22:00:00",
  "status": 1,
  "description": "描述",
  "imageUrl": "http://example.com/image.jpg"
}
```

**字段说明**:
- `code`: 实验室编号（必填）
- `name`: 实验室名称（可选，可为空字符串）
- `categoryId`: 所属分类ID（必填）
- `managerUserId` / `managerId`: 实验室管理员用户 ID（必填，优先级 `managerUserId` → `managerId`）
- `location`: 位置（可选）
- `equipment`: 设备清单（可选）
- `openTime`: 开放时间（可选，格式HH:mm:ss）
- `closeTime`: 关闭时间（可选，格式HH:mm:ss）
- `status`: 状态（可选，默认1）
- `description`: 描述（可选）
- `imageUrl`: 图片URL（可选）

---

### 3.6 编辑实验室（Admin）

- **URL**: `PUT /lab/labs/{id}`
- **路径参数**: `id` (Long)
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "code": "LAB001",
  "name": "计算机实验室",
  "categoryId": 1,
  "managerUserId": 3,
  "location": "A-305",
  "equipment": "计算机40台,投影仪1台,空调2台",
  "openTime": "08:00:00",
  "closeTime": "22:00:00",
  "status": 1,
  "description": "描述",
  "imageUrl": "http://example.com/image.jpg"
}
```

**字段说明**:
- `code`: 实验室编号（可选）
- `name`: 实验室名称（可选，传空字符串可清空名称）
- `categoryId`: 所属分类ID（可选）
- `managerUserId` / `managerId`: 实验室管理员用户 ID（可选，优先级同上）
- `location`: 位置（可选）
- `equipment`: 设备清单（可选）
- `openTime`: 开放时间（可选，格式HH:mm:ss）
- `closeTime`: 关闭时间（可选，格式HH:mm:ss）
- `status`: 状态（可选）
- `description`: 描述（可选）
- `imageUrl`: 图片URL（可选）

### 3.7 删除实验室（Admin）

- **URL**: `DELETE /lab/labs/{id}`
- **路径参数**: `id` (Long)

---

## 四、实验室分类模块 LabCategoryController（前缀 `/lab/lab-categories`）

### 4.1 查询分类列表

- **URL**: `GET /lab/lab-categories`

---

### 4.2 查询分类详情

- **URL**: `GET /lab/lab-categories/{id}`
- **路径参数**: `id` (Long)

---

### 4.3 新增分类（Admin）

- **URL**: `POST /lab/lab-categories`
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "name": "计算机类",
  "description": "计算机相关实验室分类",
  "managerUserId": 2
}
```

（也可传 `managerId`；与 `managerUserId` 同时存在时优先 `managerUserId`。）

---

### 4.4 编辑分类（Admin）

- **URL**: `PUT /lab/lab-categories/{id}`
- **说明**: 可更新名称、描述，以及**实验室管理员**（数据库字段 `manager_id`，JSON 字段名 `manager_id`，与实验室表一致）。当请求体中传入了**与当前不同的**新管理员时，后端在**同一事务**内通过 **`LabCategoryManagerCascadeService` 级联更新**该分类下所有实验室的 `lab.manager_id`，保证实验室信息模块与分类一致。
- **前端处理**（实验室信息模块）:
  1. 调用本接口或 **4.6** 成功后，**重新请求**实验室列表/详情（如 `GET /lab/labs`、`GET /lab/labs/{id}`），或在前端状态管理中 **invalidate** 实验室相关缓存；不要只更新分类对象而假定实验室已变。
  2. 若列表按 `categoryId` 过滤，可对**该分类 id** 触发一次实验室列表刷新即可。
  3. 请求体仍使用 **`managerUserId` / `managerId`**（优先级见下），与分类其它接口一致。
- **请求体示例**:

```json
{
  "name": "计算机类",
  "description": "描述",
  "managerUserId": 10
}
```

**实验室管理员用户 ID**（任选其一，优先级 **`managerUserId` → `managerId`**；同时传非空时取 `managerUserId`）：
- `managerUserId`
- `managerId`

---

### 4.5 删除分类（Admin）

- **URL**: `DELETE /lab/lab-categories/{id}`

---

### 4.6 分配/更换实验室管理员（Admin）

- **URL**: `PUT /lab/lab-categories/{id}/manager`
- **说明**: 与 4.4 相同，管理员**实际发生变化**时在同一事务内**级联更新**该分类下全部实验室的 `manager_id`；前端处理同 4.4「前端处理」三条。
- **请求体**（以下字段**任选其一**，优先级与 4.4 相同：`managerUserId` → `managerId`）:

```json
{
  "managerUserId": 10
}
```

```json
{
  "managerId": 10
}
```

---

### 4.7 根据实验室管理员ID获取管理员姓名

- **URL**: `GET /lab/lab-categories/manager/{managerId}/name`
- **路径参数**: `managerId` (Integer)

---

## 五、公告模块 AnnouncementController（前缀 `/lab/announcements`）

### 5.1 查询公告列表

- **URL**: `GET /lab/announcements`
- **Query 参数**:
  - `page` (int, 默认 `1`)
  - `size` (int, 默认 `10`)

---

### 5.2 查询公告详情

- **URL**: `GET /lab/announcements/{id}`

---

### 5.3 发布公告（Admin）

- **URL**: `POST /lab/announcements`
- **Headers**:
  - `Content-Type: application/json`
  - `X-Role`: Admin 或 系统管理员
- **请求体**:

```json
{
  "title": "公告标题",
  "content": "公告内容",
  "status": 1,
  "publisherId": 1,
  "labId": null
}
```

---

### 5.4 编辑公告（Admin）

- **URL**: `PUT /lab/announcements/{id}`

---

### 5.5 删除公告（Admin）

- **URL**: `DELETE /lab/announcements/{id}`

---

## 六、预约模块 ReservationController（前缀 `/lab/reservations`）

### 完整业务流程概览

```
用户预约 → 待审核 → [取消] 或 [审核通过] → 使用中
    ↓
使用中 → 用户点击「使用完成」或 时间到 → 实验室空闲
    ↓
使用完成后 → 用户填报修表单 → 报修记录(待处理)
    ↓
系统管理员处理报修 → 创建检修记录 → 检修完成 → 报修记录(已完成)
```

### 预约业务规则与校验

- **实验室状态与可预约性**  
  - 实验室状态：`0`-停用，`1`-正常，`2`-维护中。  
  - **仅当实验室状态为 `1`（正常）时允许预约**；停用或维护中时创建预约会返回错误。

- **时间与开放时间**  
  - 预约开始、结束时间须为**同一天**。  
  - 预约时间段须落在该实验室的 **开放时间（openTime～closeTime）** 内，否则返回错误。

- **同一实验室不允许时间重叠**  
  - 同一实验室在同一时间段内，只能存在一条“有效”预约（状态为 **待审核(0)** 或 **已通过(1)**）。  
  - 新建预约若与已有有效预约时间重叠，会返回 `该时间段该实验室已被预约，请选择其他时间或实验室`。

- **同一用户同一实验室不重复预约**  
  - 同一用户对同一实验室在同一时间段内不能有多条有效预约（待审核或已通过）。  
  - 若已存在则返回 `您在该时间段已预约过该实验室，请勿重复预约`。

- **角色与操作**  
  - **创建预约**：通常由学生/教师发起（传 `userId`）。  
  - **审核预约**：仅管理员/实验室管理员可操作（通过/拒绝）。  
  - **取消预约**：仅可取消状态为「待审核」的预约。

---

### 预约流程说明（供前端参考）

#### 一、创建预约流程（POST /lab/reservations）

后端按以下顺序执行校验，任一步失败即返回 400 及对应 `message`：

| 步骤 | 校验项 | 失败时 message |
|-----|--------|-----------------|
| 1 | 必填参数 | `userId不能为空` / `labId不能为空` / `startTime和endTime不能为空` |
| 2 | 数字格式 | `userId或labId格式不正确` |
| 3 | 用户存在 | `用户不存在` |
| 4 | 实验室存在（加锁） | `实验室不存在` |
| 5 | 时间格式 yyyy-MM-dd HH:mm:ss | `时间格式应为: yyyy-MM-dd HH:mm:ss` |
| 6 | 开始时间 < 结束时间 | `开始时间必须早于结束时间` |
| 7 | 实验室状态 = 1（正常） | `该实验室当前不可预约（停用或维护中）` |
| 8 | 同一天 + 在开放时间内 | `预约开始与结束须为同一天` / `预约时间须在实验室开放时间内（...）` |
| 9 | 该用户该实验室该时段无有效预约 | `您在该时间段已预约过该实验室，请勿重复预约` |
| 10 | 该实验室该时段无其他有效预约 | `该时间段该实验室已被预约，请选择其他时间或实验室` |
| 11 | 通过 | 插入预约，status=0，返回成功 |

**有效预约**：`status` 为 0（待审核）或 1（已通过）的预约视为占用时段；2/3/4 不占用。

#### 二、审核预约流程（PUT /lab/reservations/{id}/audit）

| 步骤 | 校验项 | 失败时 |
|-----|--------|--------|
| 1 | status 必填且为 1 或 2 | `status不能为空` / `status只能是1(通过)或2(拒绝)` |
| 2 | 预约存在 | `预约记录不存在` |
| 3 | 预约 status=0 | `只能审核待审核状态的预约` |
| 4 | 通过 | 更新 status、auditTime、rejectReason（若拒绝），返回成功 |

#### 三、取消预约流程（PUT /lab/reservations/{id}/cancel）

| 步骤 | 校验项 | 失败时 |
|-----|--------|--------|
| 1 | 预约存在 | `预约记录不存在` |
| 2 | 预约为待审核或待使用 | 否则返回 `只能取消待审核或待使用状态的预约` |
| 3 | 通过 | 更新 status=4、useStatus=4，返回成功 |

> 说明：支持在 **待审核** 或 **待使用**（审核通过但未到使用时间段）时取消；传 `userId` 时后端校验只能取消自己的预约。

#### 四、预约状态流转与完整业务流程

```
[创建] → status=0 待审核（useStatus=待审核）
           ├→ 审核通过 → status=1 已通过
           │              ├→ 未到使用时间段 → useStatus=待使用（操作栏可取消预约）
           │              ├→ 到了使用时间段 → useStatus=使用中（操作栏可点击使用完成）
           │              ├→ 用户点击「使用完成」→ useStatus=使用完成（实验室变空闲）
           │              └→ 时间到自动 → useStatus=使用完成（实验室变空闲；后端 overlap 不计入）
           ├→ 审核拒绝 → status=2 已拒绝（useStatus=已取消）
           └→ 用户取消 → status=4 已取消（useStatus=已取消）

使用完成后 → 用户可填报修表单 → 生成报修记录 → 系统管理员处理 → 生成检修记录
```

- **待审核(0)**：可被审核（通过/拒绝）、可取消；useStatus=待审核。
- **已通过(1)**：审核通过（预约状态不再变化）。未到使用时间段时 useStatus=**待使用**（操作栏可取消预约）；到了使用时间段为 **使用中**（操作栏可点击使用完成）；点击使用完成或时间到后为 **使用完成**，实验室释放。
- **已拒绝(2)**、**已取消(4)**：终态，不可再操作（使用状态为已取消）。

#### 五、流程图（Mermaid）

```mermaid
flowchart TD
    subgraph 创建预约
        A[用户提交预约] --> B{参数校验}
        B -->|失败| E1[返回 400 + message]
        B -->|通过| C[对实验室加锁]
        C --> D{用户/实验室存在?}
        D -->|否| E1
        D -->|是| F{时间格式/顺序/开放时间?}
        F -->|否| E1
        F -->|是| G{实验室状态=1?}
        G -->|否| E1
        G -->|是| H{该用户该时段已预约该实验室?}
        H -->|是| E1
        H -->|否| I{该时段实验室有有效预约?}
        I -->|是| E1
        I -->|否| J[插入预约 status=0]
        J --> K[返回 200 + 预约详情]
    end

    subgraph 审核预约
        L[管理员审核] --> M{预约存在且status=0?}
        M -->|否| E2[返回 400]
        M -->|是| N[更新 status=1/2]
        N --> O[返回 200]
    end

    subgraph 取消预约
        P[用户取消] --> Q{预约存在且(待审核或待使用)?}
        Q -->|否| E3[返回 400]
        Q -->|是| R[更新 status=4, useStatus=4]
        R --> S[返回 200]
    end

    subgraph 使用完成
        T[用户点击使用完成] --> U{预约存在且status=1?}
        U -->|否| E4[返回 400]
        U -->|是| V{已到开始时间?}
        V -->|否| E4
        V -->|是| W[更新 useStatus=2]
        W --> X[返回 200，实验室释放]
    end
```

#### 六、前端对接要点

1. **创建前预检**：可调用 `GET /lab/reservations?labId=x` 获取该实验室已有预约，在本地判断所选时段是否与 `status` 0/1 的记录重叠，提前提示用户。
2. **提交防重复**：提交时按钮 loading、禁用确定/取消，避免多次点击。
3. **错误展示**：直接展示后端返回的 `message`，无需二次翻译。
4. **成功后续**：创建成功后关闭弹窗、刷新列表；审核/取消/使用完成成功后刷新列表。
5. **useStatus 展示**：列表中的 `useStatus` 已由后端计算（待审核/待使用/使用中/使用完成/已取消），前端可直接展示。
6. **取消预约**：当 `useStatusCode` 为 0（待审核）或 3（待使用）时，展示「取消预约」按钮，调用 `PUT /lab/reservations/{id}/cancel`。
7. **使用完成**：当 `useStatus` 为「使用中」时，展示「使用完成」按钮，调用 `PUT /lab/reservations/{id}/finish`。
8. **报修入口**：当 `useStatus` 为「使用完成」且预约 `status=1`（已通过）时，可展示「故障报修」入口，调用 `POST /lab/reservations/{id}/repair`。

---

### 6.1 分页查询预约记录

- **URL**: `GET /lab/reservations`
- **说明**: 分页查询预约记录，支持按用户ID、实验室ID、状态筛选，支持关键字搜索
- **Query 参数**:
  - `page` (int, 默认 `1`) - 页码
  - `size` (int, 默认 `10`) - 每页数量
  - `userId` (Integer, 可选) - 用户ID筛选
  - `labId` (Long, 可选) - 实验室ID筛选
  - `status` (Integer, 可选) - 预约状态筛选 (0-待审核, 1-已通过, 2-已拒绝, 4-已取消)
  - `keyword` (String, 可选) - 关键字搜索（匹配预约单号或预约用途）

- **示例**: `GET /lab/reservations?page=1&size=10&userId=1&status=0`

- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 12,
        "orderNo": "R202312081454060123",
        "labName": "计算机实验室 - J-003",
        "labManagerName": "张老师",
        "reserverName": "张三",
        "createdTime": "2023-12-08 14:54:06",
        "startTime": "2023-12-08 15:00:00",
        "endTime": "2023-12-08 18:00:00",
        "purpose": "课程实验",
        "status": 0,
        "statusText": "待审核",
        "useStatusCode": 0,
        "useStatus": "待使用"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
}
```

**响应字段说明**:
- `id`: 预约记录ID
- `orderNo`: 预约单号
- `labName`: 实验室名称
- `labManagerName`: 实验室管理员姓名
- `reserverName`: 预约人姓名
- `createdTime`: 操作时间（创建时间）
- `startTime`: 预约开始时间
- `endTime`: 预约结束时间
- `purpose`: 预约用途
- `status`: 预约状态码 (0-待审核, 1-已通过, 2-已拒绝, 4-已取消)
- `statusText`: 状态文本
- `useStatusCode`: 使用状态码 (0-待审核, 1-使用中, 2-使用完成, 3-待使用, 4-已取消)
- `useStatus`: 使用状态文本（后端计算，前端可直接展示）

---

### 6.2 创建预约（Student）

- **URL**: `POST /lab/reservations`
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "userId": 1,
  "labId": 3,
  "startTime": "2023-12-08 15:00:00",
  "endTime": "2023-12-08 18:00:00",
  "purpose": "课程实验"
}
```

**字段说明**:
- `userId`: 预约人用户ID（必填）
- `labId`: 实验室ID（必填）
- `startTime`: 预约开始时间（必填，格式: yyyy-MM-dd HH:mm:ss）
- `endTime`: 预约结束时间（必填，格式: yyyy-MM-dd HH:mm:ss）
- `purpose`: 预约用途（可选）

**校验规则**（参见上文「预约业务规则与校验」）:
- 实验室必须存在且状态为 1（正常）
- 开始时间早于结束时间
- 预约须同一天且在实验室开放时间内
- 该实验室在该时间段无其他有效预约（不重叠）
- 该用户在该实验室该时间段无其他有效预约（不重复）

- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 13,
    "orderNo": "R202312081500010456",
    "labName": "计算机实验室",
    "lab": { "id": 3, "name": "计算机实验室", "code": "LAB-003" },
    "labManagerName": "张老师",
    "reserverName": "张三",
    "user": { "userId": 1, "userName": "张三" },
    "createdTime": "2023-12-08 15:00:01",
    "startTime": "2023-12-08 15:00:00",
    "endTime": "2023-12-08 18:00:00",
    "purpose": "课程实验",
    "status": 0,
    "statusText": "待审核",
    "useStatusCode": 0,
    "useStatus": "待使用"
  }
}
```

- **错误响应示例**（4xx，`code` 非 200）:

| 场景 | message 示例 |
|------|----------------|
| 参数缺失 | `userId不能为空` / `labId不能为空` / `startTime和endTime不能为空` |
| 时间格式错误 | `时间格式应为: yyyy-MM-dd HH:mm:ss` |
| 时间顺序错误 | `开始时间必须早于结束时间` |
| 实验室不可预约 | `该实验室当前不可预约（停用或维护中）` |
| 跨天预约 | `预约开始与结束须为同一天` |
| 超出开放时间 | `预约时间须在实验室开放时间内（08:00:00～22:00:00）` |
| 实验室该时段已被占 | `该时间段该实验室已被预约，请选择其他时间或实验室` |
| 用户重复预约 | `您在该时间段已预约过该实验室，请勿重复预约` |
| 用户/实验室不存在 | `用户不存在` / `实验室不存在` |

```json
{
  "code": 400,
  "message": "该时间段该实验室已被预约，请选择其他时间或实验室",
  "data": null
}
```

---

### 6.3 查询预约详情

- **URL**: `GET /lab/reservations/{id}`
- **路径参数**: `id` (Long) - 预约记录ID

- **成功响应**: 同6.1列表中的单条记录格式

---

### 6.4 审核预约（Admin/LabManager）

- **URL**: `PUT /lab/reservations/{id}/audit`
- **说明**: 管理员审核预约，通过或拒绝
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "status": 1,
  "auditUserId": 5,
  "rejectReason": "时间冲突"
}
```

**字段说明**:
- `status`: 审核状态（必填）
  - `1`: 通过
  - `2`: 拒绝
- `auditUserId`: 审核人用户ID（可选）
- `rejectReason`: 拒绝原因（拒绝时必填/可选）

- **成功响应**: 返回更新后的预约记录详情

---

### 6.5 取消预约（Student/Teacher）

- **URL**: `PUT /lab/reservations/{id}/cancel`
- **说明**: 在 **待审核** 或 **待使用**（审核通过但未到使用时间段）时可取消；学生/老师只能取消自己的预约
- **请求体**: 可选；若传 `userId`，后端会校验只能取消自己的预约

```json
{
  "userId": 1
}
```

- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 6.6 使用完成（Student）

- **URL**: `PUT /lab/reservations/{id}/finish`
- **说明**: 已通过的预约在使用中，用户可提前点击「使用完成」，将**使用状态**变为已结束，实验室释放为空闲（预约状态仍保持已通过）
- **请求体**: 可选；建议携带 `userId` 便于校验只能操作本人预约

**校验**：预约须存在、status=1（已通过）、当前时间 ≥ 预约开始时间

- **成功响应**: 返回更新后的预约详情（status=1，useStatus=已结束）
- **错误响应**:
  - `只能对已通过的预约进行操作`（status 非 1）
  - `尚未到预约开始时间，无法提前结束`

> **自动释放**：若用户未点击使用完成，等预约结束时间（endTime）过后，后端会将该预约展示为 `useStatus=已结束`，且 overlap 校验会排除 endTime 已过的预约，实验室自动可被新预约，无需定时任务更新数据库状态。

---

### 6.7 故障报修（Student）

- **URL**: `POST /lab/reservations/{id}/repair`
- **说明**: 使用完成后，用户可对已通过且已结束的预约进行故障报修。预约状态和使用状态保持不变，创建独立的报修记录。
- **Headers**:
  - `Content-Type: application/json`
- **请求体**:

```json
{
  "userId": 1,
  "title": "电脑无法开机",
  "description": "3号机按下电源键无反应"
}
```

**字段说明**:
- `userId`: 报修人用户ID（可选，用于校验）
- `title`: 报修标题（必填）
- `description`: 问题描述（可选）

**校验**：
- 预约须存在且 `status=1`（已通过）
- 预约 `useStatus=2`（已结束）
- 报修标题不能为空

- **成功响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "repairId": 15,
    "title": "电脑无法开机",
    "status": 0,
    "statusText": "待处理",
    "createdAt": "2023-12-08 18:30:00",
    "reservationId": 12,
    "orderNo": "R202312081454060123",
    "message": "报修提交成功"
  }
}
```

- **错误响应**:
  - `只能对审核通过的预约进行报修`
  - `只有使用完成后才能进行故障报修`
  - `报修标题不能为空`

---

### 6.8 重复预约与问题归属

- **重复/重叠预约由后端强制校验**：创建预约时，后端会检查同一实验室、同一用户在同一时间段是否已有有效预约，若存在则直接返回 400 及上述错误信息。因此**根因在后端逻辑**，后端已实现上述规则后即可防止重复预约。
- **并发控制**：对同一实验室的创建请求使用**悲观写锁**（`SELECT ... FOR UPDATE`）串行化，避免并发请求同时通过校验后重复入库。创建预约接口在事务内先对实验室行加锁，再执行校验与插入，锁在事务提交/回滚时释放。
- **唯一约束**：时间重叠无法用简单唯一索引表达，由应用层校验 + 行级锁保证；若需进一步加固，可考虑数据库触发器或排他约束（实现较复杂）。
- **前端建议**：前端可在提交前根据「预约列表」或「实验室时段占用」接口预判是否冲突，并提示用户；提交时使用 loading、禁用按钮等防重复提交，减少无效请求；最终以后端返回为准。

---

### 6.9 状态说明

| 状态码 | 状态文本 | 说明 |
|-------|---------|------|
| 0 | 待审核 | 刚创建的预约，等待管理员审核 |
| 1 | 已通过 | 审核通过，预约生效 |
| 2 | 已拒绝 | 审核未通过 |
| 4 | 已取消 | 用户主动取消或管理员取消 |

---

### 6.10 使用状态说明

| useStatusCode | useStatus | 说明 |
|--------------|----------|------|
| 0 | 待审核 | 预约待审核 |
| 1 | 使用中 | 审核通过且已到使用时间段内 |
| 2 | 使用完成 | 用户点击使用完成，或使用时间段结束自动完成 |
| 3 | 待使用 | 审核通过但未到使用时间段，操作栏可取消预约 |
| 4 | 已取消 | 预约取消或审核不通过 |

---

## 七、报修模块 RepairController（前缀 `/lab/repairs`）

使用完成后，用户可填报修表单生成报修记录。

### 7.1 分页查询报修记录

- **URL**: `GET /lab/repairs`
- **Query 参数**: `page`, `size`, `userId`, `labId`, `status`（0-待处理, 1-处理中, 2-已完成, 3-已关闭）

### 7.2 查询报修详情

- **URL**: `GET /lab/repairs/{id}`

### 7.3 创建报修（用户）

- **URL**: `POST /lab/repairs`
- **请求体**:

```json
{
  "userId": 1,
  "labId": 3,
  "title": "电脑无法开机",
  "description": "3号机按下电源键无反应",
  "reservationId": 12
}
```

**字段说明**:
- `userId`: 报修人ID（必填）
- `labId`: 实验室ID（必填）
- `title`: 报修标题（必填）
- `description`: 问题描述（可选）
- `reservationId`: 关联预约ID（可选，使用完成后报修可传）

- **成功响应**: 返回报修记录详情，status=0（待处理）

---

## 八、检修模块 MaintenanceController（前缀 `/lab/maintenances`）

系统管理员处理报修，生成检修记录。

### 8.1 分页查询检修记录

- **URL**: `GET /lab/maintenances`
- **说明**: 分页查询检修记录，支持按实验室、报修单筛选。**支持数据权限控制**
- **Query 参数**:
  - `page` (int, 默认 `1`) - 页码
  - `size` (int, 默认 `10`) - 每页数量
  - `labId` (Long, 可选) - 实验室ID筛选（仅系统管理员有效）
  - `repairId` (Long, 可选) - 报修单ID筛选（仅系统管理员有效）
  - `reporterUserId` (Integer, 可选) - 报修人用户 ID（与 `reporterId` 二选一即可，同时传时优先 `reporterUserId`）
  - `reporterId` (Integer, 可选) - 同上报修人用户 ID，与前端历史参数名兼容
  - `userId` (Integer, 可选) - 当前用户ID
  - `role` (String, 可选) - 当前用户角色（学生/教师/实验室管理员/系统管理员）

#### 数据权限规则

前端调用时需传入 `userId` 和 `role` 参数，后端根据角色返回不同的数据范围：

| 角色 | 数据范围 | 说明 |
|------|---------|------|
| 学生/教师 | 只能看到自己报修的检修记录 | 通过关联的报修单过滤 |
| 实验室管理员 | 能看到所管辖实验室的检修记录 | 通过实验室的 manager_id 过滤 |
| 系统管理员 | 能看到所有检修记录 | 可继续使用 labId、repairId、`reporterUserId`/`reporterId`（按报修人）筛选 |

筛选 **报修人** 时，仅返回「关联了报修单且该报修单的报修人为指定用户」的检修记录；无报修单关联的检修单不会出现在结果中。

#### 调用示例

**学生/教师查看自己的检修记录**:
```
GET /lab/maintenances?userId=1&role=学生&page=1&size=10
```

**实验室管理员查看所管辖实验室的检修记录**:
```
GET /lab/maintenances?userId=5&role=实验室管理员&page=1&size=10
```

**系统管理员查看所有检修记录（可加筛选）**:
```
GET /lab/maintenances?userId=2&role=系统管理员&labId=3&page=1&size=10
```

**按报修人筛选（系统管理员 / 实验室管理员等，与数据权限叠加）**:
```
GET /lab/maintenances?userId=2&role=系统管理员&reporterUserId=9&page=1&size=10
```

### 8.2 查询检修详情

- **URL**: `GET /lab/maintenances/{id}`

### 8.3 创建检修记录（系统管理员）

- **URL**: `POST /lab/maintenances`
- **请求体**:

```json
{
  "repairId": 5,
  "labId": 3,
  "content": "更换电源并做通电测试",
  "result": "已修复，测试正常",
  "maintenanceUnit": "校内后勤",
  "maintenanceTime": "2026-03-09 10:30:00",
  "handler": "王师傅",
  "handlerPhone": "13812345678"
}
```

**字段说明**:
- `repairId`: 关联的报修单ID（可选，可为空，允许独立录入）
- `labId`: 检修的实验室ID（必填；若传了 `repairId` 可不传，后端会从报修记录带出实验室）
- `content`: 检修内容/说明（必填，对应前端「检修说明」，TEXT）
- `result`: 检修结果（必填，VARCHAR(255)）
- `maintenanceUnit`: 检修单位（必填，对应前端「检修单位」，VARCHAR(100)）
- `maintenanceTime`: 检修时间（必填；格式 `yyyy-MM-dd HH:mm:ss`）
- `handler`: 检修人（必填，对应前端「检修人」，VARCHAR(50)）
- `handlerPhone`: 联系电话（必填，对应前端「联系电话」，VARCHAR(20)）

- **成功响应**: 返回检修记录详情；若关联了 `repairId`，后端会将对应报修记录 status 更新为 2（已完成）

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 12,
    "content": "更换电源并做通电测试",
    "result": "已修复，测试正常",
    "maintenanceUnit": "校内后勤",
    "maintenanceTime": "2026-03-09 10:30:00",
    "createdTime": "2026-03-09 10:35:00",
    "updatedTime": "2026-03-09 10:35:00",
    "status": 0,
    "statusText": "处理中",
    "handler": "王师傅",
    "handlerPhone": "13812345678",
    "lab": {
      "id": 3,
      "name": "计算机实验室",
      "code": "LAB-003"
    },
    "labId": 3,
    "repair": {
      "id": 5,
      "title": "电脑无法开机",
      "description": "3号机按下电源键无反应",
      "status": 2,
      "reporter": {
        "userId": 9,
        "userName": "张三"
      }
    },
    "repairId": 5,
    "reporterId": 9,
    "reporterName": "张三"
  }
}
```

**响应字段说明**:
- `id`: 检修记录ID
- `content`: 检修内容/说明
- `result`: 检修结果
- `maintenanceUnit`: 检修单位
- `maintenanceTime`: 检修时间
- `createdTime`: 创建时间
- `updatedTime`: 更新时间
- `status`: 检修状态（0-处理中, 1-已完成）
- `statusText`: 状态文本
- `handler`: 检修人
- `handlerPhone`: 联系电话
- `lab`: 关联实验室信息
- `labId`: 实验室ID
- `repair`: 关联报修单信息（如有），包含报修人信息
  - `reporter`: 报修人信息
    - `userId`: 报修人ID
    - `userName`: 报修人姓名
- `repairId`: 报修单ID
- `reporterId`: 报修人ID（直接在检修记录级别暴露，方便权限判断）
- `reporterName`: 报修人姓名（直接在检修记录级别暴露）

### 8.4 更新检修记录（按 id）

- **URL**: `PUT /lab/maintenances/{id}`
- **说明**: 按主键更新检修记录；请求体为 JSON，**字段均可选**，仅传需要修改的项即可。
- **Headers**: `Content-Type: application/json`
- **请求体示例**:

```json
{
  "content": "补充：更换电源后再次压测 2 小时",
  "maintenanceUnit": "校内后勤",
  "maintenanceTime": "2026-03-09 14:00:00",
  "handler": "李师傅",
  "handlerPhone": "13900001111",
  "labId": 3,
  "status": 1
}
```

**字段说明**:
- `content`: 检修内容（若传则不能为空字符串）
- `maintenanceUnit`: 检修单位
- `maintenanceTime`: 检修时间（格式 `yyyy-MM-dd HH:mm:ss`）
- `handler`: 检修人
- `handlerPhone`: 联系电话
- `labId`: 更换关联实验室时传入
- `status`: `0`-处理中，`1`-已完成；若本记录关联了报修单，会同步更新报修状态（0→1 处理中，1→2 已完成）

> 另：若仅需改状态，可继续用 `PUT /lab/maintenances/{id}/status`；**常用 REST 风格**为本文 `PUT /lab/maintenances/{id}`（可同时改状态与其它字段）。

---

## 九、实验室状态与预约关系

| 实验室 status | 含义 | 是否可被预约 |
|---------------|------|--------------|
| 0 | 停用 | 否 |
| 1 | 正常 | 是 |
| 2 | 维护中 | 否 |

---

## 十、角色类型说明

| 前端路由 | 角色值 |
|---------|--------|
| /users | 系统管理员 |
| /users/lab-managers | 实验室管理员 |
| /users/students | 学生 |
| /users/teachers | 教师 |

---

## 十一、前端对接示例

### axios 封装（`src/api/request.js`）

```javascript
import axios from 'axios';

const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

service.interceptors.response.use(
  response => {
    const res = response.data;
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res.data;
  },
  error => {
    return Promise.reject(error);
  }
);

export default service;
```

### 用户 API 封装（`src/api/user.js`）

```javascript
import request from './request';

export const listUsers = (params) => request({ url: '/lab/users', method: 'get', params });
export const createUser = (data) => request({ url: '/lab/users', method: 'post', data });
export const updateUser = (userId, data) => request({ url: `/lab/users/${userId}`, method: 'put', data });
export const deleteUser = (userId) => request({ url: `/lab/users/${userId}`, method: 'delete' });
export const updateUserPassword = (userId, password) => request({ 
  url: `/lab/users/${userId}/password`, method: 'put', data: { password } 
});
export const getProfile = (userId) => request({ url: '/lab/users/profile', method: 'get', params: { userId } });
export const updateProfile = (data) => request({ url: '/lab/users/profile', method: 'put', data });
export const changePassword = (data) => request({ url: '/lab/users/password', method: 'put', data });
```

---

> **文档生成时间**: 2025年3月
> **后端框架**: Spring Boot + JPA
> **接口前缀**: `/lab`
