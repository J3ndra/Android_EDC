package com.junianto.posedc.menu

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.junianto.posedc.R
import com.junianto.posedc.menu.abort.AbortActivity
import com.junianto.posedc.menu.abort.AbortEnterPinActivity
import com.junianto.posedc.menu.reprint.ReprintActivity
import com.junianto.posedc.menu.sale.SaleActivity
import com.junianto.posedc.menu.sale.SaleTapCardActivity
import com.junianto.posedc.menu.settlements.SettlementsPinActivity

class MenuAdapter(private val buttons: List<MenuButton>, private val context: Context) : RecyclerView.Adapter<MenuAdapter.ButtonViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_button, parent, false)
        return ButtonViewHolder(view)
    }

    override fun getItemCount(): Int {
        return buttons.size
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val button = buttons[position]
        holder.bind(button)
    }

    inner class ButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageIcon: ImageView = itemView.findViewById(R.id.imageIcon)
        private val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        private val textSubtitle: TextView = itemView.findViewById(R.id.textSubtitle)

        fun bind(button: MenuButton) {
            imageIcon.setImageResource(button.iconRes)
            textTitle.text = button.title
            textSubtitle.text = button.subtitle

            itemView.setOnClickListener{
                when (adapterPosition) {
                    0 -> context.startActivity(Intent(context, SaleTapCardActivity::class.java))
                    1 -> context.startActivity(Intent(context, SettlementsPinActivity::class.java))
                    2 -> context.startActivity(Intent(context, AbortEnterPinActivity::class.java))
                    3 -> context.startActivity(Intent(context, ReprintActivity::class.java))
                }
            }
        }
    }
}