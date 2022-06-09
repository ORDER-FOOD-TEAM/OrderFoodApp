package com.example.orderfoodapp.adapters

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.BillDetailActivity
import com.example.orderfoodapp.models.OrderHistory
import kotlinx.android.synthetic.main.activity_bill_detail.*
import kotlinx.android.synthetic.main.item_order_history.view.*

class OrderHistoryAdapter(
    private val orderList: MutableList<OrderHistory>
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHisViewHolder>() {

    class OrderHisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHisViewHolder {
        return OrderHisViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_order_history,
                parent,
                false
            )
        )
    }

    fun addOrder(order: OrderHistory) {
        orderList.add(order)
        notifyItemInserted(orderList.size - 1)
    }

    fun deleteAll() {
        orderList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: OrderHisViewHolder, position: Int) {
        val curOrder = orderList[position]
        holder.itemView.apply {
            order_history_id.text = "#" + curOrder.id
            order_history_cost_and_amout.text = "$${curOrder.total} • ${curOrder.num} item(s)"
            order_history_date.text = curOrder.time

            var color: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.getColor(R.color.baemin)
            } else {
                resources.getColor(R.color.baemin)
            };
            when (curOrder.status) {
                "Pending" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    color = context.getColor(R.color.yellow)
                }
                "Decline" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    color = context.getColor(R.color.red)
                }

                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    color = context.getColor(R.color.baemin)
                }
            }

            order_history_id.setTextColor(color)

                setOnClickListener {
                    val intent = Intent(context, BillDetailActivity::class.java)
                    intent.putExtra("id", curOrder.id)
                    context.startActivities(arrayOf(intent))
                }
            }
        }

        override fun getItemCount(): Int {
            return orderList.size
        }
    }