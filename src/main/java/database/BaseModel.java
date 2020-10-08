package database;

import org.joda.time.DateTime;

import java.sql.*;

/**
 * MYSQL database connection
 */
public abstract  class BaseModel
{
    protected String url;
    protected String database;
    protected String username;
    protected String password;

    public BaseModel()
    {
        this.database = "nano";
        this.username = "root";
        this.password = "";
        this.url = "jdbc:mysql://localhost:3306/" + database;
    }

    /**
     * Execute Select query and return
     * @param query SQL query
     * @return the result of Select query as ResultSet
     */
    public ResultSet executeSelectQuery(String query)
    {
        try (
                Connection connection = DriverManager.getConnection(
                        this.url,
                        this.username,
                        this.password)
        )
        {

            try
            {
                PreparedStatement statement = connection.prepareStatement(query);
                return statement.executeQuery();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Execute Insert/Update/Delete query
     * @param query SQL query
     * @return number of changed record
     */
    public int executeUpdateQuery(String query)
    {
        try (
                Connection connection = DriverManager.getConnection(
                        this.url,
                        this.username,
                        this.password)
        )
        {
            try (PreparedStatement statement = connection.prepareStatement(query))
            {
                int counter = statement.executeUpdate();
                return counter;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Check user id is administrator or nor
     * @param id user id.
     * @return true is user id administrator.
     */
    public boolean isAdministrator(long id)
    {
        String query = "SELECT COUNT(USER_ID) " +
                "FROM ADMIN " +
                "WHERE USER_ID = " + id;

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
                        int counter = result.getInt(1);
                        return counter > 0;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check user/guild is premium or not
     * @param id USER ID or GUILD ID
     * @param type "USER" or "GUILD"
     * @return true if the id is registered.
     */
    public boolean isPremium(long id, String type)
    {
        String query = "SELECT COUNT(" + type + "_ID), DATE_END " +
                        "FROM PREMIUM_" + type +
                        " WHERE " + type + "_ID = " + id;

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
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
