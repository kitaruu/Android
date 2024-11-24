package cristian.mansilla.verduritassa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val nameEditText: EditText = findViewById(R.id.nameEditText)
        val countryEditText: EditText = findViewById(R.id.countryEditText)
        val genderEditText: EditText = findViewById(R.id.genderEditText)
        val registerButton: Button = findViewById(R.id.registerButton)


        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val country = countryEditText.text.toString().trim()
            val gender = genderEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() && country.isNotEmpty() && gender.isNotEmpty()) {

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val userId = auth.currentUser?.uid
                            val userMap = hashMapOf(
                                "email" to email,
                                "name" to name,
                                "country" to country,
                                "gender" to gender
                            )


                            userId?.let {
                                db.collection("usuarios").document(it).set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registro exitoso. Inicia sesión.", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {

                            Toast.makeText(this, "Error de registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
