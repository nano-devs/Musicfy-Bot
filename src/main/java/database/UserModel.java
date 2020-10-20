package database;

import database.Entity.ClassicUser;

import java.sql.*;

public class UserModel extends BaseModel {
    public UserModel() {
        super();
    }

    public void create(long id, int recommendationQuota, int dailyQuota) throws SQLException {
        String query = "INSERT INTO USER (ID, RECOMMENDATION_QUOTA, DAILY_QUOTA) " +
                "VALUES (" + id + ", " + recommendationQuota + ", " + dailyQuota + ")";
        this.executeUpdateQuery(query);
    }

    public ClassicUser read(long id) throws SQLException {
        ClassicUser classicUser = new ClassicUser();

        String query = "SELECT * FROM USER WHERE ID = " + id;
        try (Connection connection = DriverManager.getConnection(this.url,this.username,this.password)) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        int recommendationQuota = result.getInt(2);
                        int dailyQuota = result.getInt(3);
                        classicUser.setId(id);
                        classicUser.setRecommendationQuota(recommendationQuota);
                        classicUser.setDailyQuota(dailyQuota);
                    }
                    else {
                        classicUser = null;
                    }
                }
            }
        }

        return classicUser;
    }

    public void update(long id) {

    }

    public void updateRecommendationQuota(long id, int recommendationQuota) throws SQLException {
        String query =
                "UPDATE USER" +
                " SET RECOMMENDATION_QUOTA = " + recommendationQuota +
                " WHERE ID = " + id;
        this.executeUpdateQuery(query);
    }

    public void updateDailyQuota(long id, int dailyQuota) throws SQLException {
        String query =
                "UPDATE USER" +
                        " SET DAILY_QUOTA = " + dailyQuota +
                        " WHERE ID = " + id;
        this.executeUpdateQuery(query);
    }

    public void delete(long id) {

    }
}
