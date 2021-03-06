package jp.gr.java_conf.nuranimation.my_bookshelf.application;


import android.os.Bundle;
import android.support.transition.Slide;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;

import jp.gr.java_conf.nuranimation.my_bookshelf.background.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.BookDetailFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.ShelfBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.NewBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.SearchBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.SettingsFragment;

/**
 * Created by Kamada on 2019/03/11.
 */


public enum MyBookshelfEvent {
    SELECT_SHELF_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService service = activity.getService();
            if(service != null){
                service.cancelSearch();
            }
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ShelfBooksFragment shelfBooksFragment = new ShelfBooksFragment();
            shelfBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,shelfBooksFragment, ShelfBooksFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    RESELECT_SHELF_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            fragment = fragmentManager.findFragmentByTag(ShelfBooksFragment.TAG);
            if (fragment instanceof ShelfBooksFragment) {
                ((ShelfBooksFragment) fragment).scrollToTop();
            }
        }
    },
    SELECT_SEARCH_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SearchBooksFragment searchBooksFragment = new SearchBooksFragment();
            searchBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,searchBooksFragment, SearchBooksFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    RESELECT_SEARCH_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            fragment = fragmentManager.findFragmentByTag(SearchBooksFragment.TAG);
            if (fragment instanceof SearchBooksFragment) {
                ((SearchBooksFragment) fragment).prepareSearch();
            }
        }
    },
    SELECT_NEW_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService service = activity.getService();
            if(service != null){
                service.cancelSearch();
            }
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            NewBooksFragment newBooksFragment = new NewBooksFragment();
            newBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,newBooksFragment, NewBooksFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    RESELECT_NEW_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }

            fragment = fragmentManager.findFragmentByTag(NewBooksFragment.TAG);
            if (fragment instanceof NewBooksFragment) {
                ((NewBooksFragment) fragment).scrollToTop();
            }

        }
    },
    SELECT_SETTINGS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService service = activity.getService();
            if(service != null){
                service.cancelSearch();
            }
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SettingsFragment settingsFragment = new SettingsFragment();
            settingsFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,settingsFragment, SettingsFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    RESELECT_SETTINGS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
        }
    },
    GO_TO_BOOK_DETAIL {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            BookDetailFragment bookDetailFragment = new BookDetailFragment();
            bookDetailFragment.setArguments(bundle);
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);
            bookDetailFragment.setEnterTransition(slide);
            fragmentTransaction.replace(R.id.contents_container, bookDetailFragment, BookDetailFragment.TAG);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    },
    POP_BACK_STACK_BOOK_DETAIL {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
        }
    },

    CHECK_SEARCH_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(SearchBooksFragment.TAG);
            if (fragment instanceof SearchBooksFragment) {
                ((SearchBooksFragment) fragment).checkSearchState();
            }
        }
    },
    CHECK_RELOAD_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(NewBooksFragment.TAG);
            if (fragment instanceof NewBooksFragment) {
                ((NewBooksFragment) fragment).checkReloadState();
            }
        }
    },
    CHECK_SETTINGS_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(SettingsFragment.TAG);
            if (fragment instanceof SettingsFragment) {
                ((SettingsFragment) fragment).checkSettingsState();
            }
        }
    },




    @SuppressWarnings("unused")
    DEFAULT {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
        }
    };

    abstract public void apply(MainActivity activity, Bundle bundle);

}
