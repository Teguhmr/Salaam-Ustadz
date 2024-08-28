package id.uinjkt.salaamustadz.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import id.uinjkt.salaamustadz.R
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.FragmentHomeBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.ustadz.UstadzTopHomeAdapter
import id.uinjkt.salaamustadz.ui.chat.RoomChatActivity
import id.uinjkt.salaamustadz.ui.chat.RoomChatsViewModel
import id.uinjkt.salaamustadz.ui.home.ustadz.UstadzViewModel
import id.uinjkt.salaamustadz.ui.notification.HistoryNotificationActivity
import id.uinjkt.salaamustadz.ui.profile.ProfileActivity
import id.uinjkt.salaamustadz.utils.Constants
import id.uinjkt.salaamustadz.utils.UstadzType
import id.uinjkt.salaamustadz.utils.firebase.StorageUtil
import id.uinjkt.salaamustadz.viewmodelfactory.RoomChatsViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding as FragmentHomeBinding


    private lateinit var viewModel2: RoomChatsViewModel
    private val senderId: String = FirebaseAuth.getInstance().uid!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel: HomeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val ustadzArray = arrayOf(
            "Ustadz",
            "Ustadzah"
        )

        val adapterUstadz = ViewPagerAdapterUstadz(childFragmentManager, lifecycle)
        binding.viewPager.adapter = adapterUstadz

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = ustadzArray[position]
        }.attach()

        viewModel.userLiveData.observe(viewLifecycleOwner){ state ->
            setUpUi(state)
        }

        binding.btnMessage.setOnClickListener {
            activity?.startActivity(Intent(requireContext(), RoomChatActivity::class.java))
        }
        binding.btnNotify.setOnClickListener {
            activity?.startActivity(Intent(requireContext(), HistoryNotificationActivity::class.java))
        }



        viewModel2 = ViewModelProvider(this, RoomChatsViewModelFactory(senderId,""))[RoomChatsViewModel::class.java]


        viewModel2.chatLiveData.observe(viewLifecycleOwner) { state ->
            val listChat = state.data

            val totalCount = listChat?.filter { it.lastTextFrom != senderId }?.sumOf { it.unreadCount }
            if (totalCount != null) {
                binding.btnMessage.badgeValue = totalCount.toInt()
            }
        }

        setObserver()
    }

    private fun setObserver(){
        val ustadzViewModel: UstadzViewModel = ViewModelProvider(this)[UstadzViewModel::class.java]

        ustadzViewModel.fetchUserListUstadz(UstadzType.USTADZ.role, UstadzType.USTADZAH.role)
        ustadzViewModel.userLiveData.observe(viewLifecycleOwner) { state ->
            processUstadzList(state)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun processUstadzList(state: Result<List<User>?>){
        when (state) {
            is  Result.Error -> {
            }
            is  Result.Loading -> {
            }
            is  Result.Success -> {
                if (!state.data.isNullOrEmpty()) {
                    val data = state.data
                    val ustadzData = data.shuffled()

                    val adapter = UstadzTopHomeAdapter(requireContext(), ustadzData)
                    val rvUstadz = binding.recyclerViewUstadzTop
                    rvUstadz.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
                    rvUstadz.setHasFixedSize(true)
                    rvUstadz.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }
        }

    }

    private fun setUpUi(state: Result<User?>) {
        when (state){
            is Result.Error -> {}
            is Result.Loading -> {}
            is Result.Success -> {
                val data = state.data
                binding.txtUsername.text = state.data?.name
                if (data?.image.isNullOrEmpty()){
                    Glide
                        .with(this)
                        .load(StorageUtil.pathToReference("/images/profile_user.png"))
                        .centerCrop()
                        .placeholder(R.drawable.profile_user)
                        .into(binding.imageProfile)
                } else {
                    Glide
                        .with(this)
                        .load(StorageUtil.pathToReference(data?.image.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.profile_user)
                        .into(binding.imageProfile)
                }

                binding.imageProfile.setOnClickListener {
                    val intent = Intent(requireContext(), ProfileActivity::class.java)
                    intent.putExtra(Constants.KEY_USER_ID, data?.id)
                    activity?.startActivity(intent)
                }
            }
        }
    }
    

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}