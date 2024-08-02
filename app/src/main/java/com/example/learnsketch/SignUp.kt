package com.example.learnsketch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.learnsketch.databinding.SignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {

    private lateinit var binding: SignupBinding
    private  lateinit var database:DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate (savedInstanceState)
        binding=SignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.signupBtn.setOnClickListener {
            doSignup()
        }
    }

    private fun doSignup() {
        val name = binding.name.text.toString()
        val email = binding.email.text.toString()
        val pass = binding.password.text.toString()
        val cpass = binding.cpassword.text.toString()

        val useremail = email.replace(".",",")
        Log.i("tag", email)
        Log.i("aftertag", useremail)




        if (email.isEmpty() || pass.isEmpty() || name.isEmpty() || cpass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if(cpass == pass) {

        }
        else
        {
            Toast.makeText(this, "Check the password", Toast.LENGTH_SHORT)
                .show()
            return
        }

        auth.createUserWithEmailAndPassword(email,pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    database= FirebaseDatabase.getInstance().getReference("Users")
                    val user = User(name,useremail,pass,cpass)
                    database.child(useremail).setValue(user).addOnSuccessListener {
                        binding.name.text.clear()
                        binding.email.text.clear()
                        binding.password.text.clear()
                        binding.cpassword.text.clear()



                        Toast.makeText(this, "Successfully Registered", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, SignIn::class.java)
                        startActivity(intent)

                        Toast.makeText(
                            baseContext, "Success",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // If sign in fails, display a message to the user.

                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error occurred ${it.localizedMessage} ", Toast.LENGTH_SHORT).show()
            }
    }
}