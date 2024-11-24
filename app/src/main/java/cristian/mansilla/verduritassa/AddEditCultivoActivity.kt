package cristian.mansilla.verduritassa

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddEditCultivoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_cultivo)

        val aliasEditText: EditText = findViewById(R.id.etAlias)
        val fechaSiembraEditText: EditText = findViewById(R.id.etFechaSiembra)
        val spinnerPlanta: Spinner = findViewById(R.id.spinnerPlanta)
        val btnGuardar: Button = findViewById(R.id.btnGuardar)
        val backIcon: ImageView = findViewById(R.id.backIcon)


        db = FirebaseFirestore.getInstance()


        val plantas = arrayOf("Tomates", "Cebollas", "Lechugas", "Apio", "Choclo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, plantas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPlanta.adapter = adapter


        fechaSiembraEditText.setOnClickListener {
            mostrarDatePicker(fechaSiembraEditText)
        }


        val cultivoId = intent.getStringExtra("id")
        if (cultivoId != null) {
            cargarDatosCultivo(cultivoId, aliasEditText, fechaSiembraEditText, spinnerPlanta)
        }


        btnGuardar.setOnClickListener {
            val alias = aliasEditText.text.toString()
            val fechaSiembra = fechaSiembraEditText.text.toString()
            val planta = spinnerPlanta.selectedItem.toString()

            if (alias.isEmpty() || fechaSiembra.isEmpty() || planta.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val fechaCosecha = calcularFechaCosecha(fechaSiembra, planta)

            val cultivo = hashMapOf(
                "alias" to alias,
                "fechaSiembra" to fechaSiembra,
                "planta" to planta,
                "fechaCosecha" to fechaCosecha,
                "userId" to userId
            )

            if (cultivoId == null) {

                db.collection("cultivos").add(cultivo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cultivo guardado correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {

                db.collection("cultivos").document(cultivoId).set(cultivo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cultivo actualizado correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }


        backIcon.setOnClickListener {
            finish()
        }
    }

    private fun mostrarDatePicker(fechaEditText: EditText) {
        val calendario = Calendar.getInstance()
        val anio = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val fecha = "$dayOfMonth/${month + 1}/$year"
            fechaEditText.setText(fecha)
        }, anio, mes, dia)
        datePickerDialog.show()
    }

    private fun cargarDatosCultivo(
        cultivoId: String,
        aliasEditText: EditText,
        fechaSiembraEditText: EditText,
        spinnerPlanta: Spinner
    ) {
        db.collection("cultivos").document(cultivoId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    aliasEditText.setText(document.getString("alias") ?: "")
                    fechaSiembraEditText.setText(document.getString("fechaSiembra") ?: "")

                    val planta = document.getString("planta") ?: ""
                    val index = (spinnerPlanta.adapter as ArrayAdapter<String>).getPosition(planta)
                    spinnerPlanta.setSelection(index)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calcularFechaCosecha(fechaSiembra: String, planta: String): String {
        val tiemposCultivo = mapOf(
            "Tomates" to 80,
            "Cebollas" to 120,
            "Lechugas" to 85,
            "Apio" to 150,
            "Choclo" to 90
        )

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = formatoFecha.parse(fechaSiembra)

        val diasHastaCosecha = tiemposCultivo[planta] ?: 0
        val calendar = Calendar.getInstance()
        calendar.time = fecha!!
        calendar.add(Calendar.DAY_OF_YEAR, diasHastaCosecha)

        return formatoFecha.format(calendar.time)
    }
}
