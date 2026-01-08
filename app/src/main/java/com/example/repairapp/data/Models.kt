package com.example.repairapp.data

data class User(
    val id: Long,
    val username: String,
    val password: String,
    val role: String,
    val createTime: Long
)

data class RepairOrder(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val type: String,
    val location: String,
    val level: Int,
    val description: String,
    val status: Int,
    val handlerId: Long?,
    val handlerName: String?,
    val claimTime: Long?,
    val createTime: Long,
    val updateTime: Long
)

data class RepairLog(
    val id: Long,
    val orderId: Long,
    val operatorId: Long,
    val operatorName: String,
    val action: String,
    val note: String?,
    val time: Long
)
