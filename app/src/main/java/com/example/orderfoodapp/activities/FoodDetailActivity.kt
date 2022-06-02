package com.example.orderfoodapp.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.orderfoodapp.R
import com.example.orderfoodapp.fragments.MainMenuFragment
import com.example.orderfoodapp.models.Dish
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.r0adkll.slidr.Slidr
import kotlinx.android.synthetic.main.activity_food_detail.*
import java.io.File
import java.text.DecimalFormat

class FoodDetailActivity : AppCompatActivity() {

    private lateinit var customerEmail: String
    private lateinit var listDish: ArrayList<Dish>

    lateinit var bitmap: Bitmap

    private val df = DecimalFormat("##.00")
    private var sizeChosen = "none"
    private var keyBill = "none"
    private var subTotal = 0.0

    private var priceS: Double = 0.0
    private var priceM: Double = 0.0
    private var priceL: Double = 0.0

    private var keyFav = "none"
    private var isFav = false

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        val fade = Fade()
        window.enterTransition = fade
        window.exitTransition = fade

        Slidr.attach(this)

        listDish = MainMenuFragment.KotlinConstantClass.COMPANION_OBJECT_LIST_DISH

        customerEmail = Firebase.auth.currentUser?.email.toString()
        val curDish = intent.getParcelableExtra<Dish>("curDish")


        if(curDish != null) {
            //get current dish's reference
            val storageRef = FirebaseStorage.getInstance().getReference("dish_image/${curDish.id}.jpg")
            try {
                val localFile = File.createTempFile("tempfile", ".jpg")
                storageRef.getFile(localFile).addOnSuccessListener {
                    bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    food_image.setImageBitmap(bitmap)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

            food_text.text = curDish.name
            rates_text.text = curDish.rated
            time_text.text = curDish.deliveryTime
            description_textView.text = curDish.description
            price_value.text = "0.00"

            val saleOff = curDish.salePercent.toInt()

            val saleS = curDish.priceS*(saleOff*1.0/100)
            val saleM = curDish.priceM*(saleOff*1.0/100)
            val saleL = curDish.priceL*(saleOff*1.0/100)

            priceS = curDish.priceS - saleS
            priceM = curDish.priceM - saleM
            priceL = curDish.priceL - saleL
        }

        //load comments avatars
        val imageName = customerEmail.replace(".", "_")
        val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imageName.jpg")
        try {
            val localFile = File.createTempFile("tempfile", ".jpg")
            storageRef.getFile(localFile).addOnSuccessListener {
                bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                circleImageView.setImageBitmap(bitmap)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        //check whether this dish is in the favourite list and display corresponding icon
        val dbRef = FirebaseDatabase.getInstance().getReference("Favourite")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if((data.child("customerEmail").value)?.equals(customerEmail) == true) {
                        keyFav = data.key.toString()
                        val dbRef2 = FirebaseDatabase.getInstance().getReference("Favourite/$keyFav/products")
                        dbRef2.get().addOnSuccessListener {
                            for(data2 in it.children) {
                                if(data2.value as String == curDish!!.id) {
                                    ic_heart.setImageResource(R.drawable.ic_heart_fill)
                                    isFav = true
                                    break
                                }
                            }
                        }
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodDetailActivity, "Cannot check favourite or not!", Toast.LENGTH_LONG).show()
            }
        })

        //to back to the previous screen
        back_button.setOnClickListener() {
            finish()
        }

        //handle the
        image_s_size.setOnClickListener() {
            resetButton()
            //sizeChosen = "S"
            showCurAmount(curDish!!, it)

            image_s_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            s_size_text.setTextColor(Color.BLACK)
            s_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "S"
        }

        image_m_size.setOnClickListener() {
            resetButton()
            //sizeChosen = "M"
            showCurAmount(curDish!!, it)

            image_m_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            m_size_text.setTextColor(Color.BLACK)
            m_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "M"
        }

        image_l_size.setOnClickListener() {
            resetButton()
            //sizeChosen = "L"
            showCurAmount(curDish!!, it)

            image_l_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            l_size_text.setTextColor(Color.BLACK)
            l_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "L"
        }

        //handle the increase amount btn
        image_increase_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            if(number_text.text.isNotEmpty()) {
                if(amount < number_text.text.toString().toInt()) {
                    amount++
                    amount_text.text = amount.toString()
                    displayPrice()
                }
                else {
                    Toast.makeText(this@FoodDetailActivity, "Maximum", Toast.LENGTH_LONG).show()
                }
            }
            else {
                Toast.makeText(this@FoodDetailActivity, "Please choose a food size!", Toast.LENGTH_LONG).show()
            }
        }

        //handle the decrease amount btn
        image_decrease_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            if(amount > 1) {
                amount--
                amount_text.text = amount.toString()
            }
            displayPrice()
        }

        //find the pending bill of this customer
        findPendingBill()

        addToCart_button.setOnClickListener() {

        }
    }

    private fun findPendingBill() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if((data.child("customerEmail").value)?.equals(customerEmail) == true &&
                        data.child("status").value?.equals("pending") == true) {
                        keyBill = data.key.toString()
                        subTotal = data.child("subTotal").value.toString().toDouble()
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //none
            }
        })
    }



    private fun resetButton() {
        image_s_size.setBackgroundResource(R.drawable.rounded_button)
        image_m_size.setBackgroundResource(R.drawable.rounded_button)
        image_l_size.setBackgroundResource(R.drawable.rounded_button)

        s_size_text.setTextColor(Color.GRAY)
        m_size_text.setTextColor(Color.GRAY)
        l_size_text.setTextColor(Color.GRAY)

        s_size_text.typeface = Typeface.DEFAULT
        m_size_text.typeface = Typeface.DEFAULT
        l_size_text.typeface = Typeface.DEFAULT
    }

    private fun showCurAmount(curDish: Dish, view: View) {
        numLeft_text.text = "Num left: "
        val dbRef = FirebaseDatabase.getInstance().getReference("Product/${curDish.id}")
        dbRef.get().addOnSuccessListener {
            when(view.id) {
                R.id.image_s_size -> {
                    val curAmount = it.child("amountS").value as Long
                    if(curAmount == 0L) {
                        image_s_size.setBackgroundResource(R.drawable.rounded_button_soldout)
                        s_size_text.setTextColor(Color.WHITE)
                        numLeft_text.text = "Sold out!"
                        numLeft_text.setTextColor(Color.RED)
                        number_text.text = ""
                        amount_text.text = ""
                        image_increase_amount.isEnabled = false
                        image_decrease_amount.isEnabled = false
                        displayPriceDefault(curDish)
                    }
                    else {
                        numLeft_text.setTextColor(Color.parseColor("#009E0F"))
                        number_text.text = curAmount.toString()
                        number_text.setTextColor(Color.parseColor("#009E0F"))
                        amount_text.text = "1"
                        image_increase_amount.isEnabled = true
                        image_decrease_amount.isEnabled = true
                        displayPrice()
                    }
                }

                R.id.image_m_size -> {
                    val curAmount = it.child("amountM").value as Long
                    if(curAmount == 0L) {
                        image_m_size.setBackgroundResource(R.drawable.rounded_button_soldout)
                        m_size_text.setTextColor(Color.WHITE)
                        numLeft_text.text = "Sold out!"
                        numLeft_text.setTextColor(Color.RED)
                        number_text.text = ""
                        amount_text.text = ""
                        image_increase_amount.isEnabled = false
                        image_decrease_amount.isEnabled = false
                        displayPriceDefault(curDish)
                    }
                    else {
                        numLeft_text.setTextColor(Color.parseColor("#009E0F"))
                        number_text.text = curAmount.toString()
                        number_text.setTextColor(Color.parseColor("#009E0F"))
                        amount_text.text = "1"
                        image_increase_amount.isEnabled = true
                        image_decrease_amount.isEnabled = true
                        displayPrice()
                    }
                }

                R.id.image_l_size -> {
                    val curAmount = it.child("amountL").value as Long
                    if(curAmount == 0L) {
                        image_l_size.setBackgroundResource(R.drawable.rounded_button_soldout)
                        l_size_text.setTextColor(Color.WHITE)
                        numLeft_text.text = "Sold out!"
                        numLeft_text.setTextColor(Color.RED)
                        number_text.text = ""
                        amount_text.text = ""
                        image_increase_amount.isEnabled = false
                        image_decrease_amount.isEnabled = false
                        displayPriceDefault(curDish)
                    }
                    else {
                        numLeft_text.setTextColor(Color.parseColor("#009E0F"))
                        number_text.text = curAmount.toString()
                        number_text.setTextColor(Color.parseColor("#009E0F"))
                        amount_text.text = "1"
                        image_increase_amount.isEnabled = true
                        image_decrease_amount.isEnabled = true
                        displayPrice()
                    }
                }
            }
        }
    }

    private fun displayPrice() {
        when(sizeChosen) {
            "S" -> price_value.text = df.format(priceS * amount_text.text.toString().toInt())
            "M" -> price_value.text = df.format(priceM * amount_text.text.toString().toInt())
            "L" -> price_value.text = df.format(priceL * amount_text.text.toString().toInt())
        }
    }

    private fun displayPriceDefault(curDish: Dish) {
        when(sizeChosen) {
            "S" -> price_value.text = curDish.priceS.toString()
            "M" -> price_value.text = curDish.priceM.toString()
            "L" -> price_value.text = curDish.priceL.toString()
        }
    }
}