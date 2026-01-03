package hu.android.kotlinshoppinglist

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import hu.android.kotlinshoppinglist.adapter.EmployeeAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.appcompat.widget.Toolbar as AppCompatToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import hu.android.kotlinshoppinglist.data.AppDatabase
import hu.android.kotlinshoppinglist.data.Employee
import hu.android.kotlinshoppinglist.touch.ShoppingTouchHelperCallback
import androidx.appcompat.widget.SearchView
import android.view.Menu
import android.view.MenuItem




class MainActivity : AppCompatActivity(), EmployeeDialog.EmployeeHandler {
    companion object {
        val KEY_FIRST = "KEY_FIRST"
        val KEY_ITEM_TO_EDIT = "KEY_ITEM_TO_EDIT"
    }

    private lateinit var adapter: EmployeeAdapter
    private lateinit var toolbar: AppCompatToolbar
    private lateinit var fab: FloatingActionButton
    private lateinit var recyclerShopping: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        fab = findViewById(R.id.fab)
        recyclerShopping = findViewById(R.id.recyclerShopping)

        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            EmployeeDialog().show(supportFragmentManager, "TAG_ITEM")
        }

        initRecyclerView()

        saveThatItWasStarted()
    }

    private fun saveThatItWasStarted() {
        val sp = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sp.edit {
            putBoolean(KEY_FIRST, false)
        }
    }




    private fun initRecyclerView() {
        val dbThread = Thread {
            var items = AppDatabase.getInstance(this).employeeDao().findAllEmployees()

            if (items.isEmpty()) {
                // Add sample data - still on background thread
                val sampleEmployees = listOf(
                    Employee(null, "John Doe", "Software Engineer", 50000, 5, "john.doe@example.com"),
                    Employee(null, "Jane Smith", "Designer", 45000, 3, "jane.smith@example.com"),
                    Employee(null, "Bob Johnson", "Manager", 60000, 10, "bob.johnson@example.com")
                )
                for (emp in sampleEmployees) {
                    AppDatabase.getInstance(this@MainActivity).employeeDao().insertEmployee(emp)
                }
                items = AppDatabase.getInstance(this).employeeDao().findAllEmployees()
            }

            runOnUiThread {
                adapter = EmployeeAdapter(this@MainActivity, items)
                recyclerShopping.adapter = adapter

                val callback = ShoppingTouchHelperCallback(adapter)
                val touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(recyclerShopping)
            }
        }
        dbThread.start()
    }

    fun showEditEmployeeDialog(employeeToEdit: Employee) {
        val editDialog = EmployeeDialog()

        val bundle = Bundle()
        bundle.putSerializable(KEY_ITEM_TO_EDIT, employeeToEdit)
        editDialog.arguments = bundle

        editDialog.show(supportFragmentManager, "TAG_ITEM_EDIT")
    }


    override fun employeeCreated(employee: Employee) {
        val dbThread = Thread {
            val id = AppDatabase.getInstance(this@MainActivity).employeeDao().insertEmployee(employee)

            employee.employeeId = id

            runOnUiThread {
                adapter.addItem(employee)
            }
        }
        dbThread.start()
    }

    override fun employeeUpdated(employee: Employee) {
        adapter.updateItem(employee)

        val dbThread = Thread {
            AppDatabase.getInstance(this@MainActivity).employeeDao().updateEmployee(employee)

            runOnUiThread { adapter.updateItem(employee) }
        }
        dbThread.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterItems(newText ?: "")
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun filterItems(query: String) {
        val dbThread = Thread {
            val items = AppDatabase.getInstance(this).employeeDao().findEmployeesByName(query)

            runOnUiThread {
                adapter.updateItems(items)
            }
        }
        dbThread.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }
}
