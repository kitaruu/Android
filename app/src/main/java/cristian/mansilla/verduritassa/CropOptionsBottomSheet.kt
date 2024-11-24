package cristian.mansilla.verduritassa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CropOptionsBottomSheet(
    private val onEdit: () -> Unit,
    private val onDelete: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_crop_options, container, false)
        view.findViewById<Button>(R.id.btnEdit).setOnClickListener {
            onEdit()
            dismiss()
        }
        view.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            onDelete()
            dismiss()
        }
        return view
    }
}
