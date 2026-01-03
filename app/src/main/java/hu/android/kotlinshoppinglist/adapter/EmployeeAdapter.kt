package hu.android.kotlinshoppinglist.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import hu.android.kotlinshoppinglist.MainActivity
import hu.android.kotlinshoppinglist.R
import hu.android.kotlinshoppinglist.adapter.EmployeeAdapter.ViewHolder
import hu.android.kotlinshoppinglist.data.AppDatabase
import hu.android.kotlinshoppinglist.data.Employee
import hu.android.kotlinshoppinglist.touch.ShoppingTouchHelperAdapter
import java.util.*

class EmployeeAdapter : RecyclerView.Adapter<ViewHolder>, ShoppingTouchHelperAdapter {

    private val items = mutableListOf<Employee>()
    private val context: Context

    constructor(context: Context, items: List<Employee>) : super() {
        this.context = context
        this.items.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.row_item, parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val employee = items[position]
        holder.tvName.text = employee.name
        holder.tvPosition.text = employee.position
        holder.tvSalary.text = "Salary: ${employee.salary}"
        holder.tvExperience.text = "Exp: ${employee.experience} years"
        holder.tvEmail.text = employee.email

        holder.btnDelete.setOnClickListener {
            deleteItem(holder.adapterPosition)
        }

        holder.btnEdit.setOnClickListener {
            (holder.itemView.context as MainActivity).showEditEmployeeDialog(employee)
        }
    }

    fun addItem(item: Employee) {
        items.add(item)
        notifyItemInserted(items.lastIndex)
    }

    fun deleteItem(position: Int) {
        val employeeToDelete = items[position]
        val dbThread = Thread {
            AppDatabase.getInstance(context).employeeDao().deleteEmployee(employeeToDelete)
            (context as MainActivity).runOnUiThread {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        }
        dbThread.start()
    }

    fun updateItem(item: Employee) {
        val idx = items.indexOf(item)
        items[idx] = item
        notifyItemChanged(idx)
    }

    fun updateItems(newItems: List<Employee>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onItemDismissed(position: Int) {
        deleteItem(position)
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(items, fromPosition, toPosition)

        notifyItemMoved(fromPosition, toPosition)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)
        val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)
        val tvExperience: TextView = itemView.findViewById(R.id.tvExperience)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
    }
}
