# GitHub仓库创建指南

## 创建步骤

1. 访问 https://github.com/new
2. 填写仓库信息：
   - **Repository name**: ParticleAdorn
   - **Description**: Minecraft Nukkit粒子装扮系统 - 基于数学参数方程的3D粒子特效引擎
   - **Public**: ✓ Public
   - **Initialize this repository with**: 不勾选任何选项（因为我们已经有本地文件）

3. 点击 "Create repository"

## 推送现有代码

创建仓库后，执行以下命令：

```bash
git remote add origin https://github.com/TiXYA2357/ParticleAdorn.git
git branch -M main
git push -u origin main
```

## 仓库设置建议

### 1. 添加Topics标签
在仓库设置中添加以下topics：
- minecraft
- nukkit
- particle
- plugin
- java
- effects

### 2. 社交预览设置
- **Social preview image**: 可以添加插件效果图
- **Title**: ParticleAdorn 粒子装扮系统
- **Description**: 基于数学参数方程的Minecraft粒子特效引擎

### 3. 其他设置
- 启用Wiki（可选）
- 启用Pages（可选，用于展示文档）
- 设置默认分支为main

## 后续维护

### 版本发布
```bash
# 创建新版本
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### 更新文档
```bash
# 更新README等文档
git add README.md
git commit -m "docs: update documentation"
git push
```

### 处理Issues和PR
- 及时回复用户反馈
- 定期清理已解决的issues
- 审核合并贡献者的PR