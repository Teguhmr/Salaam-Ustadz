package id.uinjkt.salaamustadz.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import id.uinjkt.salaamustadz.databinding.FragmentEmailSendSuccessBinding

class EmailSendSuccessFragment : Fragment() {
    private var _binding: FragmentEmailSendSuccessBinding? = null
    private val binding get() = _binding as FragmentEmailSendSuccessBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEmailSendSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonBack.setOnClickListener {
            val action = EmailSendSuccessFragmentDirections.actionEmailSendSuccessFragmentToAuthFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}