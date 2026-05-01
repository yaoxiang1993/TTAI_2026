# Android项目清理构建脚本
# 使用方法: .\clean-build.ps1

param(
    [switch]$FullClean = $false,
    [switch]$Offline = $false
)

Write-Host "🧹 Android项目清理构建" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green

# 停止Gradle守护进程
Write-Host "停止Gradle守护进程..." -ForegroundColor Yellow
.\gradlew --stop
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Gradle守护进程已停止" -ForegroundColor Green
} else {
    Write-Host "⚠️  Gradle守护进程停止失败（可能没有运行）" -ForegroundColor Yellow
}

# 清理项目
Write-Host "清理项目构建缓存..." -ForegroundColor Yellow
.\gradlew clean
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 项目清理完成" -ForegroundColor Green
} else {
    Write-Host "❌ 项目清理失败" -ForegroundColor Red
    exit 1
}

# 完全清理（可选）
if ($FullClean) {
    Write-Host "执行完全清理..." -ForegroundColor Yellow
    
    # 删除Gradle缓存
    $gradleCachePath = "$env:USERPROFILE\.gradle\caches"
    if (Test-Path $gradleCachePath) {
        Remove-Item -Path $gradleCachePath -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "✅ Gradle缓存已清理" -ForegroundColor Green
    }
    
    # 删除项目构建目录
    $buildPath = ".\build"
    if (Test-Path $buildPath) {
        Remove-Item -Path $buildPath -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "✅ 项目构建目录已清理" -ForegroundColor Green
    }
    
    $appBuildPath = ".\app\build"
    if (Test-Path $appBuildPath) {
        Remove-Item -Path $appBuildPath -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "✅ App构建目录已清理" -ForegroundColor Green
    }
}

# 刷新依赖
Write-Host "刷新项目依赖..." -ForegroundColor Yellow
if ($Offline) {
    .\gradlew --offline build
} else {
    .\gradlew build --refresh-dependencies
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 依赖刷新完成" -ForegroundColor Green
} else {
    Write-Host "❌ 依赖刷新失败" -ForegroundColor Red
    Write-Host "尝试离线构建..." -ForegroundColor Yellow
    .\gradlew --offline build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 离线构建成功" -ForegroundColor Green
    } else {
        Write-Host "❌ 构建失败，请检查网络连接和依赖配置" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n🎉 清理构建完成！" -ForegroundColor Green
Write-Host "现在可以正常构建项目了" -ForegroundColor Cyan 