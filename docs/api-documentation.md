# 飞牛经营系统 - 接口文档模板

## 约定

- Base URL：`http://localhost:8080`
- 数据格式：`application/json`
- 认证方式：按项目实际配置，通常是 Token 或 Session

## 接口规范

- GET 用于查询。
- POST 用于新增或提交动作。
- PUT/PATCH 用于更新。
- DELETE 用于删除。
- 所有接口应返回统一响应结构。

## 响应示例

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

## 接口编写建议

- 每个 Controller 文件对应一个业务模块。
- 请求参数使用 DTO，不直接暴露实体类。
- 返回结果使用 VO，保持前后端解耦。
- 需要分页的接口统一约定 `pageNo` 和 `pageSize`。
- 需要批量操作的接口明确入参格式和失败返回规则。

## 目录建议

```text
docs/api/
├── auth.md
├── user.md
├── finance.md
├── project.md
└── common.md
```

## 说明

当前仓库还没有把接口文档生成到具体模块中，因此这里先保留通用模板。等后端接口稳定后，再按模块拆分补齐。
