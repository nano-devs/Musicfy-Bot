package database;

import org.joda.time.DateTime;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MYSQL database connection
 */
public abstract class BaseModel
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
        this.url += "?allowMultiQueries=true";
    }

    /**
     * Execute Select query and return
     * @param query SQL query
     * @return the result of Select query as ResultSet
     */
    @Deprecated
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
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        catch (SQLException e)
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
    public int executeUpdateQuery(String query) throws SQLException
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
        }
        catch (SQLException e)
        {
            if (!e.getMessage().contains("Unhandled user-defined exception condition"))
            {
                throw e;
            }
        }
        return -1;
    }
}
