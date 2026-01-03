package hu.android.kotlinshoppinglist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EmployeeDAO {

    @Query("SELECT * FROM employee")
    fun findAllEmployees(): List<Employee>

    @Query("SELECT * FROM employee WHERE name LIKE '%' || :search || '%'")
    fun findEmployeesByName(search: String): List<Employee>

    @Insert
    fun insertEmployee(employee: Employee): Long

    @Delete
    fun deleteEmployee(employee: Employee)

    @Update
    fun updateEmployee(employee: Employee)

}
