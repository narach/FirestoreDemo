package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.data.Brand
import com.example.myapplication.data.User
import com.example.myapplication.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    val TAG = "Firestore!"

    private val brandsCollection = "brands"

    private val brandsCollectionRef = Firebase.firestore.collection(brandsCollection)

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)

        val db = Firebase.firestore
        val user = hashMapOf(
                "first" to "Ada",
                "last" to "Lovelace",
                "born" to 1815
        )
        db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }

        val userObj = User("Siarhei", "Naralenkau", 1988, "Programmer")
        db.collection("users")
                .add(userObj)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }

        val brand = Brand("Nissan", "Japan")
        saveBrand(brand)

        binding.btnSave.setOnClickListener {
            val newBrand = Brand(
                    binding.etBrand.text.toString(),
                    binding.etCountry.text.toString()
            )
            saveBrand(brand)
        }

        binding.btnRetrieve.setOnClickListener {
            retrieveBrands()
        }

        subscribeToRealtimeUpdates()
    }

    private fun saveBrand(brand: Brand) = CoroutineScope(Dispatchers.IO).launch {
        try {
            brandsCollectionRef.add(brand).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Brand was successfully added!", Toast.LENGTH_LONG).show()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrieveBrands() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = brandsCollectionRef.get().await()
            val sb = StringBuilder()
            for(document in querySnapshot.documents) {
                val brand = document.toObject<Brand>()
                sb.append("$brand\n")
            }
            withContext(Dispatchers.Main) {
                binding.tvBrands.text = sb.toString()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun subscribeToRealtimeUpdates() {
        brandsCollectionRef.addSnapshotListener { querySnapshot, ex ->
            ex?.let {
                Toast.makeText(this@MainActivity, ex.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            val sb = StringBuilder()
            querySnapshot?.let {
                for(document in it.documents) {
                    val brand = document.toObject<Brand>()
                    sb.append("$brand\n")
                }
                binding.tvBrands.text = sb.toString()
            }
        }
    }
}