package com.gsnathan.pdfviewer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView;

/**
 * Created by Gokul Swaminathan on 2/25/2018.
 */

public class LogFragment  extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ChangeLogRecyclerView chgList= (ChangeLogRecyclerView) layoutInflater.inflate(R.layout.fragment_log, null);


        return new AlertDialog.Builder(getActivity(),R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.appChangelog)
                .setView(chgList)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();

    }

}