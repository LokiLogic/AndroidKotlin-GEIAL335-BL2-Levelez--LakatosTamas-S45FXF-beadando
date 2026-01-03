# 📱 Mobil Programozás — Beadandó Feladat  
## 📋 Jegyzőkönyv

**Készítette:** Lakatos Tamás  
**Neptun:** S45FXF  
**Dátum:** 2026. január 3.

---

## 📑 Tartalomjegyzék
- [1. 📖 Bevezetés](#1-bevezetés)
- [2. 🗄️ Adatbázis réteg (Room)](#2-adatbázis-réteg-room)
  - [2.1 📊 Adatmodell (Employee.kt)](#21-adatmodell-employeekt)
  - [2.2 🔍 DAO (EmployeeDAO.kt)](#22-dao-employeedao.kt)
  - [2.3 🏗️ Adatbázis példány (AppDatabase.kt)](#23-adatbázis-példány-appdatabase.kt)
- [3. 🎨 Felhasználói felület és megjelenítés](#3-felhasználói-felület-és-megjelenítés)
  - [3.1 🏠 MainActivity.kt](#31-mainactivity.kt)
  - [3.2 🔄 EmployeeAdapter.kt](#32-employeeadapter.kt)
- [4. ⚙️ Funkciók megvalósítása](#4-funkciók-megvalósítása)
  - [4.1 ➕ EmployeeDialog.kt (új felvétel és szerkesztés)](#41-employeedialog.kt-új-felvétel-és-szerkesztés)
  - [4.2 🗑️ Törlés és szálkezelés](#42-törlés-és-szálkezelés)
  - [4.3 🔎 Keresés](#43-keresés)
- [5. 📝 Összegzés](#5-összegzés)

---

## 1. 📖 Bevezetés
Az alkalmazás célja munkavállalók adatainak (név, pozíció, fizetés, tapasztalat, email) nyilvántartása modern, átlátható felületen. Főbb funkciók:
- 📋 **Listázás:** minden rögzített alkalmazott megjelenítése.
- ➕ **Új felvétel:** dolgozó hozzáadása.
- ✏️ **Szerkesztés:** meglévő adatok módosítása.
- 🗑️ **Törlés:** munkavállaló eltávolítása (pl. elhúzás).
- 🔎 **Keresés:** gyors név szerinti keresés.

Használt technológiák:
- 🅺 **Nyelv:** Kotlin
- 🤖 **Platform:** Android SDK
- 💾 **Perzisztencia:** Room (SQLite)
- 🎨 **UI:** RecyclerView, CardView, FloatingActionButton, DialogFragment, Material Design komponensek

---

## 2. 🗄️ Adatbázis réteg (Room)
A Room biztosítja az adatok perzisztens tárolását és egyszerű, típusbiztos lekérdezéseket.

### 2.1 📊 Adatmodell (Employee.kt)
A data class az employee tábla reprezentációja. Serializable, hogy könnyen átadható legyen komponensek között.

```kotlin
// Példa: Employee.kt
@Entity(tableName = "employee")
data class Employee(
    @PrimaryKey(autoGenerate = true) var employeeId: Long? = null,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "position") var position: String,
    @ColumnInfo(name = "salary") var salary: Int,
    @ColumnInfo(name = "experience") var experience: Int,
    @ColumnInfo(name = "email") var email: String
) : Serializable
```

### 2.2 🔍 DAO (EmployeeDAO.kt)
A DAO-k kezelik a lekérdezéseket és módosításokat.

```kotlin
// Példa: EmployeeDAO.kt
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
```

### 2.3 🏗️ Adatbázis példány (AppDatabase.kt)
Singleton minta alkalmazása javasolt, exportSchema = false beállítással a build figyelmeztetések elkerüléséhez.

---

## 3. 🎨 Felhasználói felület és megjelenítés

### 3.1 🏠 MainActivity.kt
Belépési pont: RecyclerView inicializálása, SearchView és FAB kezelése. Indításkor, ha üres az adatbázis, feltöltés mintákkal (pl. John Doe), hogy az első indítás is értelmes legyen.

### 3.2 🔄 EmployeeAdapter.kt
Adapter összeköti az adatokat a nézettel. A ViewHolder tárolja a row_item view elemeit. Törlés és szerkesztés eseményeket az adapter kezeli, az Activity-n keresztül hívva a megfelelő dialogokat/metódusokat.

```kotlin
// Részlet az adapter onBindViewHolder-ból
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val employee = items[position]
    holder.tvName.text = employee.name
    // ... többi mező beállítása ...

    holder.btnDelete.setOnClickListener {
        deleteItem(holder.adapterPosition)
    }
    holder.btnEdit.setOnClickListener {
        (holder.itemView.context as MainActivity).showEditEmployeeDialog(employee)
    }
}
```

---

## 4. ⚙️ Funkciók megvalósítása

### 4.1 ➕ EmployeeDialog.kt (új felvétel és szerkesztés)
DialogFragment alapú űrlap: új felvétel esetén üres mezők, szerkesztésnél a Bundle-ben kapott Employee értékekkel töltődik fel.

```kotlin
// Adatok betöltése szerkesztésnél
val arguments = this.arguments
if (arguments != null && arguments.containsKey(MainActivity.KEY_ITEM_TO_EDIT)) {
    val employee = arguments.getSerializable(MainActivity.KEY_ITEM_TO_EDIT) as Employee
    etName.setText(employee.name)
    // ...
}
```

A getSerializable használatánál figyeltem a visszafelé kompatibilitásra.

### 4.2 🗑️ Törlés és szálkezelés
Adatbázis műveletek háttérszálon történnek (Thread, Coroutine is ajánlott). A sikeres művelet után a UI-t a főszálon frissítjük.

```kotlin
fun deleteItem(position: Int) {
    val employeeToDelete = items[position]
    val dbThread = Thread {
        AppDatabase.getInstance(context).employeeDao().deleteEmployee(employeeToDelete)

        // UI frissítés a főszálon
        (context as MainActivity).runOnUiThread {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    dbThread.start()
}
```

(A modern megoldás Coroutine + Dispatchers.IO/Main használata.)

### 4.3 🔎 Keresés
SearchView.OnQueryTextListener implementálva: gépelés közben a filterItems metódus fut, ami SQL LIKE lekérdezést hajt végre háttérszálon, majd frissíti az adaptert.

---

## 5. 📝 Összegzés
Az alkalmazás megfelel a követelményeknek: stabil Room integráció, reszponzív UI háttérszál kezeléssel és modern komponensek használatával. A dokumentáció tömör, áttekinthető és könnyen követhető a további fejlesztésekhez.

---
