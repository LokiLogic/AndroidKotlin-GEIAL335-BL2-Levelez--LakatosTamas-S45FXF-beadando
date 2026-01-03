package hu.android.kotlinshoppinglist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import hu.android.kotlinshoppinglist.data.Employee

class EmployeeDialog : DialogFragment() {

    private lateinit var employeeHandler: EmployeeHandler

    private lateinit var etName: EditText
    private lateinit var etPosition: EditText
    private lateinit var etSalary: EditText
    private lateinit var etExperience: EditText
    private lateinit var etEmail: EditText

    interface EmployeeHandler {
        fun employeeCreated(employee: Employee)

        fun employeeUpdated(employee: Employee)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is EmployeeHandler) {
            employeeHandler = context
        } else {
            throw RuntimeException("The Activity does not implement the EmployeeHandler interface")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("New Employee")

        initDialogContent(builder)

        builder.setPositiveButton("OK") { _, _ ->
            // keep it empty
        }
        return builder.create()
    }

    private fun initDialogContent(builder: AlertDialog.Builder) {
        val rootView = requireActivity().layoutInflater.inflate(R.layout.dialog_create_employee, null)
        etName = rootView.findViewById(R.id.etName)
        etPosition = rootView.findViewById(R.id.etPosition)
        etSalary = rootView.findViewById(R.id.etSalary)
        etExperience = rootView.findViewById(R.id.etExperience)
        etEmail = rootView.findViewById(R.id.etEmail)
        builder.setView(rootView)

        val arguments = this.arguments
        if (arguments != null &&
                arguments.containsKey(MainActivity.KEY_ITEM_TO_EDIT)) {
            @Suppress("DEPRECATION")
            val employee = arguments.getSerializable(
                    MainActivity.KEY_ITEM_TO_EDIT) as Employee
            etName.setText(employee.name)
            etPosition.setText(employee.position)
            etSalary.setText(employee.salary.toString())
            etExperience.setText(employee.experience.toString())
            etEmail.setText(employee.email)

            builder.setTitle("Edit Employee")
        }
    }


    override fun onResume() {
        super.onResume()

        val dialog = dialog as AlertDialog
        val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)

        positiveButton.setOnClickListener {
            if (etName.text.isNotEmpty() && etPosition.text.isNotEmpty() && etSalary.text.isNotEmpty() && etExperience.text.isNotEmpty() && etEmail.text.isNotEmpty()) {
                val arguments = this.arguments
                if (arguments != null &&
                        arguments.containsKey(MainActivity.KEY_ITEM_TO_EDIT)) {
                    handleEmployeeEdit()
                } else {
                    handleEmployeeCreate()
                }

                dialog.dismiss()
            } else {
                etName.error = "This field can not be empty"
            }
        }
    }

    private fun handleEmployeeCreate() {
        employeeHandler.employeeCreated(Employee(
                null,
                etName.text.toString(),
                etPosition.text.toString(),
                etSalary.text.toString().toInt(),
                etExperience.text.toString().toInt(),
                etEmail.text.toString()
        ))
    }

    private fun handleEmployeeEdit() {
        @Suppress("DEPRECATION")
        val employeeToEdit = arguments?.getSerializable(
                MainActivity.KEY_ITEM_TO_EDIT) as Employee
        employeeToEdit.name = etName.text.toString()
        employeeToEdit.position = etPosition.text.toString()
        employeeToEdit.salary = etSalary.text.toString().toInt()
        employeeToEdit.experience = etExperience.text.toString().toInt()
        employeeToEdit.email = etEmail.text.toString()

        employeeHandler.employeeUpdated(employeeToEdit)
    }
}
