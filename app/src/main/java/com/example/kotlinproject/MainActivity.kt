package com.example.kotlinproject
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinproject.models.UserNumber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
class MainActivity : AppCompatActivity() {
    var newRandomNumber:String = ""
    var previousButtonText = ""
    var items = ArrayList<UserNumber>()
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: RvAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RvAdapter(items)
        recyclerView.adapter = adapter
        newRandomNumber(newRandomNumber)
        var new_text = findViewById<TextView>(R.id.new_text);
        var user_number = new_text.text.toString()
        val btn_check = findViewById<Button>(R.id.btn_check)
        val btn_delete = findViewById<Button>(R.id.btn_delete)
        val number_0 = findViewById<Button>(R.id.number_0);
        val number_1 = findViewById<Button>(R.id.number_1);
        val number_2 = findViewById<Button>(R.id.number_2);
        val number_3 = findViewById<Button>(R.id.number_3);
        val number_4 = findViewById<Button>(R.id.number_4);
        val number_5 = findViewById<Button>(R.id.number_5);
        val number_6 = findViewById<Button>(R.id.number_6);
        val number_7 = findViewById<Button>(R.id.number_7);
        val number_8 = findViewById<Button>(R.id.number_8);
        val number_9 = findViewById<Button>(R.id.number_9)

        fun NumberClick(button: Button) {
            button.setOnClickListener{
                previousButtonText += "${button.text}"
                new_text.setText(previousButtonText)
                button.isEnabled = false
                val myColor = R.color.black_white
                button.setBackgroundColor(ContextCompat.getColor(this, myColor))
                user_number = new_text.text.toString()
            }
        }
        fun NumberClickAfter(button: Button) {
            button.isEnabled = true
            val myColor = R.color.purple_200
            button.setBackgroundColor(ContextCompat.getColor(this, myColor))
                new_text.setText("")
        }
        NumberClick(number_0)
        NumberClick(number_1)
        NumberClick(number_2)
        NumberClick(number_3)
        NumberClick(number_4)
        NumberClick(number_5)
        NumberClick(number_6)
        NumberClick(number_7)
        NumberClick(number_8)
        NumberClick(number_9)

        btn_delete.setOnClickListener{
            if (previousButtonText.isNotEmpty()) {
                val txt = previousButtonText.last()
                previousButtonText = previousButtonText.dropLast(1)
                new_text.text = previousButtonText
                val btn_name = "number_"+txt
                fun NumberDelete(button: Button) {
                    if (resources.getResourceEntryName(button.id) == btn_name) {
                        button.isEnabled = true
                        val myColor = R.color.purple_200
                        button.setBackgroundColor(ContextCompat.getColor(this, myColor))

                    }
                }
                NumberDelete(number_0)
                NumberDelete(number_1)
                NumberDelete(number_2)
                NumberDelete(number_3)
                NumberDelete(number_4)
                NumberDelete(number_5)
                NumberDelete(number_6)
                NumberDelete(number_7)
                NumberDelete(number_8)
                NumberDelete(number_9)
            }
        }

        btn_check.setOnClickListener {
            if(user_number.length>4||user_number.length<4){
                previousButtonText = ""
                NumberClickAfter(number_0);NumberClickAfter(number_1);NumberClickAfter(number_2);NumberClickAfter(number_3);NumberClickAfter(number_4);NumberClickAfter(number_5);NumberClickAfter(number_6);NumberClickAfter(number_7);NumberClickAfter(number_8);NumberClickAfter(number_9)
                val message = "4 таңбалы сан жаз"
                val duration = Toast.LENGTH_SHORT // or Toast.LENGTH_LONG
                val toast = Toast.makeText(this, message, duration)
                toast.show()
            }
            else{
                previousButtonText = ""
                NumberClickAfter(number_0);NumberClickAfter(number_1);NumberClickAfter(number_2);NumberClickAfter(number_3);NumberClickAfter(number_4);NumberClickAfter(number_5);NumberClickAfter(number_6);NumberClickAfter(number_7);NumberClickAfter(number_8);NumberClickAfter(number_9)
                if(!user_number.equals(newRandomNumber)){
                        val newRandomNumber1 = newRandomNumber.toInt()/1000
                        val newRandomNumber2 = newRandomNumber.toInt()/100%10
                        val newRandomNumber3 = newRandomNumber.toInt()/10%10
                        val newRandomNumber4 = newRandomNumber.toInt()%10

                        val user_number1 = user_number.toInt()/1000
                        val user_number2 = user_number.toInt()/100%10
                        val user_number3 = user_number.toInt()/10%10
                        val user_number4 = user_number.toInt()%10

                        var buqa_sany = 0
                        var siyr_sany = 0;

                        if (newRandomNumber1==user_number1){
                            buqa_sany = buqa_sany+1;
                        }
                        if (newRandomNumber2==user_number2){
                            buqa_sany = buqa_sany+1;
                        }
                        if (newRandomNumber3==user_number3){
                            buqa_sany = buqa_sany+1;
                        }
                        if (newRandomNumber4==user_number4){
                            buqa_sany = buqa_sany+1;
                        }

                        if (newRandomNumber1!=user_number1  &&  newRandomNumber1==user_number2||newRandomNumber1==user_number3||newRandomNumber1==user_number4){
                            siyr_sany = siyr_sany+1;
                        }
                        if (newRandomNumber2!=user_number2  && newRandomNumber2==user_number1||newRandomNumber2==user_number3||newRandomNumber2==user_number4){
                            siyr_sany = siyr_sany+1;
                        }
                        if (newRandomNumber3 !=user_number3  && newRandomNumber3==user_number1||newRandomNumber3==user_number2 || newRandomNumber3==user_number4){
                            siyr_sany = siyr_sany+1;
                        }
                        if (newRandomNumber4 !=user_number4  && newRandomNumber4==user_number1||newRandomNumber4==user_number2 || newRandomNumber4==user_number3){
                            siyr_sany = siyr_sany+1;
                        }
                        val txt_buqa_sany = buqa_sany.toString()
                        val txt_siyr_sany = siyr_sany.toString()
                        val userNumber = UserNumber(user_number, txt_buqa_sany, txt_siyr_sany)
                        items.add(userNumber)
                        adapter.notifyDataSetChanged()
                        new_text.setText("")
                    }
                    else{
                        val builder = AlertDialog.Builder(this)
                        val itemCount:Int = adapter.itemCount+1
                        builder.setTitle("Сіздің саныңыз: "+newRandomNumber+"\nМүмкіндік саны :"+itemCount)
                        builder.setMessage("Қайтадан бастайық па?")
                        builder.setPositiveButton("Иә") { dialog, which ->
                            newRandomNumber(newRandomNumber)
                            items.clear()
                            new_text.setText("")
                        }
                        builder.setNegativeButton("Жоқ") { dialog, which ->
                            dialog.dismiss()
                        }
                        val dialog = builder.create()
                        dialog.show()
                    }
            }
        }
    }
    fun newRandomNumber(string: String){
        val random = Random()
        var number = random.nextInt(9000) + 1000
        while (!hasUniqueDigits(number)) {
            number = random.nextInt(9000) + 1000
        }
        newRandomNumber = number.toString()
        val text = findViewById<TextView>(R.id.text)
        text.text = newRandomNumber
        return
    }
    fun hasUniqueDigits(num: Int): Boolean {
        val digits = num.toString().toCharArray()
        for (i in 0 until digits.size - 1) {
            for (j in i + 1 until digits.size) {
                if (digits[i] == digits[j]) {
                    return false
                }
            }
        }
        return true
    }
}


