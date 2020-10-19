package database.Entity;

public class ClassicUser {

    private long id;
    private int dailyQuota;
    private int recommendationQuota;

    public ClassicUser() {

    }

    public ClassicUser(long id, int dailyQuota, int recommendationQuota) {
        this.id = id;
        this.dailyQuota = dailyQuota;
        this.recommendationQuota = recommendationQuota;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDailyQuota() {
        return dailyQuota;
    }

    public void setDailyQuota(int dailyQuota) {
        this.dailyQuota = dailyQuota;
    }

    public int getRecommendationQuota() {
        return recommendationQuota;
    }

    public void setRecommendationQuota(int recommendationQuota) {
        this.recommendationQuota = recommendationQuota;
    }
}
