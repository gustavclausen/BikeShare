package com.gustavclausen.bikeshare.view.fragments

import android.arch.lifecycle.ViewModelProviders
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
import com.gustavclausen.bikeshare.data.entities.User
import com.gustavclausen.bikeshare.view.activities.RegisterBikeActivity
import com.gustavclausen.bikeshare.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : Fragment() {

    private var mUser: User? = null
    private lateinit var mUserId: String
    private lateinit var mUserVM: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.title = getString(R.string.title_account) // Set toolbar title of parent activity

        val userPreferences = context!!.getSharedPreferences(BikeShareApplication.PREF_USER_FILE, Context.MODE_PRIVATE)
        mUserId = userPreferences.getString(BikeShareApplication.PREF_USER_ID, null)

        mUserVM = ViewModelProviders.of(this).get(UserViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
    }

    override fun onResume() {
        super.onResume()

        mUser = mUserVM.getById(mUserId)

        setUserInfo()
    }

    private fun addToUserBalance() {
        mUserVM.addToBalance(mUserId, 10.0)
        setUserInfo()
    }

    private fun setUserInfo() {
        user_full_name_text.text = mUser?.fullName
        user_account_balance_text.text = getString(R.string.money_amount_text, mUser!!.accountBalance)

        val balanceColor = if (mUser!!.accountBalance >= 0.0) R.color.colorPositiveBalance
                           else R.color.colorNegativeBalance
        user_account_balance_text.setTextColor(ContextCompat.getColor(context!!, balanceColor))
    }
}
