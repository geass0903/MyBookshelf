package jp.gr.java_conf.nuranimation.my_bookshelf.background;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;

@SuppressWarnings({"WeakerAccess","unused"})
public class NewBooksThread extends Thread {
    private static final String TAG = NewBooksThread.class.getSimpleName();
    private static final boolean D = false;

    public static final int NO_ERROR = 0;
    public static final int ERROR_EMPTY_AUTHORS_LIST = 1;
    public static final int ERROR_IO_EXCEPTION = 2;
    public static final int ERROR_UNKNOWN = 3;

    private static final String urlBase = "https://app.rakuten.co.jp/services/api/BooksTotal/Search/20170404?applicationId=1028251347039610250";
    private static final String urlFormat = "&format=" + "json";
    private static final String urlFormatVersion = "&formatVersion=" + "2";
    private static final String urlGenre = "&booksGenreId=" + "001"; // Books
    private static final String urlHits = "&hits=20";
    private static final String urlStockFlag = "&outOfStockFlag=" + "1";
    private static final String urlField = "&field=" + "0";
    private static final String urlSort = "&sort=" + "-releaseDate";

    private final List<String> authors;
    private boolean isCanceled;
    private ThreadFinishListener mListener;
    private LocalBroadcastManager mLocalBroadcastManager;

    public static final class Result {
        private final boolean isSuccess;
        private final int errorCode;
        private final String errorMessage;
        private final List<BookData> books;

        private Result(boolean isSuccess, int errorCode, String errorMessage, List<BookData> books) {
            this.isSuccess = isSuccess;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.books = books;
        }

        public boolean isSuccess() {
            return this.isSuccess;
        }

        public int getErrorCode() {
            return this.errorCode;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }

        public List<BookData> getBooks() {
            return new ArrayList<>(this.books);
        }

        public static Result success(List<BookData> books) {
            return new Result(true, NO_ERROR, "no error", books);
        }

        public static Result error(int errorCode, String errorMessage) {
            return new Result(false, errorCode, errorMessage, null);
        }

    }

    private static final class SearchResult {
        private final boolean isSuccess;
        private final String errorMessage;
        private final boolean hasNext;
        private final List<BookData> books;

        private SearchResult(boolean isSuccess, String errorMessage, boolean hasNext, List<BookData> books) {
            this.isSuccess = isSuccess;
            this.errorMessage = errorMessage;
            this.hasNext = hasNext;
            this.books = books;
        }

        public boolean isSuccess() {
            return this.isSuccess;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }

        public boolean hasNext() {
            return this.hasNext;
        }

        public List<BookData> getBooks() {
            return new ArrayList<>(this.books);
        }

        public static SearchResult success(boolean hasNext, List<BookData> books) {
            return new SearchResult(true, "no error", hasNext, books);
        }

        public static SearchResult error(String errorMessage) {
            return new SearchResult(false, errorMessage, false, null);
        }

    }



    public interface ThreadFinishListener {
        void deliverNewBooksResult(Result result);
    }


    public NewBooksThread(Context context, List<String> authors) {
        this.authors = authors;
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        isCanceled = false;
        if (context instanceof ThreadFinishListener) {
            mListener = (ThreadFinishListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }
    }


    @Override
    public void run() {
        List<String> authorsList = new ArrayList<>(authors);
        List<BookData> books = new ArrayList<>();
        Result mResult;
        String progress;
        int count;
        int size  = authorsList.size();

        if (authorsList.size() == 0) {
            mResult = Result.error(ERROR_EMPTY_AUTHORS_LIST, "empty authors");
        } else {
            count = 0;
            progress = count + "/" + size;
            sendProgressMessage(progress);
            for (String author : authorsList) {
                if (isCanceled) {
                    break;
                }
                if (!TextUtils.isEmpty(author)) {
                    int page = 1;
                    boolean hasNext = true;
                    while (hasNext) {
                        SearchResult result = search(author, page);
                        if (result.isSuccess()) {
                            hasNext = result.hasNext();
                            List<BookData> check = result.getBooks();
                            for (BookData book : check) {
                                if (isNewBook(book)) {
                                    String book_author = book.getAuthor();
                                    book_author = book_author.replaceAll("[\\x20\\u3000]", "");
                                    if (book_author.contains(author)) {
                                        if (D) Log.d(TAG, "author: " + author + " add: " + book.getTitle());
                                        books.add(book);
                                    }
                                } else {
                                    hasNext = false;
                                    break;
                                }
                            }
                            page++;
                        } else {
                            hasNext = false;
                        }

                    }
                    count++;
                    progress = count + "/" + size;
                    sendProgressMessage(progress);
                }
            }
            if(books.size() > 0) {
                mResult = Result.success(books);
            }else{
                mResult = Result.error(ERROR_IO_EXCEPTION, "no books");
            }
        }
        if (mListener != null && !isCanceled) {
            mListener.deliverNewBooksResult(mResult);
        }
    }


    public void cancel() {
        if (D) Log.d(TAG, "thread cancel");
        isCanceled = true;
    }



    private SearchResult search(String keyword, int page) {
        int count = 0;
        int last = 0;

        int retried = 0;
        while (retried < 3) {
            if (isCanceled) {
                return SearchResult.error("search canceled");
            }

            HttpsURLConnection connection = null;
            try {
                if (retried > 0) {
                    Thread.sleep(retried * 200);
                }
                Thread.sleep(1000);
                if (TextUtils.isEmpty(keyword)) {
                    return SearchResult.error("empty keyword");
                }
                String urlPage = "&page=" + page;
                String urlKeyword = "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
                String urlString = urlBase + urlFormat + urlFormatVersion + urlGenre + urlHits + urlStockFlag + urlField + urlSort
                        + urlPage + urlKeyword;
                URL url = new URL(urlString);
                if (isCanceled) {
                    return SearchResult.error("search canceled");
                }
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                BufferedReader reader;
                StringBuilder sb = new StringBuilder();
                String line;
                switch (connection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:             // 200
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();
                        JSONObject json = new JSONObject(sb.toString());
                        if (json.has(BookData.JSON_KEY_ITEMS)) {
                            List<BookData> tmp = new ArrayList<>();
                            JSONArray jsonArray = json.getJSONArray(BookData.JSON_KEY_ITEMS);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);
                                if (D) Log.d(TAG, "data: " + data.toString());
                                BookData book = MyBookshelfUtils.convertToBookData(data);
                                tmp.add(book);
                            }
                            if (json.has(BookData.JSON_KEY_COUNT)) {
                                count = json.getInt(BookData.JSON_KEY_COUNT);
                                if (D) Log.d(TAG, "count: " + count);
                            }
                            if (json.has(BookData.JSON_KEY_LAST)) {
                                last = json.getInt(BookData.JSON_KEY_LAST);
                                if (D) Log.d(TAG, "last: " + last);
                            }
                            boolean hasNext = count - last > 0;
                            List<BookData> books = new ArrayList<>(tmp);
                            return SearchResult.success(hasNext, books);
                        } else {
                            return SearchResult.error("No json item");
                        }
                    case HttpURLConnection.HTTP_BAD_REQUEST:    // 400 wrong parameter
                        return SearchResult.error("wrong parameter");
                    case HttpURLConnection.HTTP_NOT_FOUND:      // 404 not success
                    case 429:                                   // 429 too many requests
                    case HttpURLConnection.HTTP_INTERNAL_ERROR: // 500 system error
                    case HttpURLConnection.HTTP_UNAVAILABLE:    // 503 service unavailable
                        // retry
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "InterruptedException");
                // retry
            } catch (IOException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "IOException");
                // retry
            } catch (JSONException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "JSONException");
                // retry
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            retried++;
        }
        return SearchResult.error("search failed");
    }


    private boolean isNewBook(BookData book){
        Calendar baseDate = Calendar.getInstance();
        baseDate.add(Calendar.DAY_OF_MONTH, -14);
        Calendar salesDate = MyBookshelfUtils.parseDate(book.getSalesDate());
        if(salesDate != null){
            return salesDate.compareTo(baseDate) >= 0;
        }else{
            return false;
        }
    }

    private void sendProgressMessage(String progress) {
        Intent intent = new Intent();
        intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
        intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}