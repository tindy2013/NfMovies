package com.futuretech.nfmovies.Utils;

import com.futuretech.nfmovies.API.*;

public class APIUtil {
    public static ISite getClient(Site site) {
        switch (site) {
            case NFMOVIES:
                return NfMovies.getInstance();
            case TUANZHANG:
                return Tuanzhang.getInstance();
            case DDRK:
                return DDRK.getInstance();
            case DUBOKU:
                return Duboku.getInstance();
        }

        return null;
    }

    public static ISite getClient(String name) {
        switch (name) {
            case NfMovies.NAME:
                return NfMovies.getInstance();
            case Tuanzhang.NAME:
                return Tuanzhang.getInstance();
            case DDRK.NAME:
                return DDRK.getInstance();
            case Duboku.NAME:
                return Duboku.getInstance();
        }
        return null;
    }
}