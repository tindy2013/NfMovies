package com.xuvjso.nfmovies.Utils;

import com.xuvjso.nfmovies.API.*;

public class APIUtil {
    public static ISite getClient(Site site) {
        switch (site) {
            case NFMOVIES:
                return NfMovies.getInstance();
            case TUANZHANG:
                return Tuanzhang.getInstance();
            case DDRK:
                return DDRK.getInstance();
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
        }
        return null;
    }
}
