# kkmall-admin-web

Spring Boot 管理后台应用

## 技术栈

- Java 21
- Spring Boot 3.2.2
- Maven 3.9+

## 本地开发

```bash
# 编译
mvn clean package

# 运行
mvn spring-boot:run

# 访问
http://localhost:38080/hello
```

## Docker 构建

```bash
# 构建镜像
docker build -t kkmall-admin-web:latest .

# 运行容器
docker run -p 38080:38080 kkmall-admin-web:latest
```

## 部署到 k3s

详见 [k8s/README.md](k8s/README.md)

## CI/CD

项目使用 GitHub Actions 自动构建和推送 Docker 镜像到 GitHub Container Registry。

每次推送到 master 分支时,会自动:
1. 构建 Docker 镜像(支持 amd64 和 arm64 架构)
2. 推送到 ghcr.io/dearliwenhui/kkmall-admin-web

## 端口

- 应用端口: 38080
