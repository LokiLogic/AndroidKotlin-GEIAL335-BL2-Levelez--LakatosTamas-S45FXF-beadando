package hu.android.kotlinshoppinglist.data

import java.io.Serializable

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "employee")
data class Employee(@PrimaryKey(autoGenerate = true) var employeeId: Long?,
                    @ColumnInfo(name = "name") var name: String,
                    @ColumnInfo(name = "position") var position: String,
                    @ColumnInfo(name = "salary") var salary: Int,
                    @ColumnInfo(name = "experience") var experience: Int,
                    @ColumnInfo(name = "email") var email: String
) : Serializable
