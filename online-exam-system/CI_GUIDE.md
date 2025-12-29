# CI / 本地构建 指南

说明：本项目已升级到 Java 21。以下说明帮助在本地和 CI 中一致地使用 Java 21。

本地运行（Windows）:

1. 使用自带脚本（项目根）：

```powershell
# 使用 JDK21 构建（包含测试）
./run_build_jdk21.cmd
```

2. 如果你要在命令行直接运行：

```powershell
setx JAVA_HOME "C:\Users\16673\apps\java\jdk-21.0.9" /M
# 新开终端后运行
mvn -B package
```

GitHub Actions CI:

- 我们添加了一个工作流文件 `.github/workflows/ci.yml`，使用 `actions/setup-java@v4` 将运行器设置为 JDK 21（Temurin），并执行 `mvn -B package`。
- CI 会在 `main` 分支的 push 和 PR 时执行。

注意事项：
- 如果 CI 需要部署或推到容器镜像，请告知我我会添加相应步骤。
