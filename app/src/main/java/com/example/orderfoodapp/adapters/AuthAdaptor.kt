package com.example.orderfoodapp.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.orderfoodapp.fragments.LoginFragment
import com.example.orderfoodapp.fragments.SignupFragment

class AuthAdaptor(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) LoginFragment();
        else SignupFragment();
    }
}