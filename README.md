# GT Review Assistant Backend

这是 GT 审核收口工作台的 Spring Boot 后端 MVP。

## 当前功能

- 项目管理
- 导入 PDF/Excel 结构台账
- 按附注、表、小节、行、列、单元格存储结构
- 自动生成 PDF vs Excel 结构差异
- 提供总览、附注列表、附注详情、差异列表接口

## 本地数据库

你当前 Navicat 里创建的库名是：`gt-review-assistant`。

第一次运行前，在 MySQL 执行：

```sql
source src/main/resources/db/schema.sql;
```

或者把 `src/main/resources/db/schema.sql` 内容复制到 Navicat 查询窗口执行。

默认连接：

```yaml
url: jdbc:mysql://localhost:3306/gt-review-assistant
username: root
password: 空
```

如果 root 有密码，可以设置环境变量：

```powershell
$env:GT_REVIEW_DB_PASSWORD='你的密码'
```

## 启动

```powershell
mvn spring-boot:run
```

健康检查：

```text
GET http://localhost:18081/api/health
```