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


    @Query("SELECT * FROM payerinfo WHERE firestore_document_no = :firestore_document_no")
    suspend fun getPayerInfoWithCosts(firestore_document_no : String) : PayerInfoWithCosts

    @Query("SELECT * FROM costs WHERE firestore_document_no = :firestore_document_no")
    suspend fun getCostListInfo(firestore_document_no : String) : List<Costs>


    @Query("SELECT * FROM costs WHERE cost_uuid = :cost_uuid")
    suspend fun getCostInfo(cost_uuid : Int) : Costs


    @Query("SELECT * FROM payerinfo")
    suspend fun getAllPayers() : List<PayerInfo>

    @Query("DELETE FROM payerinfo WHERE firestore_document_no = :firestore_document_no")
    suspend fun deletePayer(firestore_document_no : String)

    @Query("DELETE FROM costs WHERE cost_uuid = :cost_uuid")
    suspend fun deleteCost(cost_uuid : Int)

    @Update(entity = PayerInfo::class)
    suspend fun updatePayer(payerInfo: PayerInfo)


    @Update(entity = Costs::class)
    suspend fun updateCosts(costs: Costs)
    //@Update
    //suspend fun updatePayerInfoWithCosts(payerInfoWithCosts : PayerInfoWithCosts)


}