package database;

public class GuildHistoryModel extends BaseModel
{
    public GuildHistoryModel()
    {
        super();
    }

    /**
     * Add played track by guild mmber to database
     * @param guidId Guild id that the user reside in.
     * @param trackId Track id that the user play
     * @return
     */
    public boolean addGuildHistory(long guidId, long trackId)
    {
        String query = "INSERT INTO GUILD_HISTORY (GUILD_ID, TRACK_ID) " +
                "VALUES (" + guidId + ", " + trackId + ")";
        return this.executeUpdateQuery(query) > 0;
    }
}
