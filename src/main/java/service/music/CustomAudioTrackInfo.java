package service.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class CustomAudioTrackInfo extends AudioTrackInfo {

    public String thumbnail;

    public CustomAudioTrackInfo(String title, String author, long length, String identifier, boolean isStream, String uri) {
        super(title, author, length, identifier, isStream, uri);
    }

    public CustomAudioTrackInfo(String title, String author, long length, String identifier, boolean isStream, String uri, String thumbnail) {
        super(title, author, length, identifier, isStream, uri);
        this.thumbnail = thumbnail;
    }
}
