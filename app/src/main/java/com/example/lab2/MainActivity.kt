package com.example.lab2


import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lab2.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class MainActivity : AppCompatActivity() {
    val URL = "https://smarhouselab2-default-rtdb.europe-west1.firebasedatabase.app/"
    val database = Firebase.database(URL)
    val myRef = database.getReference("status")
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getData()
        toggleState(binding.lampSwitch,binding.lampImg,binding.toggleLamp,binding.lamp)
        toggleState(binding.doorSwitch,binding.doorImg,binding.toggleDoor,binding.door)
        toggleState(binding.windowSwitch,binding.windowImage,binding.toggleWindow,binding.window)

        // listener for mic image view.
        binding.mic.setOnClickListener {
            // on below line we are calling speech recognizer intent.
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            // on below line we are passing language model
            // and model free form in our intent
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // on below line we are passing our
            // language as a default language.
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            // on below line we are specifying a prompt
            // message as speak to text on below line.
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            // on below line we are specifying a try catch block.
            // in this block we are calling a start activity
            // for result method and passing our result code.
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast
                    .makeText(
                        this@MainActivity, " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
    }
        }
    }

    // on below line we are calling on activity result method.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            // on below line we are checking if result code is ok
            if (resultCode == RESULT_OK && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                // on below line we are setting data
                // to our output text view.
                val output = Objects.requireNonNull(res)[0]
                when(output.toLowerCase()){
                    "open door" -> binding.doorSwitch.isChecked = true
                    "close door" -> binding.doorSwitch.isChecked = false
                    "turn off light" -> binding.lampSwitch.isChecked = false
                    "turn on light" -> binding.lampSwitch.isChecked = true
                    "open window" -> binding.windowSwitch.isChecked = true
                    "close window" -> binding.windowSwitch.isChecked = false
                }

            }
        }
    }


    private fun toggleState(switch: Switch, img:ImageView,status: TextView,name:TextView){
        switch.setOnCheckedChangeListener {_, isChecked ->
        // do whatever you need to do when the switch is toggled here
            updateData(isChecked,name.text)
            if(isChecked){
                if(status.text == "OFF") status.text = "ON" else status.text = "OPEN"
                when(name.text){
                    "LAMP" ->  img.setImageResource(R.drawable.ic_baseline_light_mode_24)
                    "DOOR" -> img.setImageResource(R.drawable.door_open)
                    "WINDOW" -> img.setImageResource(R.drawable.window_open)
                }
            }else{
                if(status.text == "ON") status.text = "OFF" else status.text = "CLOSED"
                when(name.text){
                    "LAMP" ->  img.setImageResource(R.drawable.ic_baseline_lightoff_mode_24)
                    "DOOR" -> img.setImageResource(R.drawable.door_closed)
                    "WINDOW" -> img.setImageResource(R.drawable.window_close)
                }

            }
    }
    }

    fun updateData(isChecked:Boolean, name:CharSequence){
        val data = mutableMapOf<String,String>()
        if(name=="LAMP"){
            if(isChecked) data.put(name.toString(),"ON") else data.put(name.toString(),"OFF")
        }else{
            if(isChecked) data.put(name.toString(),"OPEN") else data.put(name.toString(),"CLOSED")
        }

        myRef.updateChildren(data as Map<String, Any>).addOnSuccessListener {
            Toast.makeText(this@MainActivity,"Successfully update",Toast.LENGTH_SHORT).show()
        }

    }


    fun getData() {
        myRef.get().addOnCompleteListener(object : OnCompleteListener<DataSnapshot> {
            override fun onComplete(task: Task<DataSnapshot>) {
                if(task.isSuccessful){
                    val result = task.result
                    val lamp = result.child("LAMP").getValue().toString()
                    val door = result.child("DOOR").getValue().toString()
                    val window = result.child("WINDOW").getValue().toString()
                    if(lamp == "ON") binding.lampSwitch.isChecked = true else binding.lampSwitch.isChecked = false
                    if(door == "OPEN") binding.doorSwitch.isChecked = true else binding.doorSwitch.isChecked = false
                    if(window == "OPEN") binding.windowSwitch.isChecked = true else binding.windowSwitch.isChecked = false

                }else{
                    Toast.makeText(this@MainActivity,"Failed to read",Toast.LENGTH_SHORT).show();
                }
            }
        })
    }
}