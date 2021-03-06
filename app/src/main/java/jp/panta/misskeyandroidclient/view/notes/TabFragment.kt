package jp.panta.misskeyandroidclient.view.notes

import android.content.Context
import android.os.Bundle
import android.util.Log

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager.widget.PagerAdapter
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.view.PageableFragmentFactory
import jp.panta.misskeyandroidclient.view.ScrollableTop
import kotlinx.android.synthetic.main.fragment_tab.*

class TabFragment : Fragment(R.layout.fragment_tab), ScrollableTop{



    private var mPagerAdapter: TimelinePagerAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApp = context?.applicationContext as MiApplication

        val sharedPreferences = requireContext().getSharedPreferences(requireContext().getPreferenceName(), Context.MODE_PRIVATE)
        val includeMyRenotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_MY_RENOTES.name, true)
        val includeRenotedMyNotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_RENOTED_MY_NOTES.name, true)
        val includeLocalRenotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_LOCAL_RENOTES.name, true)

        mPagerAdapter = viewPager.adapter as? TimelinePagerAdapter
        if(mPagerAdapter == null){
            mPagerAdapter = TimelinePagerAdapter(this.childFragmentManager, emptyList())
            viewPager.adapter = mPagerAdapter
        }



        Log.d("TabFragment", "設定:$includeLocalRenotes, $includeRenotedMyNotes, $includeMyRenotes")
        miApp.getCurrentAccount().observe(viewLifecycleOwner, Observer { account ->
            val pages = account.pages


            Log.d("TabFragment", "pages:$pages")


            mPagerAdapter?.setList(
                account,
                pages.sortedBy {
                it.weight
            })
            //mPagerAdapter?.notifyDataSetChanged()


            tabLayout.setupWithViewPager(viewPager)


            if(pages.size <= 1){
                tabLayout.visibility = View.GONE
                elevationView.visibility = View.VISIBLE
            }else{
                tabLayout.visibility = View.VISIBLE
                elevationView.visibility = View.GONE
                tabLayout.elevation
            }
        })

    }


    class TimelinePagerAdapter( fragmentManager: FragmentManager, list: List<Page>) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        var requestBaseList: List<Page> = list
            private set
        private var oldRequestBaseSetting = requestBaseList

        var account: Account? = null

        val scrollableTopFragments = ArrayList<ScrollableTop>()
        private val mFragments = ArrayList<Fragment>()

        override fun getCount(): Int {
            return requestBaseList.size
        }

        override fun getItem(position: Int): Fragment {
            Log.d("getItem", "$position, ${requestBaseList[position].pageable().javaClass}")
            val item = requestBaseList[position]
            val fragment = PageableFragmentFactory.create(item)

            if(fragment is ScrollableTop){
                scrollableTopFragments.add(fragment)
            }
            mFragments.add(fragment)
            return fragment
        }


        override fun getPageTitle(position: Int): String{
            val page = requestBaseList[position]
            return page.title
        }

        override fun getItemPosition(any: Any): Int {
            val target = any as Fragment
            if(mFragments.contains(target)){
                return PagerAdapter.POSITION_UNCHANGED
            }
            return PagerAdapter.POSITION_NONE
        }


        fun setList(account: Account, list: List<Page>){
            mFragments.clear()
            oldRequestBaseSetting = requestBaseList
            requestBaseList = list
            this.account = account
            if(requestBaseList != oldRequestBaseSetting){
                notifyDataSetChanged()
            }

        }



    }

    override fun showTop() {
        showTopCurrentFragment()
    }

    private fun showTopCurrentFragment(){
        try{
            mPagerAdapter?.scrollableTopFragments?.forEach{
                it.showTop()
            }
        }catch(e: UninitializedPropertyAccessException){

        }

    }


}