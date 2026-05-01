# Android项目构建性能监控脚本
# 使用方法: .\build-performance.ps1

param(
    [string]$Task = "assembleDebug",
    [switch]$Clean = $false,
    [switch]$Profile = $false
)

Write-Host "🚀 Android项目构建性能监控" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# 检查Java环境
Write-Host "检查Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion) {
        Write-Host "✅ Java已安装: $javaVersion" -ForegroundColor Green
    } else {
        Write-Host "❌ Java未安装或未配置PATH" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Java未安装或未配置PATH" -ForegroundColor Red
    exit 1
}

# 检查Gradle环境
Write-Host "检查Gradle环境..." -ForegroundColor Yellow
if (Test-Path ".\gradlew") {
    Write-Host "✅ Gradle Wrapper已存在" -ForegroundColor Green
} else {
    Write-Host "❌ Gradle Wrapper不存在" -ForegroundColor Red
    exit 1
}

# 清理构建缓存（如果需要）
if ($Clean) {
    Write-Host "🧹 清理构建缓存..." -ForegroundColor Yellow
    .\gradlew clean
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 清理完成" -ForegroundColor Green
    } else {
        Write-Host "❌ 清理失败" -ForegroundColor Red
        exit 1
    }
}

# 构建项目
Write-Host "🔨 开始构建项目..." -ForegroundColor Yellow
$startTime = Get-Date

if ($Profile) {
    # 使用构建扫描进行性能分析
    Write-Host "📊 启用构建扫描..." -ForegroundColor Yellow
    .\gradlew $Task --scan --no-daemon
} else {
    # 普通构建
    .\gradlew $Task --no-daemon
}

$endTime = Get-Date
$buildDuration = $endTime - $startTime

# 显示构建结果
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 构建成功!" -ForegroundColor Green
    Write-Host "⏱️  构建耗时: $($buildDuration.TotalSeconds.ToString('F2')) 秒" -ForegroundColor Cyan
    
    # 性能建议
    Write-Host "`n💡 性能优化建议:" -ForegroundColor Yellow
    if ($buildDuration.TotalSeconds -gt 60) {
        Write-Host "   - 构建时间较长，建议启用Gradle守护进程" -ForegroundColor Red
        Write-Host "   - 检查是否启用了并行构建" -ForegroundColor Red
        Write-Host "   - 考虑增加JVM内存分配" -ForegroundColor Red
    } elseif ($buildDuration.TotalSeconds -gt 30) {
        Write-Host "   - 构建时间中等，可以考虑进一步优化" -ForegroundColor Yellow
    } else {
        Write-Host "   - 构建速度良好！" -ForegroundColor Green
    }
    
    # 显示构建产物
    $apkPath = ".\app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length / 1MB
        Write-Host "📱 APK大小: $($apkSize.ToString('F2')) MB" -ForegroundColor Cyan
    }
    
} else {
    Write-Host "❌ 构建失败!" -ForegroundColor Red
    Write-Host "请检查错误信息并修复问题" -ForegroundColor Red
}

Write-Host "`n📋 构建性能统计:" -ForegroundColor Yellow
Write-Host "   开始时间: $startTime" -ForegroundColor White
Write-Host "   结束时间: $endTime" -ForegroundColor White
Write-Host "   总耗时: $($buildDuration.TotalSeconds.ToString('F2')) 秒" -ForegroundColor White
Write-Host "   内存使用: $(Get-Process java -ErrorAction SilentlyContinue | ForEach-Object { $_.WorkingSet / 1MB } | Measure-Object -Average | ForEach-Object { $_.Average.ToString('F2') }) MB" -ForegroundColor White 