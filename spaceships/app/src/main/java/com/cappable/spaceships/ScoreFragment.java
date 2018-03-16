package com.cappable.spaceships;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ScoreFragment extends Fragment{

    private final String score_1 = "sp_gm1";
    private final String score_2 = "sp_gm2";
    private final String score_3 = "sp_gm3";

    TextView gameMode_1, gameMode_2, gameMode_3;
    SharedPreferences sp;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.score_fragment, container);

        gameMode_1 = (TextView) v.findViewById(R.id.scoreGameMode_1);
        gameMode_2 = (TextView) v.findViewById(R.id.scoreGameMode_2);
        gameMode_3 = (TextView) v.findViewById(R.id.scoreGameMode_3);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        gameMode_1.setText(sp.getInt(score_1, 0) + "");
        gameMode_2.setText(sp.getInt(score_2, 0) + "");
        gameMode_3.setText(sp.getInt(score_3, 0) + "");
        return v;
    }
}
