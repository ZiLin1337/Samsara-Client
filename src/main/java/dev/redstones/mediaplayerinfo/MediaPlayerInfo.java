package dev.redstones.mediaplayerinfo;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayerInfo {
    public static final MediaPlayerInfo Instance = new MediaPlayerInfo();
    
    public List<IMediaSession> getMediaSessions() {
        return new ArrayList<>();
    }
}