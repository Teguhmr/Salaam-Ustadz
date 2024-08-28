package id.uinjkt.salaamustadz.ui.home.ustadz

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import id.uinjkt.salaamustadz.data.models.user.User
import id.uinjkt.salaamustadz.databinding.FragmentUstadzahBinding
import id.uinjkt.salaamustadz.state.Result
import id.uinjkt.salaamustadz.ui.adapter.ustadz.MyUstadzRecyclerViewAdapter
import id.uinjkt.salaamustadz.utils.UstadzType
import id.uinjkt.salaamustadz.utils.gone
import id.uinjkt.salaamustadz.utils.show

class UstadzahFragment : Fragment() {
    private var _binding: FragmentUstadzahBinding? = null
    private val binding get() = _binding as FragmentUstadzahBinding
    private val viewModel: UstadzViewModel by lazy {
        ViewModelProvider(this)[UstadzViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUstadzahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setProcess()
        setObserver()
    }

    private fun setProcess(){
        viewModel.fetchUserListUstadz(UstadzType.USTADZAH.role)
    }
    private fun setObserver(){
        viewModel.userLiveData.observe(viewLifecycleOwner) { state ->
            processUstadzList(state)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun processUstadzList(state: Result<List<User>?>){
        when (state) {
            is Result.Error -> {

            }
            is Result.Loading -> {

            }
            is Result.Success -> {
                if (!state.data.isNullOrEmpty()) {
                    val adapter = MyUstadzRecyclerViewAdapter(requireContext(), state.data)
                    val rvUstadz = binding.rvUstadzList
                    rvUstadz.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
                    rvUstadz.setHasFixedSize(true)
                    rvUstadz.adapter = adapter
                    adapter.notifyDataSetChanged()
                    binding.emptyUstadzah.gone()
                    binding.rvUstadzList.show()
                } else {
                    binding.rvUstadzList.gone()
                    binding.emptyUstadzah.show()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}