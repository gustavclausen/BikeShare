package com.gustavclausen.bikeshare.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gustavclausen.bikeshare.BikeShareApplication

import com.gustavclausen.bikeshare.R
import com.gustavclausen.bikeshare.activities.RegisterBikeActivity
import com.gustavclausen.bikeshare.models.UserDB
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : Fragment() {

    private val mUserDb = UserDB.get()
    private lateinit var mUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.title = getString(R.string.title_account) // Set toolbar title of parent activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_balance_button.setOnClickListener {
            addToUserBalance()
        }

        add_bike_button.setOnClickListener {
            startActivity(Intent(context, RegisterBikeActivity::class.java))
        }

        val userPreferences = context!!.getSharedPreferences(BikeShareApplication.PREF_USER_FILE, Context.MODE_PRIVATE)
        mUserId = userPreferences.getString(BikeShareApplication.PREF_USER_ID, null)

        setUserInfo()
    }

    private fun addToUserBalance() {
        mUserDb.addToBalance(mUserId, 10.0)
        setUserInfo()
    }

    private fun setUserInfo() {
        val user = mUserDb.getUser(mUserId)!!

        user_full_name_text.text = user.fullName
        user_account_balance_text.text = getString(R.string.account_balance_text, user.accountBalance)

        val balanceColor = if (user.accountBalance >= 0.0) R.color.colorPositiveBalance
                           else R.color.colorNegativeBalance

        user_account_balance_text.setTextColor(ContextCompat.getColor(context!!, balanceColor))
    }
}
