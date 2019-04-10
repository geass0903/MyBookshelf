package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BookDetailFragment extends BaseFragment{
    public static final String TAG = BookDetailFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final String ARG_KEY_IMAGE = "arg_key_image";
    public static final String ARG_KEY_TITLE = "arg_key_title";
    public static final String ARG_KEY_AUTHOR = "arg_key_author";


    TextView titleView;
    TextView authorView;
    SimpleDraweeView draweeView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_book_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        draweeView = view.findViewById(R.id.book_detail_image);
        titleView = view.findViewById(R.id.book_detail_title);
        authorView = view.findViewById(R.id.book_detail_author);

        Bundle bundle = getArguments();
        if(bundle != null) {
            String image = bundle.getString(ARG_KEY_IMAGE);
            String title = bundle.getString(ARG_KEY_TITLE);
            String author = bundle.getString(ARG_KEY_AUTHOR);

            draweeView.setImageURI(getImageUri(image));
            titleView.setText(title);
            authorView.setText(author);

        }

    }




    private Uri getImageUri(String url){
        String REGEX_CSV_COMMA = ",";
        String REGEX_SURROUND_DOUBLE_QUOTATION = "^\"|\"$";
        String REGEX_SURROUND_BRACKET = "^\\(|\\)$";

        Pattern sdqPattern = Pattern.compile(REGEX_SURROUND_DOUBLE_QUOTATION);
        Matcher matcher = sdqPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern sbPattern = Pattern.compile(REGEX_SURROUND_BRACKET);
        matcher = sbPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
        String[] arr = cPattern.split(url, -1);
        return Uri.parse(arr[0]);
    }


}