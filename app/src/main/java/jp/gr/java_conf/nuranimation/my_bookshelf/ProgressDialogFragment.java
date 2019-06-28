package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

public class ProgressDialogFragment extends DialogFragment{
    private static final boolean D = true;
    public static final String TAG = ProgressDialogFragment.class.getSimpleName();

    public static final String title = "KEY_TITLE";
    public static final String message = "KEY_MESSAGE";
    public static final String progress = "KEY_PROGRESS";

    private TextView mTextView_Title;
    private TextView mTextView_Message;
    private TextView mTextView_Progress;
    private String mTitle;
    private String mMessage;
    private String mProgress;


    public static ProgressDialogFragment newInstance(Bundle bundle){
        ProgressDialogFragment instance = new ProgressDialogFragment();
        instance.setArguments(bundle);
        return instance;
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(D) Log.d(TAG,"onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(getActivity() == null){
            throw new IllegalArgumentException("getActivity() == null");
        }
        if(getArguments() == null){
            throw new NullPointerException("getArguments() == null");
        }
        Bundle bundle = this.getArguments();
        setCancelable(false);
        mTitle = bundle.getString(ProgressDialogFragment.title);
        mMessage = bundle.getString(ProgressDialogFragment.message);
        mProgress = bundle.getString(ProgressDialogFragment.progress);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.fragment_progress_dialog);
        return builder.create();
    }


    @Override
    public void onStart(){
        super.onStart();
        mTextView_Title = getDialog().findViewById(R.id.fragment_progress_dialog_title);
        mTextView_Message = getDialog().findViewById(R.id.fragment_progress_dialog_message);
        mTextView_Progress = getDialog().findViewById(R.id.fragment_progress_dialog_progress);
        setDialogTitle(mTitle);
        setDialogProgress(mMessage,mProgress);
    }

    public void setDialogTitle(String title){
        if(mTextView_Title == null){
            mTextView_Title = getDialog().findViewById(R.id.fragment_progress_dialog_title);
        }
        if(!TextUtils.isEmpty(title)) {
            mTextView_Title.setText(title);
        }
    }

    public void setDialogProgress(String message, String progress) {
        if (mTextView_Message == null) {
            mTextView_Message = getDialog().findViewById(R.id.fragment_progress_dialog_message);
        }
        if (mTextView_Progress == null) {
            mTextView_Progress = getDialog().findViewById(R.id.fragment_progress_dialog_progress);
        }
        mTextView_Message.setText(message);
        mTextView_Progress.setText(progress);
    }

}


