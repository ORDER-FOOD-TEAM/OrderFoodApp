package com.example.orderfoodapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodapp.R
import com.example.orderfoodapp.adapters.OrderHistoryAdapter
import com.example.orderfoodapp.models.OrderHistory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_order_history.*
import java.text.SimpleDateFormat

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private val sdf1 = SimpleDateFormat("yyyy-MM-dd")
    private val sdf2 = SimpleDateFormat("EEE, d MMM yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        val customerEmail = Firebase.auth.currentUser?.email.toString()

        orderHistoryAdapter = OrderHistoryAdapter(mutableListOf())
        rvOrderList.adapter = orderHistoryAdapter

        val layoutManager = LinearLayoutManager(this)
        rvOrderList.layoutManager = layoutManager

        val dbRef = FirebaseDatabase.getInstance().getReference("Bill")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderHistoryAdapter.deleteAll()
                for (data in snapshot.children) {
                    if ((data.child("customerEmail").value)?.equals(customerEmail) == true
                        && data.child("status").value?.equals("done") == true) {

                        var total = 0.0
                        val a: Any = data.child("total").value as Any
                        val type = a::class.simpleName
                        if(type == "Long" || type == "Double")
                            total = a.toString().toDouble()

                        val dbRef2 = FirebaseDatabase.getInstance().getReference("Bill/${data.key}/products")
                        dbRef2.get().addOnSuccessListener {
                            val count = it.childrenCount.toInt()

                            val date = sdf1.parse(data.child("time").value as String)
                            val formattedDate = sdf2.format(date)

                            val order = OrderHistory(
                                data.key.toString(),
                                total,
                                count,
                                formattedDate
                            )
                            orderHistoryAdapter.addOrder(order)
                        }

                    }
                }
                //set animation
                val layoutAnim = AnimationUtils.loadLayoutAnimation(this@OrderHistoryActivity, R.anim.layout_anim_left_to_right)
                rvOrderList.layoutAnimation = layoutAnim
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        back_layout.setOnClickListener() {
            finish()
        }
    }
}
