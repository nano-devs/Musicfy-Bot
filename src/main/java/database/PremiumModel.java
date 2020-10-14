package database;

import org.joda.time.DateTime;
import org.joda.time.Months;

import java.sql.*;

public class PremiumModel extends BaseModel
{
    public PremiumModel()
    {
        super();
    }

    /**
     * Check user/guild is premium or not
     * @param id USER ID or GUILD ID
     * @param table "USER" or "GUILD"
     * @return true if the id is registered.
     */
    public boolean isPremium(long id, String table)
    {
        String query =
                "SELECT COUNT(" + table + "_ID), DATE_END " +
                "FROM PREMIUM_" + table +
                " WHERE " + table + "_ID = " + id;

        try (
                Connection connection = DriverManager.getConnection(
                        this.url,
                        this.username,
                        this.password)
        )
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    if (result.next())
                    {
                        if (result.getLong(1) > 0)
                        {
                            String date = result.getDate("DATE_END").toString();
                            if (DateTime.now().isBefore(DateTime.parse(date)))
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Add a guild to become premium
     * @param id User id or Guild id.
     * @param table "USER" or "GUILD"
     * @return
     */
    public boolean addPremium(long id,  String table)
    {
        DateTime start = DateTime.now();
        DateTime end = start.plus(Months.ONE);

        String startSql = "STR_TO_DATE('" + start.toString("yyyy-MM-dd hh:mm:ss") + "', '%Y-%m-%d %H:%i:%s')";
        String endSql = "STR_TO_DATE('" + end.toString("yyyy-MM-dd hh:mm:ss") + "', '%Y-%m-%d %H:%i:%s')";

        String query =
                "INSERT INTO PREMIUM_" + table + " " +
                "VALUES (" + id + ", " + startSql + ", " + endSql + ")";

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * Renew subscription date
     * @param id User id or Guild id.
     * @param table "USER" or "GUILD"
     * @return true if success renew.
     */
    public boolean renewSubscription(long id, String table)
    {
        DateTime start = DateTime.now();
        DateTime end = start.plus(Months.ONE);

        String startSql = "STR_TO_DATE('" + start.toString("yyyy-MM-dd hh:mm:ss") + "', '%Y-%m-%d %H:%i:%s')";
        String endSql = "STR_TO_DATE('" + end.toString("yyyy-MM-dd hh:mm:ss") + "', '%Y-%m-%d %H:%i:%s')";

        String query =
                "UPDATE PREMIUM_" + table + " " +
                "SET DATE_START = " + startSql + ", " +
                "DATE_END = " + endSql + " " +
                "WHERE " + table + "_ID = " + id;

        return this.executeUpdateQuery(query) > 0;
    }

    /**
     * check user id is has registered as premium or not
     * @param id User id or Guild id.
     * @param table "USER" or "GUILD"
     * @return
     */
    public boolean isNew(long id, String table)
    {
        String query =
                "SELECT COUNT(" + table + "_ID) " +
                "FROM PREMIUM_" + table + " " +
                "WHERE " + table + "_ID = " + id;

        try (
                Connection connection = DriverManager.getConnection(
                        this.url,
                        this.username,
                        this.password)
        )
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    if (result.next())
                    {
                        return result.getInt(1) > 0;
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
