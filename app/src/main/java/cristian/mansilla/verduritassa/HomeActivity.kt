package cristian.mansilla.verduritassa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        val currentUser = auth.currentUser
        val welcomeTextView: TextView = findViewById(R.id.tvWelcome)
        welcomeTextView.text = "Bienvenido ${currentUser?.email}"


        val logoutIcon: ImageView = findViewById(R.id.logoutIcon)
        logoutIcon.setOnClickListener {
            auth.signOut()

            val googleSignInClient = GoogleSignIn.getClient(
                this,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            )
            googleSignInClient.signOut().addOnCompleteListener {
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        val addCropIcon: ImageView = findViewById(R.id.addCropIcon)
        addCropIcon.setOnClickListener {
            val intent = Intent(this, AddEditCultivoActivity::class.java)
            startActivity(intent)
        }
        }


override fun onResume() {
        super.onResume()
        cargarCultivos()
    }
    private fun cargarCultivos() {
        val tableCrops: TableLayout = findViewById(R.id.tableCrops)
        tableCrops.removeAllViews()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(this, "Error: Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }


        db.collection("cultivos")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "No hay cultivos registrados para este usuario.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (document in result) {
                    val alias = document.getString("alias") ?: "Sin Alias"
                    val fechaSiembra = document.getString("fechaSiembra") ?: "Sin Fecha"
                    val fechaCosecha = document.getString("fechaCosecha") ?: "Sin Fecha"

                    val tableRow = TableRow(this).apply {
                        setBackgroundResource(R.drawable.table_row_border)
                    }

                    val tvAlias = crearCeldaTexto(alias)
                    val tvFechaSiembra = crearCeldaTexto(fechaSiembra)
                    val tvFechaCosecha = crearCeldaTexto(fechaCosecha)


                    val settingsIcon = ImageView(this).apply {
                        setImageResource(R.drawable.settings)
                        layoutParams = TableRow.LayoutParams(80, 80).apply {
                            gravity = Gravity.CENTER
                        }
                        setPadding(8, 8, 8, 8)
                        setOnClickListener {
                            mostrarOpciones(document.id, alias)
                        }
                    }

                    tableRow.addView(tvAlias)
                    tableRow.addView(tvFechaSiembra)
                    tableRow.addView(tvFechaCosecha)
                    tableRow.addView(settingsIcon)
                    tableCrops.addView(tableRow)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar cultivos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun mostrarOpciones(cultivoId: String, alias: String) {

        CropOptionsBottomSheet(
            onEdit = { editarCultivo(cultivoId) },
            onDelete = { eliminarCultivo(cultivoId) }
        ).show(supportFragmentManager, "CropOptions")
    }



    private fun editarCultivo(cultivoId: String) {
        val intent = Intent(this, AddEditCultivoActivity::class.java)
        intent.putExtra("id", cultivoId)
        startActivity(intent)
    }

    private fun eliminarCultivo(cultivoId: String) {
        db.collection("cultivos").document(cultivoId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Cultivo eliminado correctamente", Toast.LENGTH_SHORT).show()
                cargarCultivos() // Recargar la tabla
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al eliminar: ${e.message}")
            }
    }


    private fun crearCeldaTexto(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            setBackgroundColor(ContextCompat.getColor(this@HomeActivity, R.color.pure_white))
            setPadding(8, 8, 8, 8)
            gravity = Gravity.CENTER
        }
    }
}

