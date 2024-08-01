package com.example.rateyourfavoriteanimal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : AppCompatActivity() {
    private var  currentSelectedAnimalRating: Float = 0.0f
    private lateinit var selectedAnimalName: String
    private var savedRating: Float = 0.0f
    private var imageIdOfRatedAnimal: Int = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var animalAdapter: ArrayAdapter<Animal>
    private lateinit var animalList: MutableList<Animal>

    data class Animal(val name: String, var rating: Float = 0.0f, val imageId: Int = 0) {
        override fun toString(): String {
            return if (rating != 0.0f) {
                "$name -- Rating: $rating/5.0"
            } else {
                name
            }
        }
    }

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialized animalList
        animalList = mutableListOf(
            Animal("Dog"),
            Animal("Cat"),
            Animal("Bear"),
            Animal("Rabbit")
        )

        //ArrayAdapter for the ListView
        animalAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, animalList)

        //Sets the ArrayAdapter to the ListView
        val animalListView = findViewById<ListView>(R.id.animal_list)
        animalListView.adapter = animalAdapter

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("app_data", Context.MODE_PRIVATE)
        val preferences = getSharedPreferences("animal_ratings", Context.MODE_PRIVATE)

        // Retrieve ratings for each animal
        val ratingDog = preferences.getFloat("Dog", 0.0f)
        val ratingCat = preferences.getFloat("Cat", 0.0f)
        val ratingBear = preferences.getFloat("Bear", 0.0f)
        val ratingRabbit = preferences.getFloat("Rabbit", 0.0f)

        // Function to update the rating for a specific animal
        fun updateRating(animalName: String, newRating: Float) {
            // Find the index of the Animal in the list
            val indexOfAnimalToUpdate = animalList.indexOfFirst { it.name == animalName }

            // Check if the animal is found in the list
            if (indexOfAnimalToUpdate != -1) {
                // Update the rating of the found Animal
                animalList[indexOfAnimalToUpdate].rating = newRating

                // Notify the adapter to update the list view
                animalAdapter.notifyDataSetChanged()
            } else {
                // Handles the case where the animal is not found in the list
                Log.d("MainActivity", "Animal not found in the list: $animalName")
            }
        }

        //Update ratings
        updateRating("Dog", ratingDog)
        updateRating("Cat", ratingCat)
        updateRating("Bear", ratingBear)
        updateRating("Rabbit", ratingRabbit)

        // Retrieve stored values
        selectedAnimalName = sharedPreferences.getString("selectedAnimalName", "") ?: ""
        savedRating = sharedPreferences.getFloat("savedRating", 0.0f)
        imageIdOfRatedAnimal = sharedPreferences.getInt("imageIdOfRatedAnimal", 0)

        findViewById<TextView>(R.id.textView4).text = selectedAnimalName
        findViewById<ImageView>(R.id.image_animal2).setImageResource(imageIdOfRatedAnimal)
        findViewById<RatingBar>(R.id.ratingBar2).rating = savedRating

        if (imageIdOfRatedAnimal != 0) {
            // If imageIdOfRatedAnimal is not zero, set the visibility of views
            findViewById<RatingBar>(R.id.ratingBar2).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textView5).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textView4).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textView3).visibility = View.VISIBLE
            findViewById<ImageView>(R.id.image_animal2).visibility = View.VISIBLE
        } else {
            // If imageIdOfRatedAnimal is zero, hides teh views
            findViewById<RatingBar>(R.id.ratingBar2).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.textView5).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.textView4).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.textView3).visibility = View.INVISIBLE
            findViewById<ImageView>(R.id.image_animal2).visibility = View.INVISIBLE
        }

        val secondActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data = result.data
                val selectedAnimalName = data?.getStringExtra("selectedAnimal")
                val savedRating = data?.getFloatExtra("savedRating", 0.0f)
                val imageIdOfRatedAnimal = data?.getIntExtra("imageId" , 0) ?: 0
                Log.d("MainActivity", "Received Image ID in onActivityResult: $imageIdOfRatedAnimal")


                with(sharedPreferences.edit()) {
                    putString("selectedAnimalName", selectedAnimalName)
                    putFloat("savedRating", savedRating?: 0.0f)
                    putInt("imageIdOfRatedAnimal", imageIdOfRatedAnimal)
                    apply()
                }

                findViewById<RatingBar>(R.id.ratingBar2).visibility = View.VISIBLE
                findViewById<TextView>(R.id.textView5).visibility = View.VISIBLE
                findViewById<TextView>(R.id.textView4).visibility = View.VISIBLE
                findViewById<TextView>(R.id.textView3).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.image_animal2).visibility = View.VISIBLE


                // Find the Animal object in the list by name
                val selectedAnimal = animalList.find { it.name == selectedAnimalName }

                // Update the text view with the selected animal name
                findViewById<TextView>(R.id.textView4).text = selectedAnimalName
                if (imageIdOfRatedAnimal != 0) {
                    Log.d("MainActivity", "Received Image ID: $imageIdOfRatedAnimal")
                    findViewById<ImageView>(R.id.image_animal2).setImageResource(imageIdOfRatedAnimal)
                }
                animalAdapter.notifyDataSetChanged()

                // Update the rating of the selected animal if found
                selectedAnimal?.let {
                    it.rating = savedRating ?: 0.0f
                    // Log the updated rating
                    Log.d("MainActivity", "Updated rating for ${it.name}: ${it.rating}")
                    // Notify the adapter to update the list view
                    animalAdapter.notifyDataSetChanged()
                }

                // Update the current selected animal's rating
                currentSelectedAnimalRating = savedRating ?: 0.0f
                findViewById<RatingBar>(R.id.ratingBar2).rating = currentSelectedAnimalRating
            }
        }

        animalListView.setOnItemClickListener { _, _, position, _ ->
            // Determine which item in the list is selected
            val selectedAnimal = animalList[position]

            // Based on the index of position selected, set the corresponding image
            val imageIdOfSelectedAnimal = when (position) {
                0 -> R.drawable.dog
                1 -> R.drawable.cat
                2 -> R.drawable.bear
                else -> R.drawable.rabbit
            }

            val intent = Intent(this@MainActivity, MainActivity2::class.java)
            intent.putExtra("selectedAnimal", selectedAnimal.name)
            Log.d("MainActivity", "Sending Image ID to SecondActivity: $imageIdOfSelectedAnimal")
            intent.putExtra("imageId", imageIdOfSelectedAnimal)
            secondActivityLauncher.launch(intent)
        }

        fun clearAllRatings() {
            // Clear SharedPreferences for ratings
            val preferences = getSharedPreferences("animal_ratings", Context.MODE_PRIVATE)
            preferences.edit().clear().apply()

            // Update animalList to reset ratings
            animalList.forEach { it.rating = 0.0f }

            // Notify the adapter to update the ListView
            animalAdapter.notifyDataSetChanged()

            sharedPreferences.edit().clear().apply()

            findViewById<RatingBar>(R.id.ratingBar2).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.textView5).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.textView4).visibility = View.INVISIBLE
            findViewById<TextView>(R.id.textView3).visibility = View.INVISIBLE
            findViewById<ImageView>(R.id.image_animal2).visibility = View.INVISIBLE
        }

        val clearButton = findViewById<Button>(R.id.clearButton)

        clearButton.setOnClickListener {

            clearAllRatings()
        }
    }
}
