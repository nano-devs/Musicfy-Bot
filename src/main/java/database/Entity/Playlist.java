package database.Entity;

import java.util.ArrayList;

public class Playlist
{
    public long id;
    public String name;
    public ArrayList<Track> track;
    public int trackCount;

    public Playlist(int id, String name, int trackCapacity, int trackCount)
    {
        this.id = id;
        this.name = name;
        this.track = new ArrayList<Track>(trackCapacity);
        this.trackCount = trackCount;
    }
}
