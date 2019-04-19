package com.gustavclausen.bikeshare.view.fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gustavclausen.bikeshare.R
import java.util.*

class OverviewFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.title = getString(R.string.title_overview) // Set toolbar title of parent activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_overview, container, false)

        // Add fragments to adapter
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(BikesOverviewFragment(), getString(R.string.title_bikes_overview))
        viewPagerAdapter.addFragment(RidesOverviewFragment(), getString(R.string.title_rides_overview))

        val viewPager = view.findViewById(R.id.view_pager) as ViewPager
        viewPager.adapter = viewPagerAdapter

        // Setup tab layout with adapter
        view.findViewById<TabLayout>(R.id.overview_tabs).setupWithViewPager(viewPager)

        return view
    }


    inner class ViewPagerAdapter(fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {

        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int) = mFragmentList[position]

        override fun getCount() = mFragmentList.size

        fun addFragment (fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int) = mFragmentTitleList[position]
    }
}
