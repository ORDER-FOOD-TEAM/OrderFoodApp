package com.example.orderfoodapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.orderfoodapp.R
import com.example.orderfoodapp.adapters.DishAdapter
import com.example.orderfoodapp.adapters.NewsAdapter
import com.example.orderfoodapp.models.Dish
import com.example.orderfoodapp.models.NewsItem
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MainMenuFragment : Fragment() {

    private val PLACE_PICKER_REQUEST = 1

    private lateinit var dishAdapterNearestRestaurants: DishAdapter
    private lateinit var dishAdapterTopRating: DishAdapter
    private lateinit var dishAdapterOnSale: DishAdapter
    private lateinit var newsAdapter: NewsAdapter

    private val filterAllFoodFragment = FilterAllFoodFragment()
    private val filterDrinkFragment = FilterDrinkFragment()

    private var searchFragment = SearchFragment()
    private var curFragment: Fragment = Fragment()

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    private var listDish = ArrayList<Dish>()

    class KotlinConstantClass {
        companion object {
            var COMPANION_OBJECT_ADDRESS = ""
            var COMPANION_OBJECT_LIST_DISH = ArrayList<Dish>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onResume() {
        super.onResume()




        //create adapter for nearestRestaurant_recyclerView
        dishAdapterNearestRestaurants = DishAdapter(mutableListOf())
        nearestRestaurants_recyclerView.adapter = dishAdapterNearestRestaurants

        //create adapter for trendingNow_recyclerView
        dishAdapterTopRating = DishAdapter(mutableListOf())
        topRating_recyclerView.adapter = dishAdapterTopRating

        //create adapter for onSale_recyclerView
        dishAdapterOnSale = DishAdapter(mutableListOf())
        onSale_recyclerView.adapter = dishAdapterOnSale

        //create adapter for news
        newsAdapter = NewsAdapter(mutableListOf())
        news_recyclerView.adapter = newsAdapter

        val layoutManager1 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        nearestRestaurants_recyclerView.layoutManager = layoutManager1

        val layoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        topRating_recyclerView.layoutManager = layoutManager2

        val layoutManager3 = GridLayoutManager(context,2)
        onSale_recyclerView.layoutManager = layoutManager3

        val layoutManager4 = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        news_recyclerView.layoutManager = layoutManager4

        showSlider()
        showNews()
    }

    private fun showNews() {
        val news1 = NewsItem(
            R.drawable.news1,
            "Best local Burger contest",
            "Just come here and vote for your\nfavourite burger restaurant to win\nvarious prize sponsored by M.T.L Restaurant"
        )

        val news2 = NewsItem(
            R.drawable.news2,
            "Decoding FAD-FOOD-NEWS",
            "How do we make educated choices\nabout which voices to listen to?\nSometimes, it can be very important"
        )

        val news3 = NewsItem(
            R.drawable.news3,
            "World food safety day",
            "You're a food lover? You don't want to\nmiss any hot news about cuisine all\naround the world? Come here!"
        )

        val news4 = NewsItem(
            R.drawable.news4,
            "Best food news websites",
            "Every one has a right to safe, healthy\nand nutritious food, but not everyone\nknow how to do that correctly"
        )

        newsAdapter.addNews(news1)
        newsAdapter.addNews(news2)
        newsAdapter.addNews(news3)
        newsAdapter.addNews(news4)
    }

    private fun showSlider() {
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.banner1, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.banner2))
        imageList.add(SlideModel(R.drawable.banner3, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.banner4))

        banner_slider.setImageList(imageList)
    }
}