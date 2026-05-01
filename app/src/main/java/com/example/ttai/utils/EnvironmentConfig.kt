package com.example.ttai.utils

import com.example.ttai.network.NetworkModule

/**
 * 环境配置工具类
 * 用于管理应用在不同环境下的配置
 */
object EnvironmentConfig {
    
    /**
     * 初始化环境配置
     * 在Application类中调用此方法
     */
    fun initialize() {
        // 默认使用测试环境
        setTestEnvironment()
    }
    
    /**
     * 设置为测试环境
     */
    fun setTestEnvironment() {
        NetworkModule.setEnvironment(NetworkModule.Environment.TEST)
    }
    
    /**
     * 设置为生产环境
     */
    fun setProductionEnvironment() {
        NetworkModule.setEnvironment(NetworkModule.Environment.PRODUCTION)
    }
    
    /**
     * 获取当前环境名称
     */
    fun getCurrentEnvironmentName(): String {
        return NetworkModule.getCurrentEnvironmentName()
    }
    
    /**
     * 切换环境
     * 用于调试时快速切换环境
     */
    fun toggleEnvironment() {
        val currentEnv = NetworkModule.getCurrentEnvironmentName()
        if (currentEnv == "测试环境") {
            setProductionEnvironment()
        } else {
            setTestEnvironment()
        }
    }
    
    /**
     * 检查是否为测试环境
     */
    fun isTestEnvironment(): Boolean {
        return getCurrentEnvironmentName() == "测试环境"
    }
    
    /**
     * 检查是否为生产环境
     */
    fun isProductionEnvironment(): Boolean {
        return getCurrentEnvironmentName() == "生产环境"
    }
} 