
package com.example.rateyourfavoriteanimal

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val selectedAnimal = intent.getStringExtra("selectedAnimal")
        val imageId = intent.getIntExtra("imageId", 0)
        Log.d("MainActivity2", "Received selectedAnimal: $selectedAnimal, imageId: $imageId")

        // Set the animal name and image in the SecondMainActivity
        Log.d("MainActivity2", "Setting Image ID in SecondActivity: $imageId")
        findViewById<ImageView>(R.id.image_animal2).setImageResource(imageId)
        findViewById<TextView>(R.id.text_selected_animal).text = selectedAnimal

        // Retrieve and set the rating from SharedPreferences
        val preferences = getSharedPreferences("animal_ratings", Context.MODE_PRIVATE)
        val defaultRating = 0.0f
        val savedRating = preferences.getFloat(selectedAnimal, defaultRating)
        findViewById<RatingBar>(R.id.ratingBar).rating = savedRating

        // Retrieve and set the saved imageId from SharedPreferences

        val savedImageId = preferences.getInt("imageId_$selectedAnimal", 0)
        Log.d("MainActivity2", "Retrieved savedImageId from SharedPreferences: $savedImageId")

        // Set the image resource after retrieving the saved image ID
        Log.d("MainActivity2", "Retrieved savedImageId from SharedPreferences: $savedImageId")

        // Set a listener to update the rating when it changes
        findViewById<RatingBar>(R.id.ratingBar).setOnRatingBarChangeListener { _, rating, _ ->
            // Save the rating to SharedPreferences
            with(preferences.edit()) {
                putFloat(selectedAnimal, rating)
                apply()
            }
        }

        findViewById<Button>(R.id.btnSaveRating).setOnClickListener {
            // Save the rating to SharedPreferences
            val rating = findViewById<RatingBar>(R.id.ratingBar).rating
            preferences.edit().putFloat(selectedAnimal, rating).apply()

            // Save the corresponding imageId to SharedPreferences
            val imageId = intent.getIntExtra("imageId", 0)
            preferences.edit().putInt("imageId_$selectedAnimal", imageId).apply()
            Log.d("MainActivity2", "Saved imageId to SharedPreferences: $imageId")

            // Call the function to return data to the first activity
            returnDataToFirstActivity()
        }
    }

    fun returnDataToFirstActivity() {
        val myIntent = Intent()

        // Retrieve the selected animal name and saved rating
        val selectedAnimal = intent.getStringExtra("selectedAnimal")

        // Retrieve the corresponding imageId from SharedPreferences
        val preferences = getSharedPreferences("animal_ratings", Context.MODE_PRIVATE)
        val imageId = preferences.getInt("imageId_$selectedAnimal", 0)
        Log.d("MainActivity2", "Received imageId from Intent: $imageId")

        val savedRating = preferences.getFloat(selectedAnimal, 0.0f)

        // Save the imageId and animal name to SharedPreferences
        preferences.edit().putInt("imageId_$selectedAnimal", imageId).apply()
        preferences.edit().putString("animalName_$selectedAnimal", selectedAnimal).apply()

        // Pass the data back to the first activity
        myIntent.putExtra("selectedAnimal", selectedAnimal)
        myIntent.putExtra("savedRating", savedRating)
        myIntent.putExtra("imageId", imageId)

        setResult(Activity.RESULT_OK, myIntent)

        finish()
    }
}