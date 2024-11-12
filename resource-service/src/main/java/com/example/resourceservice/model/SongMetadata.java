package com.example.resourceservice.model;

public class SongMetadata {
    private String name;
    private String artist;
    private String album;
    private String length;
    private String year;
    private Integer resourceId;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(final String album) {
        this.album = album;
    }

    public String getLength() {
        return length;
    }

    public void setLength(final String length) {
        this.length = length;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(final Integer resourceId) {
        this.resourceId = resourceId;
    }
}
