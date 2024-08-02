package com.example.learnsketch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var database: DatabaseReference? = null
    private var firebaseDatabase: FirebaseDatabase? = null

    companion object {
        var fname: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signin)

        // Initialize Firebase Auth
        auth = Firebase.auth

        val signinbtn: Button = findViewById(R.id.btn_Login2)
        signinbtn.setOnClickListener {
            doSignIn()

        }

        val ntreg: TextView = findViewById(R.id.notreg)
        ntreg.setOnClickListener {
            val launchMainScreen = Intent(this@SignIn, SignUp::class.java)
            startActivity(launchMainScreen)
        }
    }

    private fun doSignIn() {
        val email_login = findViewById<EditText>(R.id.email_login)
        val pass_login = findViewById<EditText>(R.id.password_login)

        //null checks on inputs
        if (email_login.text.isEmpty() || pass_login.text.isEmpty()){
            Toast.makeText(this,"Please fill all the fields", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val inputemail = email_login.text.toString()
        val inputpass = pass_login.text.toString()

        auth.signInWithEmailAndPassword(inputemail, inputpass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = Firebase.auth.currentUser
                    user?.let {

                        val email = it.email.toString()
                        val uid = it.uid

                        val useremail = email.replace(".", ",")

                        firebaseDatabase = FirebaseDatabase.getInstance()
                        database = firebaseDatabase?.getReference("Users")

                        database?.child(useremail)?.get()?.addOnSuccessListener {
                            if (it.exists()) {
                                fname = it.child("name").value.toString()
                                val intent = Intent(this, HomeScreen::class.java)
                                intent.putExtra("username", fname)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "username doesnt exist", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                    }


                    Toast.makeText(
                        baseContext, "Success",
                        Toast.LENGTH_SHORT
                    ).show()

                    /*val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        username = user.displayName.toString()

                        if (username != null) {
                            Log.d("Username!", username)
                        }
                        // User is signed in
                    } else {
                        // No user is signed in
                    }*/


                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Authentication failed. ${it.localizedMessage}",
                    Toast.LENGTH_SHORT).show()
            }
    }
}