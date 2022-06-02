package com.mesutyukselusta.katlmevimicratakip.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfoWithCosts

@Database(entities = [
    PayerInfo::class,
    Costs::class,
                     ], version = 1 )

abstract class PayerDatabase : RoomDatabase() {
    abstract fun payerDao () : PayerDao


    companion object{
       @Volatile private var instance : PayerDatabase ?= null

        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock){
            instance ?: makeDatabase(context).also {
                instance = it
            }
        }

        private fun makeDatabase(context : Context) = Room.databaseBuilder(
            context.applicationContext,PayerDatabase::class.java,"payerdatabase").build()
    }
}