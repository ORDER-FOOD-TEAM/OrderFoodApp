package com.example.orderfoodapp.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodapp.adapters.BillAdapter
import com.example.orderfoodapp.models.BillItem
import com.example.orderfoodapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_bill_detail.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class BillDetailActivity: AppCompatActivity() {
    private lateinit var billAdapter: BillAdapter
    private val df = DecimalFormat("##.##")
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd")
    private val sdf2 = SimpleDateFormat("EEE, d MMM yyyy")
    private var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)

        id = intent.getStringExtra("id").toString()

        billAdapter = BillAdapter(mutableListOf())
        billDetail_recyclerView.adapter = billAdapter

        val layoutManager = LinearLayoutManager(this)
        billDetail_recyclerView.layoutManager = layoutManager

        displayProducts()
        displayBill()
        displayCustomerInfor()

        back_layout.setOnClickListener() {
            finish()
        }
    }

    private fun displayProducts() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$id/products")
        dbRef.get().addOnSuccessListener {
            for(data in it.children) {
                var unitPrice = 0.0
                val a: Any = data.child("unitPrice").value as Any
                val type = a::class.simpleName
                if(type == "Long" || type == "Double")
                    unitPrice = a.toString().toDouble()

                val bill = BillItem(
                    data.child("amount").value as Long,
                    data.child("name").value as String,
                    unitPrice
                )
                billAdapter.addBill(bill)
            }
        }
    }

    private fun displayBill() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$id")
        dbRef.get().addOnSuccessListener {
            var subTotal = 0.0
            val a: Any = it.child("subTotal").value as Any
            val typeA = a::class.simpleName
            if(typeA == "Long" || typeA == "Double")
                subTotal = a.toString().toDouble()

            var total = 0.0
            val b: Any = it.child("total").value as Any
            val typeB = b::class.simpleName
            if(typeB == "Long" || typeB == "Double")
                total = b.toString().toDouble()

            val deliveryFee = total - subTotal

            val date = sdf1.parse(it.child("time").value as String)
            val formattedDate = sdf2.format(date)

            subtotal_textView.text = "$$subTotal"
            total_textView.text = "$$total"
            deliveryFee_textView.text = "$${df.format(deliveryFee)}"
            time_textView.text = formattedDate

            address_textView.text = it.child("address").value.toString()

            statusBill_textView.text = it.child("status").value.toString()

            when(it.child("status").value as String) {
                "Pending" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    statusBill_textView.setTextColor(getColor(R.color.yellow))
                }
                "Decline" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    statusBill_textView.setTextColor(getColor(R.color.red))
                }
                "Done" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    statusBill_textView.setTextColor(getColor(R.color.green))
                }

                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    statusBill_textView.setTextColor(getColor(R.color.baemin))
                }
            }
        }
    }

    private fun displayCustomerInfor() {
        val customerEmail = Firebase.auth.currentUser?.email.toString()
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("email").value as String == customerEmail) {
                        orderID_textView.text = id
                        customerName_textView.text = data.child("fullName").value.toString()
                        phone_textView.text = data.child("phoneNumber").value.toString()

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}
