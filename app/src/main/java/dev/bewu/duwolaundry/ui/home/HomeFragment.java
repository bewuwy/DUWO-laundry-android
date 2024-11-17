package dev.bewu.duwolaundry.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dev.bewu.duwolaundry.MainActivity;
import dev.bewu.duwolaundry.R;
import dev.bewu.duwolaundry.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        assert getActivity() != null;
        ((MainActivity) getActivity()).updateAvailability(false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // enlarged QR functionality
        assert getView() != null;
        ImageView qrPass = getView().findViewById(R.id.qrPass);
        ImageView qrPassEnlarged = getView().findViewById(R.id.qrPass_expanded);
        qrPass.setOnClickListener(v -> {
            qrPassEnlarged.setVisibility(View.VISIBLE);
        });

        qrPassEnlarged.setOnClickListener(v -> {
            qrPassEnlarged.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}