package apps.trichain.gtalauncher.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import apps.trichain.gtalauncher.R;
import apps.trichain.gtalauncher.databinding.FragmentFirstBinding;
import apps.trichain.gtalauncher.util.SharedPrefsManager;

import static apps.trichain.gtalauncher.util.util.saveNickName;

/**
 * A placeholder fragment containing a simple view.
 */
public class FirstFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private FragmentFirstBinding b;
    private String nickName = "";

    public static FirstFragment newInstance(int index) {
        FirstFragment fragment = new FirstFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_first, container, false);

        SharedPrefsManager sharedPrefsManager = SharedPrefsManager.getInstance(getContext());

        b.edtNickName.setText(sharedPrefsManager.getNickName());

        b.btnSaveNickName.setOnClickListener(v -> {
            nickName = b.edtNickName.getText().toString();

            if (TextUtils.isEmpty(nickName)) {
                Toast.makeText(getContext(), "O apelido é obrigatório", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Salvando apelido", Toast.LENGTH_SHORT).show();
                saveNickName(getContext(), nickName);
            }
        });

        return b.getRoot();
    }
}