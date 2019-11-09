package com.xuvjso.nfmovies.Entity;

import java.util.List;

public class Episodes {
    private String name;
    private List<Episode> episodes;

    public Episodes(String name, List<Episode> episodes) {
        this.name = name;
        this.episodes = episodes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }
}
