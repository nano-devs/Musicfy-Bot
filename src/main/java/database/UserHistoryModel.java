package database;

public class UserHistoryModel extends BaseModel
{
    public UserHistoryModel()
    {
        super();
    }

    /**
     * Add played track by user to history database
     * @param userId User id that issued the command
     * @param trackId Track id that the user play
     * @return true if success to insert data to database.
     */
    public boolean addUserHistory(long userId, long trackId)
    {
        String query = "INSERT INTO USER_HISTORY (USER_ID, TRACK_ID) " +
                "VALUES (" + userId + ", " + trackId + ")";
        return this.executeUpdateQuery(query) > 0;
    }

}
