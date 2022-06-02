package com.mesutyukselusta.katlmevimicratakip.dataconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo

class Converter {

    @TypeConverter
    fun fromGroupTaskMemberList(value: List<PayerInfo>): String {
        val gson = Gson()
        val type = object : TypeToken<List<PayerInfo>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toGroupTaskMemberList(value: String): List<PayerInfo> {
        val gson = Gson()
        val type = object : TypeToken<List<PayerInfo>>() {}.type
        return gson.fromJson(value, type)
    }
}