package database.Entity;

import org.joda.time.DateTime;

public class UserHistory
{
    public String title;
    public String url;
    public DateTime date;

    public UserHistory()
    {
        this.title = "";
        this.url = "";
        this.date = null;
    }
}
