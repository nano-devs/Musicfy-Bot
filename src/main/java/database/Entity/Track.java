package database.Entity;

public class Track
{
    public long trackId;
    public String title;
    public String url;

    public Track()
    {
        this.trackId = 0;
        this.title = "";
        this.url = "";
    }

    public Track(long id, String name, String url)
    {
        this.trackId = id;
        this.title = name;
        this.url = url;
    }
}
