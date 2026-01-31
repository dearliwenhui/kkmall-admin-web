# K3s 部署指南

## 前置条件

1. 已安装 k3s
2. 已配置 kubectl 访问 k3s 集群
3. GitHub Container Registry 访问权限

## 部署步骤

### 1. 创建 GitHub Container Registry 密钥

首先,在 GitHub 创建一个 Personal Access Token (PAT):
- 访问 GitHub Settings > Developer settings > Personal access tokens > Tokens (classic)
- 点击 "Generate new token (classic)"
- 选择权限: `read:packages`
- 生成并复制 token

然后在 k3s 中创建密钥:

```bash
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=你的GitHub用户名 \
  --docker-password=你的GitHub_PAT \
  --docker-email=你的邮箱 \
  -n kkmall
```

### 2. 部署应用

```bash
# 应用配置
kubectl apply -f k8s/deployment.yaml

# 查看部署状态
kubectl get pods -n kkmall
kubectl get svc -n kkmall
kubectl get ingress -n kkmall
```

### 3. 更新部署

当 GitHub Actions 构建新镜像后,更新部署:

```bash
# 重启 deployment 以拉取最新镜像
kubectl rollout restart deployment/kkmall-admin-web -n kkmall

# 查看滚动更新状态
kubectl rollout status deployment/kkmall-admin-web -n kkmall
```

### 4. 查看日志

```bash
# 查看 pod 日志
kubectl logs -f deployment/kkmall-admin-web -n kkmall

# 查看最近的日志
kubectl logs --tail=100 deployment/kkmall-admin-web -n kkmall
```

### 5. 故障排查

```bash
# 查看 pod 详情
kubectl describe pod -l app=kkmall-admin-web -n kkmall

# 查看事件
kubectl get events -n kkmall --sort-by='.lastTimestamp'

# 进入容器调试
kubectl exec -it deployment/kkmall-admin-web -n kkmall -- /bin/sh
```

## 配置说明

### 修改域名

编辑 `k8s/deployment.yaml` 中的 Ingress 配置:
```yaml
spec:
  rules:
  - host: kkmall-admin.yourdomain.com  # 修改为你的域名
```

### 调整资源限制

根据你的 OCI ARM 实例配置调整:
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

### 修改副本数

```yaml
spec:
  replicas: 2  # 根据需要调整
```

## 自动化部署

可以使用以下脚本自动拉取最新镜像:

```bash
#!/bin/bash
# update-deployment.sh

echo "拉取最新镜像并更新部署..."
kubectl rollout restart deployment/kkmall-admin-web -n kkmall
kubectl rollout status deployment/kkmall-admin-web -n kkmall
echo "部署完成!"
```

## 监控和维护

```bash
# 查看资源使用情况
kubectl top pods -n kkmall

# 查看服务端点
kubectl get endpoints -n kkmall

# 测试服务连通性
kubectl run -it --rm debug --image=alpine --restart=Never -n kkmall -- sh
# 在容器内执行: wget -O- http://kkmall-admin-web/hello
```
