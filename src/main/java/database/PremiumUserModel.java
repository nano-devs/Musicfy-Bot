package database;

import org.joda.time.DateTime;
import org.joda.time.Months;

public class PremiumUserModel extends BaseModel
{
    public PremiumUserModel ()
    {
        super();
    }

    /**
     * Add a user to become premium
     * @param id User id.
     * @return
     */
    public boolean addPremiumUser(long id)
    {
        DateTime start = DateTime.now();
        DateTime end = start.plus(Months.ONE);

        String startSql = "STR_TO_DATE('" + start.toString("yyyy-MM-dd hh:mm:ss") + "', '%Y-%m-%d %H:%i:%s')";
        String endSql = "STR_TO_DATE('" + end.toString("yyyy-MM-dd hh:mm:ss") + "', '%Y-%m-%d %H:%i:%s')";

        String query = "INSERT INTO PREMIUM_USER " +
                "VALUES (" + id + ", " + startSql + ", " + endSql + ")";

        try
        {
            return this.executeUpdateQuery(query) > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

}
