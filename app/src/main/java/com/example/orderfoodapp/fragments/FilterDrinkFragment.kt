package com.example.orderfoodapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.orderfoodapp.R
import com.example.orderfoodapp.adapters.DishAdapter
import com.example.orderfoodapp.models.Dish
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_drink.*

class FilterDrinkFragment : Fragment() {

    private lateinit var dishAdapterDrink: DishAdapter

//    private lateinit var database: FirebaseDatabase
//    private lateinit var ref: DatabaseReference

    private lateinit var map: HashMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        map = HashMap()
        val bundle = this.arguments
        if(bundle != null) {
            map = bundle.getSerializable("map") as HashMap<String, String>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drink, container, false)
    }

    override fun onResume() {
        super.onResume()

        //change the adapter of recyclerView to the custom adapter
        dishAdapterDrink = DishAdapter(mutableListOf())
        drink_recyclerView.adapter = dishAdapterDrink

        val layoutManager = GridLayoutManager(context,2)
        drink_recyclerView.layoutManager = layoutManager

        val dbRef = FirebaseDatabase.getInstance().getReference("Product")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //to delete the local
                dishAdapterDrink.deleteAll()
                //then update the new list
                for(data in snapshot.children) {
                    if((data.child("category").value as String) == "Drinking") {
                        val prName = data.child("provider").value as String
                        if(map.containsKey(prName)) {
                            val deliveryTime = map[prName]

                            var priceS = 0.0
                            val a: Any = data.child("priceS").value as Any
                            val typeA = a::class.simpleName
                            if(typeA == "Long" || typeA == "Double")
                                priceS = a.toString().toDouble()

                            var priceM = 0.0
                            val b: Any = data.child("priceM").value as Any
                            val typeB = b::class.simpleName
                            if(typeB == "Long" || typeB == "Double")
                                priceM = b.toString().toDouble()

                            var priceL = 0.0
                            val c: Any = data.child("priceL").value as Any
                            val typeC = c::class.simpleName
                            if(typeC == "Long" || typeC == "Double")
                                priceL = c.toString().toDouble()

                            val dish = Dish(
                                data.child("id").value as String,
                                data.child("name").value as String,
                                priceS,
                                priceM,
                                priceL,
                                data.child("rated").value as String,
                                deliveryTime!!,
                                data.child("category").value as String,
                                data.child("description").value as String,
                                data.child("salePercent").value as Long,
                                data.child("provider").value as String,
                                data.child("amountS").value as Long,
                                data.child("amountSsold").value as Long,
                                data.child("amountM").value as Long,
                                data.child("amountMsold").value as Long,
                                data.child("amountL").value as Long,
                                data.child("amountLsold").value as Long,
                            )
                            dishAdapterDrink.addDish(dish)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                //null
            }
        })

    }

}