package io.github.lee0701.gukhanwiki.android.view.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.lee0701.gukhanwiki.android.api.GukhanWikiApi
import io.github.lee0701.gukhanwiki.android.databinding.FragmentFootnoteBinding

class FootnoteBottomSheet(
    val title: String,
    val content: String,
): BottomSheetDialogFragment() {

    private var binding: FragmentFootnoteBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFootnoteBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = binding ?: return
        binding.title.text = title
        binding.webView.loadDataWithBaseURL(
            GukhanWikiApi.DOC_URL.toString(),
            content,
            "text/html",
            "UTF-8",
            null,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        this.binding = null
    }

    companion object {
        const val TAG = "RefBottomSheet"
    }
}