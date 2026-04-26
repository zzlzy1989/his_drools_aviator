---
alwaysApply: true
scene: git_message
---

# Git Commit Message 规则

## 提交格式
遵循 Conventional Commits 规范：
```
<type>(<scope>): <subject>

<body>

<footer>
```

## 提交类型 (type)
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档变更
- `style`: 代码格式调整（不影响代码逻辑）
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变更
- `ci`: CI/CD 配置变更
- `revert`: 回退提交

## 规则要求
1. 标题行不超过 72 个字符
2. 使用中文编写提交信息
3. 标题行末尾不加句号
4. type 和 scope 之间无空格
5. subject 使用祈使句，如"添加"、"修复"、"更新"
6. body 说明变更原因和具体改动（可选）
7. footer 可包含 BREAKING CHANGE 或关联 Issue（可选）

## 示例
```
feat(user): 添加用户登录功能

实现基于 JWT 的用户认证机制，支持账号密码登录和记住我功能

Closes #123
```

```
fix(order): 修复订单金额计算错误

修正了折扣应用顺序导致的金额计算偏差问题
```
