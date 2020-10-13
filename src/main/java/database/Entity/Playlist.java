package database.Entity;

import java.util.ArrayList;

public class Playlist
{
    public long id;
    public String name;
    public ArrayList<Track> track;

    public Playlist(int id, String name, int trackCapacity)
    {
        this.id = id;
        this.name = name;
        this.track = new ArrayList<Track>(trackCapacity);
    }
}
