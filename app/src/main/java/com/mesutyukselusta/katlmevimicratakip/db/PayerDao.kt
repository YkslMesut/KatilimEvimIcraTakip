package com.mesutyukselusta.katlmevimicratakip.db

import androidx.room.*
import com.mesutyukselusta.katlmevimicratakip.model.Costs
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfo
import com.mesutyukselusta.katlmevimicratakip.model.PayerInfoWithCosts

@Dao
interface PayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosts(costs:Costs)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayerInfo(payerInfo:PayerInfo)


    @Query("SELECT * FROM payerinfo WHERE uuid = :uuid")
    suspend fun getPayerInfoWithCosts(uuid : Int) : PayerInfoWithCosts

    @Query("SELECT * FROM costs WHERE uuid = :uuid")
    suspend fun getCostListInfo(uuid : Int) : List<Costs>


    @Query("SELECT * FROM costs WHERE cost_uuid = :cost_uuid")
    suspend fun getCostInfo(cost_uuid : Int) : Costs


    @Query("SELECT * FROM payerinfo")
    suspend fun getAllPayers() : List<PayerInfo>

    @Query("DELETE FROM payerinfo WHERE uuid = :uuid")
    suspend fun deletePayer(uuid : Int)

    @Query("DELETE FROM costs WHERE cost_uuid = :cost_uuid")
    suspend fun deleteCost(cost_uuid : Int)

    @Update(entity = PayerInfo::class)
    suspend fun updatePayer(payerInfo: PayerInfo)


    @Update(entity = Costs::class)
    suspend fun updateCosts(costs: Costs)
    //@Update
    //suspend fun updatePayerInfoWithCosts(payerInfoWithCosts : PayerInfoWithCosts)


}