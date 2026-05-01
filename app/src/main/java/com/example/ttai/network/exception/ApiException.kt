package com.example.ttai.network.exception

/**
 * API异常类，用于表示API调用过程中的错误
 */
class ApiException(val code: Int, override val message: String): Exception(message)