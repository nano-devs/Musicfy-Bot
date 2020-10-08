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
    public boolean addGuildHistory(long guidId, long userId, long trackId)
    {
        String query = "INSERT INTO GUILD_HISTORY (GUILD_ID, USER_ID, TRACK_ID) " +
                "VALUES (" + guidId + ", " + userId + ", " + trackId + ")";
        return this.executeUpdateQuery(query) > 0;
    }
}
