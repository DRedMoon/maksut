package com.oma.maksut.database.dao

import androidx.room.*
import com.oma.maksut.database.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE is_monthly_payment = 1 ORDER BY name ASC")
    fun getMonthlyPaymentCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE has_due_date = 1 ORDER BY name ASC")
    fun getCategoriesWithDueDate(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?
    
    @Insert
    suspend fun insertCategory(category: Category): Long
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)
    
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}